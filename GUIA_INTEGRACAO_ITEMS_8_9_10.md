# 🚀 Guia de Integração: Items 8, 9, 10

**Para integrar rapidamente os 3 items no seu ViewModel/Repository**

---

## ⚡ Setup Rápido (5 minutos)

### 1. Injetar Dependências no ViewModel

```kotlin
class TakStudViewModel(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Item 8: Offline Mode
    private val offlineQueue by lazy {
        OfflineSyncQueueImpl(database)
    }

    private val connectivityMonitor by lazy {
        ConnectivityMonitorImpl(context = getApplication<Application>())
    }

    // Item 9: Deduplicação
    private val attendanceDedup by lazy {
        AttendanceDeduplicationManager(database.attendanceDao())
    }

    // Item 10: Batch Operations
    private val gradeBatchManager by lazy {
        GradeBatchManager(database.gradeDao(), firestore)
    }

    init {
        // Item 8: Iniciar monitoramento de conectividade
        connectivityMonitor.startMonitoring()

        // Item 8: Agendar sincronização periódica
        SyncWorkerImpl.schedulePeriodicSync(getApplication<Application>())

        // Item 8: Observar mudanças de conexão
        viewModelScope.launch {
            connectivityMonitor.connectionChanged.collect { isOnline ->
                if (isOnline == true) {
                    Log.i("App", "✅ Internet voltou - iniciando sync")
                    SyncWorkerImpl.triggerImmediateSync(getApplication<Application>())
                    syncOfflineData()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Item 8: Parar monitoramento
        connectivityMonitor.stopMonitoring()
    }
}
```

---

## 📝 Operações Comuns

### Item 8: Salvar Presença Offline

```kotlin
fun markAttendance(studentId: String, date: String, isPresent: Boolean) {
    viewModelScope.launch {
        val record = AttendanceRecord(
            id = "$studentId-$date",
            studentId = studentId,
            date = date,
            isPresent = isPresent,
            modifiedAt = System.currentTimeMillis()
        )

        // Item 8 + 9 integrado
        val result = saveAttendanceWithDeduplicationAndQueue(
            deduplicationManager = attendanceDedup,
            offlineQueue = offlineQueue,
            record = record
        )

        if (result.success) {
            _uiState.update { it.copy(message = "Presença registrada") }
        } else if (result.isDuplicate) {
            _uiState.update { it.copy(message = "Duplicata descartada") }
        }
    }
}
```

### Item 9: Validar Lote de Presença

```kotlin
fun validateAttendanceImport(records: List<AttendanceRecord>) {
    viewModelScope.launch {
        val validation = attendanceDedup.validateBatch(records)

        if (validation.isValid) {
            _uiState.update {
                it.copy(message = "✅ ${validation.validRecords.size} registros válidos")
            }
        } else {
            _uiState.update {
                it.copy(error = "❌ ${validation.issues.size} problemas encontrados")
            }
        }
    }
}
```

### Item 10: Lançar Notas para Turma

```kotlin
fun releaseGradesForClass(
    taskId: String,
    classId: String,
    score: String
) {
    viewModelScope.launch {
        // Obter alunos da turma
        val students = repository.getStudentsByClass(classId)
        val studentIds = students.map { it.id }

        // Item 10 + Item 8 integrado
        val result = bulkReleaseWithQueue(
            manager = gradeBatchManager,
            studentIds = studentIds,
            taskId = taskId,
            score = score,
            offlineQueue = offlineQueue
        )

        if (result.isSuccess) {
            _uiState.update {
                it.copy(message = "✅ Lançadas ${result.created}/${result.totalStudents} notas")
            }
        } else {
            _uiState.update {
                it.copy(error = "❌ Erro: ${result.message}")
            }
        }
    }
}
```

### Item 10: Curva de Notas

```kotlin
fun curveGradesForClass(
    classId: String,
    curvePercentage: Double
) {
    viewModelScope.launch {
        val students = repository.getStudentsByClass(classId)
        val studentIds = students.map { it.id }

        val result = curveGradesWithQueue(
            manager = gradeBatchManager,
            studentIds = studentIds,
            curvePercentage = curvePercentage,
            offlineQueue = offlineQueue
        )

        val report = """
            ✅ Curvadas: ${result.updated} notas
            ⚠️  Limitadas: ${result.capped} (máximo 100)
            ❌ Falhas: ${result.failed}
        """.trimIndent()

        _uiState.update { it.copy(message = report) }
    }
}
```

### Item 10: Salvar Múltiplas Notas

```kotlin
fun saveMultipleGrades(grades: List<Grade>) {
    viewModelScope.launch {
        val result = validateAndSaveGradesBatch(
            manager = gradeBatchManager,
            grades = grades,
            offlineQueue = offlineQueue
        )

        if (result.success) {
            _uiState.update {
                it.copy(message = "✅ Salvos ${result.savedCount} grades")
            }
        } else {
            // Mostrar erros de validação
            val errorMsg = result.invalidGrades.joinToString("\n") { invalid ->
                "Índice ${invalid.index}: ${invalid.reasons.joinToString()}"
            }
            _uiState.update { it.copy(error = errorMsg) }
        }
    }
}
```

---

## 🔄 Sincronização Automática

### Sincronizar Quando Volta Internet

```kotlin
private fun syncOfflineData() {
    viewModelScope.launch {
        try {
            // Sincronizar presença
            Log.d("Sync", "Sincronizando presença...")
            val attendanceResult = offlineQueue.syncAll { item ->
                try {
                    repository.saveAttendanceRecord(item.toModel())
                    true
                } catch (e: Exception) {
                    false
                }
            }

            // Sincronizar grades
            Log.d("Sync", "Sincronizando grades...")
            val gradesResult = syncGradesBatch(gradeBatchManager, database.gradeDao())

            _uiState.update {
                it.copy(
                    message = "✅ Sincronização completa",
                    syncSuccess = true
                )
            }
        } catch (e: Exception) {
            Log.e("Sync", "Erro ao sincronizar", e)
            _uiState.update {
                it.copy(error = "Erro ao sincronizar: ${e.message}")
            }
        }
    }
}
```

---

## 🎯 Observar Status Offline

### Em Composable

```kotlin
@Composable
fun MainScreen(viewModel: TakStudViewModel) {
    val connectivityMonitor = remember { /* obter */ }
    val isOnline = connectivityMonitor.isOnline.collectAsState()
    val networkQuality = connectivityMonitor.networkQuality.collectAsState()

    Column {
        // Status de conectividade
        if (!isOnline.value) {
            Surface(color = Color.Red) {
                Text(
                    "⚠️  Offline - dados serão sincronizados automaticamente",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Surface(color = Color.Green) {
                Text(
                    "✅ Online - ${networkQuality.value}",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Resto do conteúdo
        MainContent(viewModel)
    }
}
```

---

## 📊 Verificar Integridade de Dados

```kotlin
fun checkDataIntegrity() {
    viewModelScope.launch {
        // Item 9: Verificar duplicatas de presença
        val integrityResult = attendanceDedup.performIntegrityCheck()

        if (!integrityResult.isHealthy) {
            Log.w("Integrity", "Banco contém duplicatas: ${integrityResult.duplicatesFound}")
            Log.w("Integrity", "Removidas: ${integrityResult.duplicatesRemoved}")
        }

        // Item 9: Gerar relatório
        val allAttendance = database.attendanceDao().getAllAttendance().first()
        val report = attendanceDedup.generateDeduplicationReport(allAttendance)

        Log.i("Report", report.toString())
    }
}
```

---

## 📋 Checklist de Integração

```
□ Adicionar OfflineSyncQueueImpl ao projeto
□ Adicionar ConnectivityMonitorImpl ao projeto
□ Adicionar SyncWorkerImpl ao projeto
□ Adicionar AttendanceDeduplicationManager ao projeto
□ Adicionar GradeBatchManager ao projeto
□ Injetar em ViewModel
□ Testar offline → online transition
□ Testar lançamento de 30+ notas
□ Testar detecção de duplicata
□ Testar sincronização com Firestore
□ Adicionar observação de status online/offline na UI
□ Documentar no README do projeto
```

---

## 🚨 Tratamento de Erros

```kotlin
// Modo seguro com try-catch
fun safeOperation(block: suspend () -> Unit) {
    viewModelScope.launch {
        try {
            block()
        } catch (e: Exception) {
            Log.e("Operation", "Erro", e)
            _uiState.update { it.copy(error = "Erro: ${e.message}") }
        }
    }
}

// Usar em operações críticas
safeOperation {
    val result = validateAndSaveGradesBatch(...)
    if (!result.success) {
        throw Exception(result.message)
    }
}
```

---

## 🔧 Debugging

### Ativar Verbose Logging

```kotlin
// Em BuildConfig
if (BuildConfig.DEBUG) {
    // Item 8
    Log.i("OfflineQueue", "Debug mode: verbose logging enabled")

    // Item 9
    Log.d("AttendanceDedup", "Dedup debug: detailed tracking")

    // Item 10
    Log.d("GradeBatch", "Batch debug: operation details")
}
```

### Monitorar Fila Offline

```kotlin
fun observeQueueStats() {
    viewModelScope.launch {
        offlineQueue.stats.collect { stats ->
            Log.d("Queue", """
                Total: ${stats.totalItems}
                Pendente: ${stats.pendingItems}
                Sincronizado: ${stats.syncedItems}
                Falhas: ${stats.failedItems}
            """)
        }
    }
}
```

---

## 📚 Referências Rápidas

### Item 8 (Offline Mode)
```kotlin
// Salvar operação
offlineQueue.addOperation(CREATE, "ATTENDANCE", id, record)

// Obter não sincronizados
val unsync = offlineQueue.getUnsyncedItems()

// Sincronizar tudo
offlineQueue.syncAll { item -> firestore.save(item) }

// Status de internet
connectivityMonitor.isOnline.collect { online ->
    if (online) triggerSync()
}
```

### Item 9 (Deduplication)
```kotlin
// Salvar com dedup
dedup.saveAttendanceWithDeduplication(record)

// Detectar duplicatas
val result = dedup.detectDuplicates(list)
Log.i("Dedup", "Únicos: ${result.unique.size}, Duplicatas: ${result.removedCount}")

// Verificar integridade
val health = dedup.performIntegrityCheck()
if (!health.isHealthy) cleanup()

// Validar lote
val validation = dedup.validateBatch(records)
```

### Item 10 (Batch Operations)
```kotlin
// Salvar em batch
val result = gradeBatchManager.saveGradesBatch(grades)

// Lançar para turma
val release = gradeBatchManager.bulkGradeRelease(studentIds, taskId, score)

// Curvar notas
val curve = gradeBatchManager.curveGrades(studentIds, 15.0)

// Sincronizar
val sync = syncGradesBatch(manager, gradeDao)
```

---

## ⚠️ Erros Comuns e Soluções

### "Offline queue is null"
```kotlin
// ❌ Errado
saveAttendanceWithDeduplicationAndQueue(manager, grades, null)

// ✅ Correto
val offlineQueue = OfflineSyncQueueImpl(database)
saveAttendanceWithDeduplicationAndQueue(manager, grades, offlineQueue)
```

### "Batch failed with invalid grades"
```kotlin
// ❌ Errado - sem validação
manager.saveGradesBatch(grades, validateBeforeSave = false)

// ✅ Correto
val result = manager.saveGradesBatch(grades, validateBeforeSave = true)
if (!result.isSuccess) {
    result.invalidGrades.forEach { invalid ->
        Log.e("Validation", "Índice ${invalid.index}: ${invalid.reasons}")
    }
}
```

### "Score out of range"
```kotlin
// ❌ Errado
Grade(score = "150")  // > 100

// ✅ Correto
Grade(score = "85")   // 0-100
```

### "Duplicata detection not working"
```kotlin
// ❌ Errado - salvar sem dedup
gradeDao.insertAttendance(entity)

// ✅ Correto
dedup.saveAttendanceWithDeduplication(record)
```

---

## 📞 Suporte

Se tiver dúvidas:

1. Consultar `IMPLEMENTACAO_ITEM_*.md` para detalhes
2. Checar `*Test.kt` para exemplos de uso
3. Ler KDoc das classes públicas (Ctrl+Q no IDE)
4. Ver logs (Tag: OfflineQueue, AttendanceDedup, GradeBatchManager)

---

**Última atualização**: 13/11/2025
**Versão**: 1.0
**Status**: Production Ready ✅
