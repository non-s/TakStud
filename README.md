# TakStud — Sistema de Gestao Educacional Android

Aplicativo Android nativo para gestao escolar com suporte a professores, alunos e responsaveis. Oferece controle de horarios, tarefas, frequencia, notas e comunicados com sincronizacao em tempo real via Firebase.

---

## Visao Geral

O TakStud e um sistema offline-first: todas as operacoes sao realizadas primeiro no banco local (Room/SQLite) e sincronizadas com o Firebase Firestore em background. Isso garante funcionamento sem internet e consistencia eventual de dados.

---

## Stack Tecnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.0.0 |
| UI | Jetpack Compose + Material Design 3 |
| Arquitetura | MVVM + Clean Architecture |
| DI | Hilt (Dagger) |
| Banco local | Room 2.6.1 (SQLite) |
| Banco remoto | Firebase Firestore |
| Push | Firebase Cloud Messaging (FCM) |
| Background | WorkManager 2.9.0 |
| Async | Kotlin Coroutines + Flow |
| Paginacao | Paging 3 |
| Preferencias | DataStore |
| Seguranca | EncryptedSharedPreferences (androidx.security) |
| Qualidade | Detekt 1.23.1 |
| Logging | Timber 5.0.1 |
| Serialization | Gson 2.10.1 + kotlinx-serialization 1.6.0 |
| Min SDK | 29 (Android 10) |
| Target SDK | 36 |

---

## Arquitetura

O projeto segue Clean Architecture com separacao em tres camadas principais:

```
app/src/main/java/com/example/takstud/
│
├── model/                          # Entidades de dominio
│   ├── Class.kt                    # Turma escolar (codigo, alunos, professor)
│   ├── Student.kt                  # Aluno (nome, matricula, turma)
│   ├── Task.kt                     # Tarefa/atividade
│   ├── Notice.kt                   # Comunicado
│   ├── Notification.kt             # Notificacao interna
│   ├── Period.kt                   # Periodo letivo
│   ├── Role.kt                     # Papel do usuario (TEACHER, STUDENT, PARENT, ADMIN)
│   ├── Permission.kt               # Permissoes por role
│   ├── UserSession.kt              # Sessao autenticada
│   ├── EventCalendar.kt            # Evento no calendario
│   ├── schedule/
│   │   ├── ClassSchedule.kt        # Grade horaria de uma turma
│   │   ├── Subject.kt              # Disciplina
│   │   ├── TimeSlot.kt             # Slot de horario (dia, hora inicio/fim)
│   │   └── ScheduleConflict.kt     # Conflito de horario detectado
│   ├── student/
│   │   └── StudentExtended.kt      # Aluno com notas, frequencia e estatisticas
│   └── task/
│       └── TaskExtended.kt         # Tarefa com submissions e status
│
├── data/                           # Camada de dados
│   ├── local/
│   │   ├── AppDatabase.kt          # Room Database — configura todas as entidades e DAOs
│   │   ├── entity/
│   │   │   ├── Entities.kt         # Entidades Room principais
│   │   │   ├── ScheduleEntities.kt # Entidades de horario
│   │   │   ├── StudentEntities.kt  # Entidades de aluno/nota/frequencia
│   │   │   ├── TaskEntities.kt     # Entidades de tarefas e submissoes
│   │   │   ├── EventEntity.kt      # Entidade de eventos
│   │   │   └── NotificationEntity.kt
│   │   ├── dao/                    # 16 DAOs com operacoes CRUD + Flow
│   │   │   ├── AttendanceDao.kt
│   │   │   ├── ClassScheduleDao.kt
│   │   │   ├── EventDao.kt
│   │   │   ├── GradeDao.kt
│   │   │   ├── NoticeDao.kt
│   │   │   ├── NotificationDao.kt
│   │   │   ├── ScheduleDao.kt
│   │   │   ├── StudentDao.kt
│   │   │   ├── StudentGradeDao.kt
│   │   │   ├── StudentStatsDao.kt
│   │   │   ├── StudentTimelineDao.kt
│   │   │   ├── SubjectDao.kt
│   │   │   ├── SyncQueueDao.kt     # Fila de operacoes pendentes para sync
│   │   │   ├── TaskDao.kt
│   │   │   └── TimeSlotDao.kt
│   │   └── converters/
│   │       └── StringListConverter.kt  # TypeConverter Room: List<String> <-> JSON
│   ├── remote/
│   │   ├── FirebaseScheduleService.kt  # CRUD de horarios no Firestore
│   │   └── FirebaseStudentService.kt   # CRUD de alunos no Firestore
│   ├── repository/                 # Repository pattern — orquestra local + remoto
│   │   ├── ClassRepository.kt
│   │   ├── ScheduleRepository.kt
│   │   ├── StudentRepository.kt
│   │   ├── TaskRepository.kt
│   │   ├── NoticeRepository.kt
│   │   ├── NotificationRepository.kt
│   │   ├── EventRepository.kt
│   │   └── AuditRepository.kt
│   └── FirestorePagingSource.kt    # PagingSource para queries paginadas no Firestore
│
├── viewmodel/                      # ViewModels (injetados via Hilt)
│   ├── AuthViewModel.kt
│   ├── LoginViewModel.kt
│   ├── TaskViewModel.kt
│   ├── ScheduleViewModel.kt
│   ├── StudentManagementViewModel.kt
│   ├── NoticeViewModel.kt
│   ├── NotificationViewModel.kt
│   ├── ParentViewModel.kt
│   ├── CalendarViewModel.kt
│   ├── ThemeViewModel.kt
│   └── ExpandedNotificationViewModel.kt
│
├── ui/                             # Telas Compose
│   ├── login/
│   │   ├── LoginScreen.kt          # Selecao de perfil
│   │   ├── TeacherLoginScreen.kt
│   │   ├── ParentLoginScreen.kt
│   │   └── AdminLoginScreen.kt
│   ├── teacher/                    # Telas do professor
│   │   ├── TeacherScreen.kt        # Container com bottom navigation
│   │   ├── TeacherDashboardScreen.kt
│   │   ├── ManageClassesScreen.kt
│   │   ├── ManageStudentsScreen.kt
│   │   ├── RegisterStudentScreen.kt
│   │   ├── AttendanceScreen.kt
│   │   ├── TaskListScreen.kt
│   │   ├── AddTaskScreen.kt
│   │   ├── NoticeListScreen.kt
│   │   ├── AddNoticeScreen.kt
│   │   ├── SchedulesListScreen.kt
│   │   ├── ManageScheduleScreen.kt
│   │   ├── AnalyticsScreen.kt
│   │   ├── schedule/
│   │   │   ├── ScheduleManagementScreen.kt
│   │   │   └── SubjectManagementScreen.kt
│   │   └── task/
│   │       └── RichTaskEditorScreen.kt
│   ├── parent/                     # Telas do responsavel
│   │   ├── ParentScreen.kt
│   │   ├── StudentSelectionScreen.kt
│   │   ├── ParentTaskListScreen.kt
│   │   ├── ParentNoticeListScreen.kt
│   │   ├── ParentScheduleListScreen.kt
│   │   └── ParentAnalyticsScreen.kt
│   ├── student/
│   │   └── task/
│   │       └── StudentTaskSubmissionScreen.kt
│   ├── admin/
│   │   └── AdminDashboardScreen.kt
│   ├── calendar/
│   │   ├── CalendarScreen.kt
│   │   └── WeeklyCalendarView.kt
│   ├── components/                 # Componentes reutilizaveis
│   │   ├── Charts.kt
│   │   ├── FormComponents.kt
│   │   ├── LoadingStates.kt
│   │   ├── EmptyStates.kt
│   │   ├── ErrorStates.kt
│   │   ├── DashboardComponents.kt
│   │   ├── NeonComponents.kt
│   │   ├── PremiumCards.kt
│   │   ├── SnackbarManager.kt
│   │   ├── SearchAndFilters.kt
│   │   └── schedule/
│   │       ├── WeeklyScheduleGrid.kt
│   │       └── TimeSlotDialog.kt
│   ├── theme/
│   │   ├── Theme.kt                # MaterialTheme com suporte dark/light
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   ├── Shape.kt
│   │   ├── ThemeCustomization.kt
│   │   └── ThemePreferences.kt     # Persistencia da preferencia de tema
│   ├── AuthGuard.kt                # Composable que protege rotas por role
│   ├── HomeScreen.kt
│   └── NotificationScreen.kt
│
├── di/                             # Modulos de injecao de dependencia (Hilt)
│   ├── AppModule.kt                # Provê Firebase, repositorios, etc.
│   └── DatabaseModule.kt          # Provê AppDatabase e todos os DAOs
│
├── security/
│   ├── SecureSessionManager.kt     # Gerencia sessao com EncryptedSharedPreferences
│   ├── AccessValidator.kt          # Valida permissoes por role
│   └── LoginRateLimiter.kt         # Limita tentativas de login
│
├── service/
│   └── TakStudMessagingService.kt  # FCM — recebe e processa push notifications
│
├── notifications/
│   └── NotificationHelper.kt       # Cria e exibe notificacoes locais
│
├── util/
│   ├── Result.kt                   # Sealed class: Success / Error / Loading
│   ├── SessionManager.kt           # Estado de sessao em memoria
│   ├── SessionStorage.kt           # Persistencia de sessao
│   ├── ErrorHandler.kt             # Tratamento centralizado de erros
│   ├── InputValidator.kt           # Validacao de formularios
│   ├── FirestoreFlowHelper.kt      # Converte snapshots Firestore em Flow
│   └── NotificationManager.kt      # Utilitarios de notificacao
│
├── TakStudApplication.kt           # Application — inicializa Hilt e Timber
├── MainActivity.kt                 # Entry point — NavHost Compose
├── TakStudNavGraph.kt              # Grafo de navegacao completo
└── StudentAuthRepository.kt        # Autenticacao de alunos via codigo de turma
```

---

## Fluxo de Dados

```
UI (Composable)
    ↕  collectAsState / LaunchedEffect
ViewModel
    ↕  suspend fun / Flow
Repository
    ├─→ Local (Room DAO)      ← fonte de verdade
    └─→ Remote (Firestore)    ← sincronizacao em background
```

**Offline-first**: leituras sempre vem do Room. Escritas vao para o Room imediatamente e para o Firestore de forma assíncrona. Operacoes que falham ficam na `SyncQueue` e sao reenviadas pelo WorkManager.

---

## Modulo de Seguranca

- **`SecureSessionManager`**: armazena token de sessao em `EncryptedSharedPreferences` (AES-256)
- **`LoginRateLimiter`**: bloqueia tentativas apos N falhas consecutivas
- **`AccessValidator`**: verifica permissoes antes de executar operacoes criticas
- **`AuthGuard`**: composable que redireciona para login caso a sessao seja invalida

---

## Papeis de Usuario (Roles)

| Role | Acesso |
|---|---|
| `TEACHER` | Dashboard, turmas, alunos, tarefas, frequencia, horarios, comunicados, analytics |
| `PARENT` | Tarefas, comunicados, horarios e analytics do filho selecionado |
| `STUDENT` | Submissao de tarefas |
| `ADMIN` | Dashboard administrativo com visao geral |

---

## Banco de Dados Local (Room)

**16 DAOs** cobrindo as entidades:

- `ClassSchedule`, `TimeSlot`, `Subject` — horarios
- `Student`, `StudentStats`, `StudentTimeline` — alunos
- `Grade`, `StudentGrade` — notas
- `Attendance` — frequencia
- `Task` — tarefas
- `Notice` — comunicados
- `Notification` — notificacoes internas
- `Event` — eventos do calendario
- `SyncQueue` — fila de sincronizacao

---

## Firebase

| Servico | Uso |
|---|---|
| Firestore | Banco principal remoto (turmas, alunos, tarefas, horarios) |
| Cloud Messaging | Push notifications para professores e responsaveis |
| Analytics | Rastreamento de eventos de uso |
| Storage | Armazenamento de arquivos anexados |
| Remote Config | Configuracoes remotas de feature flags |

**Regras de seguranca**: definidas em `firestore.rules` — acesso autenticado por role com validacao server-side.

---

## Configuracao para Rodar

1. Crie um projeto no [Firebase Console](https://console.firebase.google.com)
2. Adicione um app Android com package `com.example.takstud`
3. Baixe o `google-services.json` e coloque em `app/`
4. Configure as regras do Firestore usando `firestore.rules`
5. Abra no Android Studio e execute em um dispositivo Android 10+

```bash
# Build release
./gradlew assembleRelease

# Rodar lint/detekt
./gradlew detekt
```

---

## Estrutura de Arquivos Raiz

```
TakStud/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/takstud/   # Codigo-fonte (descrito acima)
│   │   ├── res/                         # Resources Android
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml              # Catalogo de versoes (Gradle Version Catalog)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── detekt.yml                          # Regras de analise estatica
├── firestore.rules                     # Regras de seguranca Firestore
├── takstud_logo.svg
└── web-demo/                           # Demo web da interface (HTML/CSS/JS)
```
