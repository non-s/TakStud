# 🗺️ ROADMAP COMPLETO DE MELHORIAS (30 ITEMS)

**Data de Início**: 12/11/2025 | **Status Inicial**: 5/30 (17%)

---

## SEMANA 1-2: SEGURANÇA (5 melhorias)

### ✅ [CONCLUÍDA] 1. Remover código admin hardcoded
- **Status**: ✅ Implementada
- **Arquivo**: `TakStudViewModel.kt`
- **Impacto**: Alto - Evita exposição de credenciais
- **Tempo**: ~1 hora

### ✅ [CONCLUÍDA] 2. Implementar rate limiting no login
- **Status**: ✅ Implementada
- **Arquivo**: `LoginRateLimiter.kt` (novo)
- **Protege contra**: Força bruta, DDoS de login
- **Tempo**: ~2 horas
- **Teste**: Tentar login 5x falhando → deve bloquear 6ª tentativa
- **Integração pendente**: Techer/ParentLoginScreen

### ✅ [CONCLUÍDA] 3. Criptografar dados em repouso
- **Status**: ✅ Implementada
- **Arquivo**: `SecureSessionManager.kt` (novo)
- **Usa**: EncryptedSharedPreferences (AES256-GCM)
- **Tempo**: ~2 horas
- **Benefício**: Sessões não legíveis mesmo com acesso ao device
- **Integração pendente**: MainActivity.onCreate()

### ✅ [CONCLUÍDA] 4. Adicionar validação de entrada robusta
- **Status**: ✅ Implementada
- **Arquivo**: `AdvancedValidator.kt` (novo)
- **Valida**: 9 tipos diferentes (nome, RA, email, etc)
- **Tempo**: ~3 horas
- **Reutilizável**: Sim, em todas as telas com formulários
- **Integração pendente**: Múltiplas telas

### ✅ [CONCLUÍDA] 5. Melhorar tratamento de erros global
- **Status**: ✅ Implementada
- **Arquivo**: `ErrorHandler.kt` (novo)
- **Recursos**: Retry automático, logging centralizado
- **Tempo**: ~2 horas
- **Benefício**: Melhor UX, menos crashes silenciosos
- **Integração pendente**: Repository, ViewModels

---

## SEMANA 2-3: DADOS & SINCRONIZAÇÃO (6 melhorias)

### ⏳ [PRÓXIMA] 6. Validar relacionamento parent-student
**Prioridade**: ALTA | **Esforço**: 4 horas | **Segurança**: CRÍTICA

**Problema**: Pai consegue acessar qualquer student se souber o ID

**Solução**:
```kotlin
// Em MainActivity.kt - rota Parent
composable("${TakStudDestinations.PARENT_ROUTE}/{studentId}") { backStackEntry ->
    val studentId = backStackEntry.arguments?.getString("studentId")
    val currentSession = sessionManager.getActiveSession()

    // NOVO: Verificar se parent é responsável do student
    val isValidParent = repository.isParentOfStudent(currentSession.userId, studentId)

    if (!isValidParent) {
        navigationActions.navigateToHome()
        return@composable
    }

    // Continuar normalmente
}
```

**Testes**:
- [ ] Parent logado consegue acessar seu próprio student
- [ ] Parent logado NÃO consegue acessar student de outro parent
- [ ] Admin consegue acessar qualquer student

---

### ⏳ [PRÓXIMA] 7. Implementar sync bidirecional com Firestore
**Prioridade**: ALTA | **Esforço**: 1 semana | **Impacto**: CRÍTICO

**Problema**: Apenas faz upload, não baixa mudanças do server

**Arquitetura**:
```kotlin
data class SyncState(
    val id: String,
    val lastModifiedLocal: Long,
    val lastModifiedRemote: Long,
    val isSynced: Boolean = false
)

// Estratégia: Last-Write-Wins com timestamps
suspend fun syncTask(taskEntity: TaskEntity) {
    val remoteDoc = db.collection("tasks").document(taskEntity.id).get().await()
    val remoteTimestamp = remoteDoc.getLong("lastModified") ?: 0L

    if (taskEntity.lastModified > remoteTimestamp) {
        // Local é mais recente - fazer upload
        uploadTask(taskEntity)
    } else {
        // Remote é mais recente - fazer download
        downloadTask(remoteDoc.toObject(Task::class.java))
    }
}
```

**Testes**:
- [ ] Mudança local é enviada para Firebase
- [ ] Mudança remota é baixada para local
- [ ] Conflitos são resolvidos por timestamp
- [ ] Sincronização automática a cada 15 min

---

### ⏳ [PRÓXIMA] 8. Adicionar suporte offline mode com queue de sync
**Prioridade**: ALTA | **Esforço**: 1 semana | **Experiência**: MELHOR

**Problema**: App fica travada sem internet

**Solução - Queue de Sincronização**:
```kotlin
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val operation: String, // CREATE, UPDATE, DELETE
    val entityType: String, // TASK, ATTENDANCE, GRADE
    val entityId: String,
    val data: String, // JSON do objeto
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

// Em Repository
fun queueForSync(operation: String, entity: Any) {
    val item = SyncQueueEntity(
        id = UUID.randomUUID().toString(),
        operation = operation,
        entityType = entity::class.simpleName ?: "UNKNOWN",
        entityId = (entity as? BaseEntity)?.id ?: "",
        data = Gson().toJson(entity)
    )
    db.syncQueueDao().insert(item)
}

// Worker periodicamente tenta sincronizar
class SyncWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        val queue = db.syncQueueDao().getUnsyncedItems()

        for (item in queue) {
            try {
                syncItem(item)
                db.syncQueueDao().markSynced(item.id)
            } catch (e: Exception) {
                // Retry na próxima execução
                return Result.retry()
            }
        }
        return Result.success()
    }
}
```

**Testes**:
- [ ] Desconectar internet
- [ ] Cadastrar novo student (salvo localmente)
- [ ] Reconectar internet
- [ ] Student aparece no Firebase (sincronizado)

---

### ⏳ [PRÓXIMA] 9. Implementar detecção de duplicatas
**Prioridade**: MÉDIA | **Esforço**: 1 dia | **Bug Fix**: Sim

**Problema**: Mesma presença pode ser registrada 2x

**Solução - Unique Constraint**:
```kotlin
@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["studentId", "date"], unique = true) // Força unicidade
    ]
)
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val date: String,
    val isPresent: Boolean,
    // ...
)

// Validação em Repository
suspend fun saveAttendance(record: AttendanceRecord) {
    try {
        db.attendanceDao().insert(record.toEntity())
    } catch (e: SQLiteIntegrityConstraintException) {
        // Duplicata detectada - atualizar ao invés de inserir
        db.attendanceDao().update(record.toEntity())
    }
}
```

---

### ⏳ [PRÓXIMA] 10. Operações em batch para grades
**Prioridade**: MÉDIA | **Esforço**: 1 dia | **Performance**: +300%

**Problema**: Salvando notas uma por uma é lento

**Solução - WriteBatch**:
```kotlin
suspend fun saveGradesBatch(grades: List<Grade>) {
    val batch = db.batch()

    grades.forEach { grade ->
        val docRef = db.collection("grades").document(grade.id)
        batch.set(docRef, grade)
    }

    batch.commit().await()
}

// Em ManageGradesScreen
Button(
    onClick = {
        val newGrades = students.map { student ->
            Grade(
                id = UUID.randomUUID().toString(),
                studentId = student.id,
                taskId = currentTask.id,
                score = scores[student.id] ?: 0.0
            )
        }

        ErrorHandler.withErrorHandling("Salvamento de notas") {
            repository.saveGradesBatch(newGrades)
        }.onSuccess {
            showMessage("Notas salvas com sucesso")
        }
    }
) { Text("Salvar Todas as Notas") }
```

---

### ⏳ [PRÓXIMA] 11. Refatorar padrão callbackFlow duplicado
**Prioridade**: BAIXA (Tech Debt) | **Esforço**: 1 dia | **Maintainability**: +200%

**Problema**: Código duplicado 7x em Repository

**Solução - Função Genérica**:
```kotlin
private inline fun <reified T : Any> observeCollection(
    collectionName: String,
    crossinline mapper: (QuerySnapshot?) -> List<T>
): Flow<List<T>> = callbackFlow {
    val listener = db.collection(collectionName).addSnapshotListener { snapshots, e ->
        if (e != null) {
            ErrorHandler.logError("observeCollection($collectionName)", e)
            close(e)
            return@addSnapshotListener
        }

        try {
            trySend(mapper(snapshots))
        } catch (e: Exception) {
            close(e)
        }
    }

    awaitClose { listener.remove() }
}

// Usar assim:
fun getTasks() = observeCollection("tasks") { snapshot ->
    snapshot?.mapNotNull { doc ->
        doc.toObject(Task::class.java).copy(id = doc.id)
    } ?: emptyList()
}

fun getStudents() = observeCollection("students") { snapshot ->
    snapshot?.mapNotNull { doc ->
        doc.toObject(Student::class.java).copy(id = doc.id)
    } ?: emptyList()
}

// Reduz 821 linhas para ~120 linhas em Repository!
```

---

## SEMANA 3-4: TESTES & DOCUMENTAÇÃO (3 melhorias)

### ⏳ [PRÓXIMA] 12. Aumentar test coverage para 70%+
**Prioridade**: ALTA | **Esforço**: 2 semanas | **Qualidade**: CRÍTICA

**Áreas para testar**:
- [ ] TakStudRepository (unit tests)
- [ ] TakStudViewModel (unit tests)
- [ ] LoginRateLimiter (unit tests)
- [ ] AdvancedValidator (unit tests) - 50+ testes
- [ ] ErrorHandler (unit tests)
- [ ] Authentication flow (integration test)
- [ ] Attendance sync (integration test)

**Setup Necessário**:
```gradle
testImplementation "org.jetbrains.kotlin:kotlin-test:1.9.20"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
testImplementation "io.mockk:mockk:1.13.5"
testImplementation "app.cash.turbine:turbine:1.0.0"
```

**Exemplo de teste**:
```kotlin
class AdvancedValidatorTest {
    @Test
    fun validateName_withValidName_returnsValid() {
        val result = AdvancedValidator.validateName("João Silva")
        assertIs<ValidationResult.Valid<String>>(result)
    }

    @Test
    fun validateName_withInvalidName_returnsError() {
        val result = AdvancedValidator.validateName("A")
        assertIs<ValidationResult.Invalid>(result)
    }

    // ... 50+ testes para cada validador
}
```

---

### ⏳ [PRÓXIMA] 13. Adicionar documentação KDoc completa
**Prioridade**: MÉDIA | **Esforço**: 1 semana | **Manutenção**: +200%

**Arquivos críticos a documentar**:
- TakStudRepository.kt (20+ funções)
- TakStudViewModel.kt (15+ funções)
- SyncWorker.kt (5+ funções)
- Todos os novos arquivos (security/, util/)

**Exemplo KDoc**:
```kotlin
/**
 * Carrega todas as tarefas do professor.
 *
 * Escuta mudanças em tempo real do Firestore e emite quando há atualizações.
 *
 * @return Flow<List<Task>> que emite lista de tarefas conforme mudam
 *
 * @throws FirebaseFirestoreException se falhar conexão
 *
 * Exemplo:
 * ```kotlin
 * viewModel.tasks.collect { tasks ->
 *     updateUI(tasks)
 * }
 * ```
 *
 * @see TakStudViewModel.tasks
 */
fun getTasks(): Flow<List<Task>> { ... }
```

---

### ⏳ [PRÓXIMA] 14. Implementar UiState para loading/error
**Prioridade**: MÉDIA | **Esforço**: 3 dias | **UX**: MELHOR

**Problema**: Sem indicador visual de loading

**Solução**:
```kotlin
sealed class UiState<T> {
    class Loading<T> : UiState<T>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val message: String) : UiState<T>()
}

// Em ViewModel
class TaskListViewModel : ViewModel() {
    private val _tasksState = MutableStateFlow<UiState<List<Task>>>(UiState.Loading())
    val tasksState: StateFlow<UiState<List<Task>>> = _tasksState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            try {
                _tasksState.value = UiState.Loading()
                val tasks = repository.getTasks().first()
                _tasksState.value = UiState.Success(tasks)
            } catch (e: Exception) {
                _tasksState.value = UiState.Error(ErrorHandler.getUserFriendlyMessage(e))
            }
        }
    }
}

// Em Composable
when (val state = tasksState.collectAsState().value) {
    is UiState.Loading -> {
        CircularProgressIndicator()
    }
    is UiState.Success -> {
        TaskListContent(state.data)
    }
    is UiState.Error -> {
        ErrorContent(state.message)
    }
}
```

---

## SEMANA 4-5: FEATURES (4 melhorias)

### ⏳ [PRÓXIMA] 15. Criar relatórios de frequência
**Prioridade**: MÉDIA | **Esforço**: 4 dias | **Valor**: RELATÓRIOS

**Novos cálculos**:
```kotlin
data class AttendanceReport(
    val studentId: String,
    val studentName: String,
    val totalClasses: Int,
    val presentDays: Int,
    val absentDays: Int,
    val percentage: Double // (presentDays / totalClasses) * 100
)

// Em Repository
suspend fun getAttendanceReport(
    studentId: String,
    startDate: LocalDate,
    endDate: LocalDate
): AttendanceReport {
    val records = db.collection("attendance")
        .whereEqualTo("studentId", studentId)
        .whereGreaterThanOrEqualTo("date", startDate.toString())
        .whereLessThanOrEqualTo("date", endDate.toString())
        .get().await()
        .toObjects(AttendanceRecord::class.java)

    val presentCount = records.count { it.isPresent }
    val totalCount = records.size

    return AttendanceReport(
        studentId = studentId,
        studentName = records.firstOrNull()?.studentName ?: "",
        totalClasses = totalCount,
        presentDays = presentCount,
        absentDays = totalCount - presentCount,
        percentage = (presentCount.toDouble() / totalCount) * 100
    )
}
```

**Nova Tela**: `ReportsScreen.kt`
- Mostrar gráficos de presença
- Filtrar por período
- Exportar relatórios

---

### ⏳ [PRÓXIMA] 16. Implementar notificações FCM para pais
**Prioridade**: MÉDIA | **Esforço**: 3 dias | **Comunicação**: MELHOR

**Eventos para notificar**:
- Nova tarefa criada
- Aviso/recado do professor
- Frequência baixa (< 70%)
- Notas atualizadas

**Implementação**:
```kotlin
class TakStudMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"] ?: "TakStud"
        val body = message.data["body"] ?: "Nova mensagem"

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "takstud_notifications")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}

// Para enviar notificação (server-side ou admin)
fun notifyParentAboutAttendance(parentId: String, studentName: String) {
    val message = Message.builder()
        .putData("title", "Aviso de Presença")
        .putData("body", "$studentName teve frequência baixa")
        .setToken(parentFCMToken)
        .build()

    FirebaseMessaging.getInstance().send(message)
}
```

---

### ⏳ [PRÓXIMA] 17. Adicionar busca e filtros
**Prioridade**: MÉDIA | **Esforço**: 2 dias | **Usabilidade**: +150%

**Novos componentes**:
```kotlin
@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilters: List<String>,
    onFilterChange: (List<String>) -> Unit,
    availableFilters: List<String>
) {
    Column {
        TextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Pesquisar...") },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            availableFilters.forEach { filter ->
                FilterChip(
                    selected = filter in selectedFilters,
                    onClick = {
                        if (filter in selectedFilters) {
                            onFilterChange(selectedFilters - filter)
                        } else {
                            onFilterChange(selectedFilters + filter)
                        }
                    },
                    label = { Text(filter) }
                )
            }
        }
    }
}

// Em TaskListScreen
val filteredTasks = tasks.filter { task ->
    (searchQuery.isEmpty() || task.title.contains(searchQuery, ignoreCase = true)) &&
    (selectedFilters.isEmpty() || task.status in selectedFilters)
}
```

---

### ⏳ [PRÓXIMA] 18. Gerenciamento flexível de períodos
**Prioridade**: BAIXA | **Esforço**: 2 dias | **Flexibilidade**: +300%

**Problema**: Períodos hardcoded (MANHÃ, TARDE, EJA)

**Solução**:
```kotlin
@Entity(tableName = "periods")
data class PeriodEntity(
    @PrimaryKey val id: String,
    val name: String,     // "Manhã", "Tarde", "EJA", etc
    val startTime: String, // "07:00"
    val endTime: String,   // "12:00"
    val color: Long = Color.Blue.toLong()
)

// Admin pode criar novos períodos
// App carrega do banco ao invés de hardcoded
```

---

## SEMANA 5-6: UI/UX (7 melhorias)

### ⏳ [PRÓXIMA] 19. Melhorias de acessibilidade WCAG 2.1
**Prioridade**: ALTA | **Esforço**: 2 semanas | **Legal**: IMPORTANTE

**Áreas**:
- [ ] Contraste (4.5:1 para texto normal)
- [ ] Tamanho mínimo de fonte (16sp)
- [ ] Touch targets (48dp mínimo)
- [ ] Navegação por teclado
- [ ] Descrições semânticas

**Exemplo**:
```kotlin
IconButton(
    onClick = onBack,
    modifier = Modifier
        .semantics {
            contentDescription = stringResource(R.string.accessibility_back_button)
            role = Role.Button
        }
        .size(48.dp)  // Mínimo 48x48dp
) {
    Icon(Icons.Default.ArrowBack, null)
}

Text(
    text = taskTitle,
    fontSize = 16.sp,  // Mínimo 16sp
    color = NavyBlue,  // Contraste suficiente
    modifier = Modifier.semantics {
        contentDescription = "Título: $taskTitle"
    }
)
```

---

### ⏳ [PRÓXIMA] 20. Remover emoji e usar Material Design icons
**Prioridade**: BAIXA | **Esforço**: 1 dia | **Profissionalismo**: +200%

**Mudanças**:
```kotlin
// ANTES:
Text("☀️ Manhã")
Text("👨‍🏫 Gerenciar Alunos")

// DEPOIS:
Icon(Icons.Filled.WbSunny)
Text("Manhã")

Icon(Icons.Filled.PeopleAlt)
Text("Gerenciar Alunos")
```

---

### ⏳ [PRÓXIMA] 21. Layouts responsivos para tablet
**Prioridade**: MÉDIA | **Esforço**: 2 dias | **UX TABLET**: CRIADO

**Usar WindowSizeClass**:
```kotlin
@Composable
fun AdaptiveTaskListScreen(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Phone layout
            TaskListPhone()
        }
        WindowWidthSizeClass.Medium -> {
            // Tablet layout (2 colunas)
            TaskListTablet()
        }
        WindowWidthSizeClass.Expanded -> {
            // Large tablet (3 colunas)
            TaskListWide()
        }
    }
}
```

---

### ⏳ [PRÓXIMA] 22. Localização multi-idioma (PT/EN/ES)
**Prioridade**: BAIXA | **Esforço**: 3 dias | **Alcance**: INTERNACIONAL

**Adicionar string resources**:
```xml
<!-- res/values/strings.xml (PT) -->
<string name="app_name">TakStud</string>
<string name="manage_students">Gerenciar Alunos</string>

<!-- res/values-en/strings.xml (EN) -->
<string name="app_name">TakStud</string>
<string name="manage_students">Manage Students</string>

<!-- res/values-es/strings.xml (ES) -->
<string name="app_name">TakStud</string>
<string name="manage_students">Gestionar Estudiantes</string>
```

---

### ⏳ [PRÓXIMA] 23. Implementar dark mode com Material You
**Prioridade**: MÉDIA | **Esforço**: 2 dias | **VISUAL**: +150%

**Detectar tema do sistema**:
```kotlin
val isDarkMode = isSystemInDarkTheme()

TakStudTheme(darkTheme = isDarkMode) {
    // App content
}
```

**Material You colors**:
```kotlin
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF371E55),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF003735)
)
```

---

### ⏳ [PRÓXIMA] 24. Mensagens de erro específicas e construtivas
**Prioridade**: MÉDIA | **Esforço**: 1 dia | **UX**: +200%

**Exemplo ruim** ❌:
```
"Erro ao fazer chamada"
```

**Exemplo bom** ✅:
```
"Falha ao sincronizar frequência. Verifique sua conexão de internet e tente novamente."
```

**Implementação**:
```kotlin
val errorMessages = mapOf(
    "NETWORK_ERROR" to "Sem conexão. Verifique seu internet.",
    "INVALID_DATE" to "Data inválida. Use formato dd/MM/yyyy.",
    "DUPLICATE_ATTENDANCE" to "Presença já registrada para esta data.",
    "INVALID_GRADE" to "Nota deve estar entre 0 e 100."
)
```

---

### ⏳ [PRÓXIMA] 25. Adicionar animações de transição
**Prioridade**: BAIXA | **Esforço**: 1 dia | **POLISH**: +100%

**Exemplo**:
```kotlin
composable(
    TakStudDestinations.TEACHER_ROUTE,
    enterTransition = {
        slideInHorizontally(initialOffsetX = { 1000 }) +
        fadeIn(animationSpec = tween(500))
    },
    exitTransition = {
        slideOutHorizontally(targetOffsetX = { -1000 }) +
        fadeOut(animationSpec = tween(500))
    }
) {
    TeacherScreen()
}
```

---

## SEMANA 6-8: DADOS & OTIMIZAÇÃO (5 melhorias)

### ⏳ [PRÓXIMA] 26. Paginação com Paging 3
**Prioridade**: ALTA | **Esforço**: 2 dias | **PERFORMANCE**: +500%

**Problema**: Carrega 10000+ registros na memória

**Solução**:
```kotlin
fun getTasksPaged(): Flow<PagingData<Task>> {
    return Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true,
            maxSize = 100
        ),
        pagingSourceFactory = {
            FirestorePagingSource(
                db.collection("tasks")
                    .whereEqualTo("studentClass", studentClass)
            )
        }
    ).flow
}

// Em Composable
val pagingItems = tasksPagingFlow.collectAsLazyPagingItems()

LazyColumn {
    items(pagingItems) { task ->
        TaskItem(task)
    }
}
```

---

### ⏳ [PRÓXIMA] 27. Índices compostos e cascade deletes
**Prioridade**: ALTA | **Esforço**: 1 dia | **INTEGRIDADE**: CRÍTICA

**Entity com índices**:
```kotlin
@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["studentId", "date"], unique = true),  // Evita duplicatas
        Index("date"),  // Para queries por data
        Index("studentId", "date")  // Composto otimizado
    ],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE  // Auto-delete ao deletar student
        )
    ]
)
data class AttendanceEntity(...)
```

---

### ⏳ [PRÓXIMA] 28. Compilar e testar todas as mudanças (FINAL)
**Prioridade**: CRÍTICA | **Esforço**: 2 dias | **QUALIDADE**: 100%

**Checklist Final**:
- [ ] `./gradlew clean assemble Debug` - BUILD SUCCESSFUL
- [ ] `./gradlew test` - Todos os testes passam
- [ ] `./gradlew detekt` - Sem warnings de análise estática
- [ ] Nenhum crash ao usar app
- [ ] Performance aceitável (< 5s para qualquer operação)
- [ ] UI renderiza corretamente em phone & tablet
- [ ] Dark mode funciona
- [ ] Offline mode funciona
- [ ] Notificações FCM funcionam
- [ ] Tudo compila com API 29+

---

### ⏳ [PRÓXIMA] 29. Performance optimization & security hardening
**Prioridade**: MÉDIA | **Esforço**: 1 semana | **POLIMENTO**: FINAL

**Otimizações**:
- ProGuard/R8 minification
- Bitmap/image caching
- Memory leak detection
- Detekt/Lint zero violations
- Security policy hardening

---

### ⏳ [PRÓXIMA] 30. Documentação Final & Deploy
**Prioridade**: ALTA | **Esforço**: 2 dias | **MANUTENÇÃO**: FUTURA

**Documentos finais**:
- [ ] README.md completo
- [ ] ARCHITECTURE.md com diagrama
- [ ] API documentation
- [ ] Security checklist
- [ ] Deployment guide

---

## 📊 DISTRIBUIÇÃO DE ESFORÇO

```
Segurança            ████░░░░░░  30% (5 tasks)
Dados & Sync         ██░░░░░░░░  20% (6 tasks)
Testes & Docs        ██░░░░░░░░  10% (3 tasks)
Features             ██░░░░░░░░  15% (4 tasks)
UI/UX                ███░░░░░░░  20% (7 tasks)
Otimização           ░░░░░░░░░░   5% (5 tasks)
```

---

## ⏱️ TIMELINE ESTIMADO

| Fase | Semanas | Tarefas | Status |
|------|---------|---------|--------|
| 1-2  | 2       | 5       | ✅ CONCLUÍDA |
| 2-3  | 2       | 6       | ⏳ PRÓXIMA |
| 3-4  | 2       | 3       | ⏳ FUTURA |
| 4-5  | 2       | 4       | ⏳ FUTURA |
| 5-6  | 2       | 7       | ⏳ FUTURA |
| 6-8  | 2       | 5       | ⏳ FUTURA |

**Total**: ~12 semanas para 100%

---

## 🎯 METAS POR MÊS

**Novembro (Semana 1-2)**: ✅ CUMPRIDO
- [x] 5 melhorias de segurança

**Dezembro (Semana 3-6)**: 🎯 ALVO
- [ ] 6 melhorias de dados/sync
- [ ] 3 melhorias de testes
- [ ] 4 features novas

**Janeiro (Semana 7-8)**: 🎯 ALVO
- [ ] 12 melhorias de UI/UX e otimização
- [ ] Documentação final
- [ ] Deploy para produção

---

**Última atualização**: 12/11/2025 17:45
**Progresso Global**: ████░░░░░░ 17% (5/30)
**Status Próxima Fase**: ✅ Pronta para começar
