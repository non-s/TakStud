# ⚡ Guia Rápido: Deduplicação de Presença (Item 9)

## 🔧 Setup Rápido

### 1. Injetar no ViewModel

```kotlin
class TakStudViewModel : ViewModel() {
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "takstud").build()

    // Injetar manager
    private val attendanceDedup by lazy {
        AttendanceDeduplicationManager(db.attendanceDao())
    }

    private val offlineQueue by lazy {
        OfflineSyncQueueImpl(db)
    }
}
```

### 2. Salvar Presença com Deduplicação

```kotlin
// ✓ Forma recomendada (Item 8 + 9 integrados)
fun saveAttendance(record: AttendanceRecord) {
    viewModelScope.launch {
        val result = saveAttendanceWithDeduplicationAndQueue(
            deduplicationManager = attendanceDedup,
            offlineQueue = offlineQueue,
            record = record
        )

        if (result.success) {
            showSuccess("Presença registrada")
        } else if (result.isDuplicate) {
            showWarning("Presença já registrada")
        }
    }
}
```

### 3. Sincronizar com Deduplicação

```kotlin
fun syncAttendance() {
    viewModelScope.launch {
        val unsyncedRecords = db.attendanceDao()
            .getUnsyncedAttendance()
            .map { it.toModel() }

        val syncResult = processSyncWithDeduplication(
            deduplicationManager = attendanceDedup,
            records = unsyncedRecords,
            syncCallback = { record ->
                try {
                    repository.saveAttendanceRecord(record)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        )

        showStatus("Sincronizados: ${syncResult.successCount}/${syncResult.totalToSync}")
    }
}
```

---

## 📚 API Essencial

### Salvar com Deduplicação
```kotlin
// Retorna: Boolean (true = inserido/atualizado, false = descartado como duplicata)
val saved = manager.saveAttendanceWithDeduplication(record)
```

### Detectar Duplicatas
```kotlin
// Retorna: DuplicateAttendanceResult
val result = manager.detectDuplicates(recordsList)
println("Únicos: ${result.unique.size}, Duplicatas: ${result.removedCount}")
```

### Deduplica antes de Sync
```kotlin
// Retorna: List<AttendanceRecord> (sem duplicatas)
val cleanRecords = manager.deduplicateBeforeSync(recordsList)
```

### Validar Lote
```kotlin
// Retorna: ValidationResult
val validation = manager.validateBatch(recordsList)
if (validation.isValid) {
    saveAll(validation.validRecords)
}
```

### Verificar Integridade
```kotlin
// Retorna: IntegrityCheckResult
val integrity = manager.performIntegrityCheck()
if (!integrity.isHealthy) {
    Log.w("App", "Banco contém duplicatas!")
}
```

### Gerar Relatório
```kotlin
// Retorna: DeduplicationReport (com estatísticas)
val report = manager.generateDeduplicationReport(recordsList)
println(report)  // Mostra relatório formatado
```

---

## 🎯 Cenários Comuns

### Cenário 1: Professor marca presença para turma

```kotlin
fun markAttendanceForClass(classId: String, date: String) {
    val students = repository.getStudentsByClass(classId)

    viewModelScope.launch {
        for (student in students) {
            val record = AttendanceRecord(
                studentId = student.id,
                studentName = student.name,
                date = date,
                isPresent = true,
                modifiedAt = System.currentTimeMillis()
            )

            // Salva com deduplicação automática
            val result = manager.saveAttendanceWithDeduplication(record)

            if (!result) {
                Log.w("Attendance", "Duplicata para ${student.name} descartada")
            }
        }
    }
}
```

### Cenário 2: Aluno marcado 2x por erro

```kotlin
// Primeira marcação (timestamp 1000)
manager.saveAttendanceWithDeduplication(
    AttendanceRecord(studentId = "s001", date = "2025-11-13", isPresent = true, modifiedAt = 1000)
)
// ✅ Inserido

// Segunda marcação (timestamp 2000) - por engano
manager.saveAttendanceWithDeduplication(
    AttendanceRecord(studentId = "s001", date = "2025-11-13", isPresent = false, modifiedAt = 2000)
)
// ✅ Atualizado (timestamp mais recente)

// BD tem: student s001, presente = false, lastModified = 2000
```

### Cenário 3: Validar importação de dados

```kotlin
fun importAttendanceFromFile(file: File) {
    viewModelScope.launch {
        val records = parseAttendanceFile(file)  // Lista de registros

        // Validar
        val validation = manager.validateBatch(records)

        if (!validation.isValid) {
            showErrors("Problemas encontrados: ${validation.issues.size}")
            validation.issues.forEach { issue ->
                Log.e("Import", "Registro ${issue.recordIndex}: ${issue.message}")
            }
            return@launch
        }

        // Salvar registros válidos
        for (record in validation.validRecords) {
            manager.saveAttendanceWithDeduplication(record)
        }

        showSuccess("${validation.validRecords.size} registros importados")
    }
}
```

### Cenário 4: Limpeza periódica (ex: 1x por semana)

```kotlin
// Em WorkManager ou SystemAlarmManager
suspend fun cleanupDuplicateAttendance() {
    val allRecords = db.attendanceDao().getAllAttendance()

    val cleanupResult = cleanupDuplicateAttendance(
        deduplicationManager = manager,
        attendanceDao = db.attendanceDao(),
        records = allRecords
    )

    if (cleanupResult.duplicatesRemoved > 0) {
        Log.i("Cleanup", "Removidas ${cleanupResult.duplicatesRemoved} duplicatas")
    }
}
```

### Cenário 5: Relatório de auditoria

```kotlin
fun generateAuditReport() {
    viewModelScope.launch {
        val allRecords = db.attendanceDao().getAllAttendance()

        val report = generateComprehensiveAttendanceReport(
            deduplicationManager = manager,
            records = allRecords,
            includeDetails = true
        )

        // Mostrar relatório
        showReport(report.toString())

        // Log estatísticas
        Log.i("Audit", """
            Total: ${report.totalRecords}
            Válidos: ${report.validRecords}
            Duplicatas: ${report.duplicatesFound}
            Taxa: ${String.format("%.2f%%", report.deduplicationRate)}
        """)
    }
}
```

---

## 🚨 Tratamento de Erros

```kotlin
// Modo seguro com try-catch
viewModelScope.launch {
    try {
        val result = manager.saveAttendanceWithDeduplication(record)
        if (!result) {
            showWarning("Duplicata detectada")
        }
    } catch (e: Exception) {
        Log.e("Attendance", "Erro ao salvar", e)
        showError("Erro: ${e.message}")
    }
}

// Com fallback
suspend fun saveAttendanceWithFallback(record: AttendanceRecord): Boolean {
    return try {
        manager.saveAttendanceWithDeduplication(record)
    } catch (e: Exception) {
        Log.e("Attendance", "Erro, tentando operação simples", e)
        // Fallback: salva sem deduplicação
        db.attendanceDao().insertAttendance(record.toEntity())
        true
    }
}
```

---

## 📊 Monitoramento

```kotlin
// Observar integridade periodicamente
fun startIntegrityMonitoring() {
    val integrityJob = viewModelScope.launch {
        while (isActive) {
            val result = manager.performIntegrityCheck()

            if (!result.isHealthy) {
                Log.w("Monitor", "Duplicatas detectadas: ${result.duplicatesFound}")
                // Pode disparar limpeza automática
                cleanupDuplicateAttendance()
            }

            delay(1.hours)  // Verificar 1x por hora
        }
    }
}

// Observar stats de deduplicação
viewModelScope.launch {
    offlineQueue.stats.collect { stats ->
        Log.d("Stats", """
            Fila:
            - Total: ${stats.totalItems}
            - Pendente: ${stats.pendingItems}
            - Sincronizados: ${stats.syncedItems}
            - Falhas: ${stats.failedItems}
        """)
    }
}
```

---

## 🔑 Chave Composta

### Formato
```
{studentId}-{date}

Exemplos:
- "student_001-2025-11-13"
- "s01234-2025-12-01"
- "ra:A001-2025-11-15"
```

### Criação
```kotlin
fun createAttendanceId(studentId: String, date: String): String {
    return "$studentId-$date"
}

// Ao salvar
val record = AttendanceRecord(
    id = createAttendanceId(record.studentId, record.date),
    studentId = "student_001",
    date = "2025-11-13",
    ...
)
```

---

## ⏱️ Timestamps (Last-Write-Wins)

### O que é modifiedAt?
```kotlin
// Campo que indica QUANDO o registro foi modificado
data class AttendanceRecord(
    ...
    modifiedAt: Long = System.currentTimeMillis()  // Milisegundos desde 1970
)
```

### Como funciona LWW?
```
Presença 1: timestamp = 1000  ✗ Descartado (antigo)
Presença 2: timestamp = 2000  ✓ Mantido (mais recente)
Presença 3: timestamp = 1500  ✗ Descartado (intermediário)

Resultado: Mantém a Presença 2 (maior timestamp)
```

### Ao Sincronizar
```kotlin
// Detecta qual é a versão mais recente
val online = firestore.getAttendance(id)    // timestamp 2000
val offline = localDb.getAttendance(id)     // timestamp 2000

if (offline.modifiedAt > online.modifiedAt) {
    // Local é mais novo → enviar para Firestore
    firestore.update(offline)
} else {
    // Firestore é mais novo → baixar do servidor
    localDb.update(online)
}
```

---

## 📋 Checklist de Implementação

```
□ Adicionar AttendanceDeduplicationManager ao projeto
□ Injetar em ViewModel ou Activity
□ Modificar saveAttendance() para usar manager
□ Modificar sync para usar deduplicateBeforeSync()
□ Adicionar testes unitários
□ Testar com dados reais (30+ alunos)
□ Adicionar monitoring/logging
□ Documentar no README do projeto
□ Implementar limpeza periódica (WorkManager)
□ Testar offline/online transitions
```

---

## 🎓 Conceitos-Chave

| Conceito | O Quê | Por Quê |
|----------|-------|--------|
| **Chave Composta** | studentId-date | Garante 1 presença por aluno/dia |
| **Last-Write-Wins** | Mantém timestamp mais recente | Resolve conflitos determinísticamente |
| **Deduplicação** | Remove registros duplicados | Garante integridade de dados |
| **Validação** | Verifica antes de salvar | Previne dados inválidos |
| **Integridade** | Verifica banco periodicamente | Detecta inconsistências |

---

## 🔗 Referências

- 📄 `IMPLEMENTACAO_ITEM_9_DEDUPLICACAO.md` - Documentação completa
- 📄 `RESUMO_ITEMS_8_9.md` - Visão geral dos 2 items
- 📄 `IMPLEMENTACAO_ITEM_8_OFFLINE_MODE.md` - Offline mode
- 🧪 `AttendanceDeduplicationManagerTest.kt` - Testes
- 🧪 `AttendanceDeduplicationIntegration.kt` - Integração

---

## ⚡ Dicas de Performance

```kotlin
// ✗ Ineficiente: chamadas repetidas
for (record in records) {
    manager.saveAttendanceWithDeduplication(record)  // Mutex 30x
}

// ✓ Eficiente: validar e salvar batch
val validation = manager.validateBatch(records)
db.attendanceDao().insertAttendances(validation.validRecords)

// ✓ Muito eficiente: deduplicar offline antes de sincronizar
val dedup = manager.deduplicateBeforeSync(queuedRecords)
// Depois sincroniza apenas registros únicos
```

---

**Última atualização**: 13/11/2025
**Versão**: 1.0
