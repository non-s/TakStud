# 📋 PLANO DETALHADO: ITEMS 13-30 DO ROADMAP

**Data**: 14/11/2025
**Status Atual**: Items 8-12 completados (40/30 items)
**Próximo**: Item 13 (Documentação KDoc)
**Target**: 70%+ coverage mantido, 18 items finais

---

## 📊 RESUMO DOS 18 ITEMS RESTANTES

```
Items 13-18: DOCUMENTAÇÃO & FEATURES (4 items)
├─ Item 13: KDoc documentation
├─ Item 14: UiState pattern
├─ Item 15: Relatórios de frequência
└─ Item 16: Notificações FCM

Items 17-25: UI/UX (9 items)
├─ Item 17: Busca e filtros
├─ Item 18: Períodos flexíveis
├─ Item 19: Acessibilidade WCAG
├─ Item 20: Remover emoji
├─ Item 21: Layouts responsivos tablet
├─ Item 22: Multi-idioma (PT/EN/ES)
├─ Item 23: Dark mode
├─ Item 24: Mensagens de erro específicas
└─ Item 25: Animações de transição

Items 26-30: OTIMIZAÇÃO & DEPLOY (5 items)
├─ Item 26: Paginação com Paging 3
├─ Item 27: Índices compostos e cascade deletes
├─ Item 28: Compilar e testar final
├─ Item 29: Performance optimization & security
└─ Item 30: Documentação final & deploy
```

---

## 🎯 ITEM 13: ADICIONAR DOCUMENTAÇÃO KDOC COMPLETA

**Prioridade**: MÉDIA
**Esforço**: 1 semana (~40 horas)
**Impacto**: Manutenção +200%

### Arquivos a Documentar (20+ arquivos)

#### Críticos:
- `TakStudRepository.kt` - 20+ funções
- `TakStudViewModel.kt` - 15+ funções
- `TakStudRepositoryExtensions.kt` - 10+ funções
- `TakStudViewModel.kt` - 15+ funções
- `SyncWorker.kt` - 5+ funções

#### Novos (security/util):
- `LoginRateLimiter.kt` - 8+ funções
- `SecureSessionManager.kt` - 10+ funções
- `AdvancedValidator.kt` - 12+ funções
- `ErrorHandler.kt` - 10+ funções
- `FirestoreFlowHelper.kt` - 3+ funções

#### Modelos & Entidades:
- `AttendanceRecord.kt` - 5+ proprietários
- `Task.kt` - 8+ proprietários
- `Grade.kt` - 10+ proprietários
- Todas as Entities do Room

### Padrão KDoc a Usar

```kotlin
/**
 * Breve descrição de uma linha do que a função faz.
 *
 * Descrição longa (parágrafo) explicando:
 * - O que faz
 * - Quando usar
 * - Casos especiais
 * - Comportamento sob erro
 *
 * @param paramName Descrição do parâmetro
 * @return Descrição do retorno
 *
 * @throws ExceptionType Quando e por que é lançada
 *
 * Exemplo:
 * ```kotlin
 * val result = myFunction(param)
 * // Resultado esperado
 * ```
 *
 * @see RelatedFunction
 * @see RelatedClass
 */
fun myFunction(paramName: String): Result { ... }
```

### Checklist

- [ ] Documentar TakStudRepository (20+ funções)
- [ ] Documentar TakStudViewModel (15+ funções)
- [ ] Documentar todas as classes de security
- [ ] Documentar AdvancedValidator (12+)
- [ ] Documentar ErrorHandler (10+)
- [ ] Documentar FirestoreFlowHelper (3+)
- [ ] Documentar modelos/entidades
- [ ] Gerar Dokka documentation

**Estimado**: 40 horas (~1 semana)

---

## 🎯 ITEM 14: IMPLEMENTAR UISTATE PARA LOADING/ERROR

**Prioridade**: MÉDIA
**Esforço**: 3 dias (~24 horas)
**Impacto**: UX +200%

### UiState Sealed Class

```kotlin
sealed class UiState<T> {
    class Loading<T> : UiState<T>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val message: String, val exception: Exception? = null) : UiState<T>()

    fun getOrNull(): T? = (this as? Success)?.data
    fun getError(): String? = (this as? Error)?.message
}
```

### ViewModels a Atualizar

- `TakStudViewModel.kt`
- `TaskListViewModel.kt` (novo ou existente)
- `StudentManagementViewModel.kt`
- `GradeManagementViewModel.kt`
- `AttendanceViewModel.kt`

### Padrão de Uso

```kotlin
class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<Task>>>(UiState.Loading())
    val state: StateFlow<UiState<List<Task>>> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = UiState.Loading()
                val data = repository.loadTasks()
                _state.value = UiState.Success(data)
            } catch (e: Exception) {
                _state.value = UiState.Error(
                    message = ErrorHandler.getUserFriendlyMessage(e),
                    exception = e
                )
            }
        }
    }
}

// Em Composable
@Composable
fun MyScreen(viewModel: MyViewModel) {
    when (val state = viewModel.state.collectAsState().value) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> SuccessContent(state.data)
        is UiState.Error -> ErrorContent(state.message)
    }
}
```

### Checklist

- [ ] Criar UiState sealed class
- [ ] Atualizar TakStudViewModel
- [ ] Criar TaskListViewModel com UiState
- [ ] Criar StudentManagementViewModel
- [ ] Criar GradeManagementViewModel
- [ ] Atualizar todas as Composables
- [ ] Testar loading/error states

**Estimado**: 24 horas (~3 dias)

---

## 🎯 ITEM 15: CRIAR RELATÓRIOS DE FREQUÊNCIA

**Prioridade**: MÉDIA
**Esforço**: 4 dias (~32 horas)
**Impacto**: Relatórios +300%

### Novas Classes

```kotlin
data class AttendanceReport(
    val id: String,
    val studentId: String,
    val studentName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalClasses: Int,
    val presentDays: Int,
    val absentDays: Int,
    val percentage: Double
) {
    val status: ReportStatus
        get() = when {
            percentage >= 90.0 -> ReportStatus.EXCELLENT
            percentage >= 75.0 -> ReportStatus.GOOD
            percentage >= 70.0 -> ReportStatus.ATTENTION
            else -> ReportStatus.CRITICAL
        }
}

enum class ReportStatus {
    EXCELLENT, GOOD, ATTENTION, CRITICAL
}

data class ClassAttendanceReport(
    val classId: String,
    val className: String,
    val period: String,
    val totalStudents: Int,
    val studentReports: List<AttendanceReport>,
    val averageAttendance: Double,
    val studentsAtRisk: Int,
    val studentsCritical: Int
)
```

### Novas Funções no Repository

```kotlin
suspend fun getAttendanceReport(
    studentId: String,
    startDate: LocalDate,
    endDate: LocalDate
): AttendanceReport

suspend fun getClassAttendanceReport(
    classId: String,
    startDate: LocalDate,
    endDate: LocalDate
): ClassAttendanceReport

suspend fun exportReportToCSV(
    report: ClassAttendanceReport
): String // CSV content
```

### Nova Tela: ReportsScreen.kt

- Seleção de estudante/turma
- Seleção de período (data inicial/final)
- Exibição de relatório
- Gráficos de presença (pie chart, bar chart)
- Exportar para CSV

### Checklist

- [ ] Criar classes AttendanceReport, ClassAttendanceReport
- [ ] Implementar funções no Repository
- [ ] Criar ReportsScreen.kt
- [ ] Adicionar gráficos (usando Charts library)
- [ ] Implementar exportação CSV
- [ ] Adicionar testes (15+)
- [ ] Integrar na navegação

**Estimado**: 32 horas (~4 dias)

---

## 🎯 ITEM 16: IMPLEMENTAR NOTIFICAÇÕES FCM

**Prioridade**: MÉDIA
**Esforço**: 3 dias (~24 horas)
**Impacto**: Comunicação +300%

### Setup Firebase Cloud Messaging

1. Adicionar dependência
```gradle
implementation "com.google.firebase:firebase-messaging:23.4.1"
```

2. Criar MessagingService

```kotlin
class TakStudMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"] ?: "TakStud"
        val body = message.data["body"] ?: ""
        val type = message.data["type"] ?: "general"

        showNotification(title, body, type)
    }

    override fun onNewToken(token: String) {
        // Enviar token para servidor
        saveFCMToken(token)
    }

    private fun showNotification(title: String, body: String, type: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("notification_type", type)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

### Eventos para Notificar

- ✅ Nova tarefa criada
- ✅ Aviso/recado do professor
- ✅ Frequência baixa (< 70%)
- ✅ Notas atualizadas
- ✅ Chamada de classe iniciada

### Implementar no Repository

```kotlin
// Salvar FCM token ao fazer login
suspend fun saveFCMToken(userId: String) {
    val token = FirebaseMessaging.getInstance().token.await()
    db.collection("users").document(userId).update(
        "fcmToken" to token,
        "fcmTokenUpdatedAt" to System.currentTimeMillis()
    ).await()
}

// Função para enviar notificação (server-side ou Cloud Function)
suspend fun notifyParentAboutAttendance(parentId: String, studentName: String) {
    // Cloud Function vai enviar via FCM
    db.collection("notifications").add(
        mapOf(
            "recipient" to parentId,
            "type" to "attendance_low",
            "studentName" to studentName,
            "createdAt" to System.currentTimeMillis(),
            "sent" to false
        )
    ).await()
}
```

### Checklist

- [ ] Adicionar dependência FCM
- [ ] Criar TakStudMessagingService
- [ ] Configurar canal de notificações
- [ ] Implementar saveFCMToken
- [ ] Integrar notification handler
- [ ] Testar envio de notificações
- [ ] Adicionar testes (10+)

**Estimado**: 24 horas (~3 dias)

---

## ⏱️ CRONOGRAMA SUGERIDO

```
Week 1 (Nov 15-21):     Item 13 (KDoc Documentation)
Week 2 (Nov 22-28):     Item 14 (UiState) + Item 15 (Reports)
Week 3 (Nov 29-Dec 5):  Item 16 (FCM Notifications) + Item 17 (Search)
Week 4 (Dec 6-12):      Item 18-19 (Períodos + Accessibility)
Week 5 (Dec 13-19):     Item 20-22 (Icons + Tablet + i18n)
Week 6 (Dec 20-26):     Item 23-24 (Dark Mode + Error Messages)
Week 7 (Dec 27-Jan 2):  Item 25-26 (Animations + Pagination)
Week 8 (Jan 3-9):       Item 27-28 (Database Indices + Testing)
Week 9 (Jan 10-16):     Item 29-30 (Performance + Documentation)
```

---

## 📈 MÉTRICAS DE SUCESSO

Para cada item, validar:
- ✅ Implementação completa
- ✅ Testes > 80% coverage
- ✅ Build sem erros
- ✅ Sem warnings estáticos
- ✅ Performance aceitável
- ✅ Funcionamento em produção

---

## 🎯 METAS POR CATEGORIA

### Documentação
- [ ] 100% KDoc para classes principais
- [ ] Dokka documentation gerada
- [ ] README.md completo
- [ ] Architecture.md com diagramas

### Qualidade
- [ ] 70%+ test coverage mantido
- [ ] Zero warnings de Detekt
- [ ] Zero Lint violations
- [ ] Build reproducível

### Features
- [ ] Relatórios de frequência
- [ ] Notificações FCM
- [ ] Busca e filtros
- [ ] Dark mode + responsivo

### Performance
- [ ] Paginação para grandes listas
- [ ] Índices otimizados no Room
- [ ] Caching eficiente
- [ ] Memory profiling OK

---

## 🚀 PRÓXIMOS PASSOS

1. **Começar Item 13** (KDoc Documentation)
   - Documentar módulos críticos
   - Tempo: 40 horas (~1 semana)

2. **Depois Item 14** (UiState Pattern)
   - Implementar em ViewModels
   - Tempo: 24 horas (~3 dias)

3. **Depois Item 15** (Relatórios)
   - Adicionar tela de reports
   - Tempo: 32 horas (~4 dias)

4. **Depois Item 16** (FCM)
   - Setup de notificações
   - Tempo: 24 horas (~3 dias)

Total para Items 13-16: ~14 dias (~2 semanas)

---

**Status**: Pronto para começar Item 13! 🚀

