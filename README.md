# TakStud — Sistema de Gestão Educacional Android

Aplicativo Android nativo para gestão escolar com suporte a professores, alunos e responsáveis. Oferece controle de horários, tarefas, frequência, notas e comunicados com sincronização em tempo real via Firebase.

---

## Visão Geral

O TakStud é um sistema offline-first: todas as operações são realizadas primeiro no banco local (Room/SQLite) e sincronizadas com o Firebase Firestore em background. Isso garante funcionamento sem internet e consistência eventual de dados.

---

## Stack Técnica

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
| Paginação | Paging 3 |
| Preferências | DataStore |
| Segurança | EncryptedSharedPreferences (androidx.security) |
| Qualidade | Detekt 1.23.1 |
| Logging | Timber 5.0.1 |
| Serialization | Gson 2.10.1 + kotlinx-serialization 1.6.0 |
| Min SDK | 29 (Android 10) |
| Target SDK | 36 |

---

## Arquitetura

O projeto segue Clean Architecture com separação em três camadas principais:

```
app/src/main/java/com/example/takstud/
│
├── model/                          # Entidades de domínio
│   ├── Class.kt                    # Turma escolar (código, alunos, professor)
│   ├── Student.kt                  # Aluno (nome, matrícula, turma)
│   ├── Task.kt                     # Tarefa/atividade
│   ├── Notice.kt                   # Comunicado
│   ├── Notification.kt             # Notificação interna
│   ├── Period.kt                   # Período letivo
│   ├── Role.kt                     # Papel do usuário (TEACHER, STUDENT, PARENT, ADMIN)
│   ├── Permission.kt               # Permissões por role
│   ├── UserSession.kt              # Sessão autenticada
│   ├── EventCalendar.kt            # Evento no calendário
│   ├── schedule/
│   │   ├── ClassSchedule.kt        # Grade horária de uma turma
│   │   ├── Subject.kt              # Disciplina
│   │   ├── TimeSlot.kt             # Slot de horário (dia, hora início/fim)
│   │   └── ScheduleConflict.kt     # Conflito de horário detectado
│   ├── student/
│   │   └── StudentExtended.kt      # Aluno com notas, frequência e estatísticas
│   └── task/
│       └── TaskExtended.kt         # Tarefa com submissions e status
│
├── data/                           # Camada de dados
│   ├── local/
│   │   ├── AppDatabase.kt          # Room Database — configura todas as entidades e DAOs
│   │   ├── entity/
│   │   │   ├── Entities.kt         # Entidades Room principais
│   │   │   ├── ScheduleEntities.kt # Entidades de horário
│   │   │   ├── StudentEntities.kt  # Entidades de aluno/nota/frequência
│   │   │   ├── TaskEntities.kt     # Entidades de tarefas e submissões
│   │   │   ├── EventEntity.kt      # Entidade de eventos
│   │   │   └── NotificationEntity.kt
│   │   ├── dao/                    # 16 DAOs com operações CRUD + Flow
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
│   │   │   ├── SyncQueueDao.kt     # Fila de operações pendentes para sync
│   │   │   ├── TaskDao.kt
│   │   │   └── TimeSlotDao.kt
│   │   └── converters/
│   │       └── StringListConverter.kt  # TypeConverter Room: List<String> <-> JSON
│   ├── remote/
│   │   ├── FirebaseScheduleService.kt  # CRUD de horários no Firestore
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
│   │   ├── LoginScreen.kt          # Seleção de perfil
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
│   ├── parent/                     # Telas do responsável
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
│   ├── components/                 # Componentes reutilizáveis
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
│   │   └── ThemePreferences.kt     # Persistência da preferência de tema
│   ├── AuthGuard.kt                # Composable que protege rotas por role
│   ├── HomeScreen.kt
│   └── NotificationScreen.kt
│
├── di/                             # Módulos de injeção de dependência (Hilt)
│   ├── AppModule.kt                # Provê Firebase, repositórios, etc.
│   └── DatabaseModule.kt          # Provê AppDatabase e todos os DAOs
│
├── security/
│   ├── SecureSessionManager.kt     # Gerencia sessão com EncryptedSharedPreferences
│   ├── AccessValidator.kt          # Valida permissões por role
│   └── LoginRateLimiter.kt         # Limita tentativas de login
│
├── service/
│   └── TakStudMessagingService.kt  # FCM — recebe e processa push notifications
│
├── notifications/
│   └── NotificationHelper.kt       # Cria e exibe notificações locais
│
├── util/
│   ├── Result.kt                   # Sealed class: Success / Error / Loading
│   ├── SessionManager.kt           # Estado de sessão em memória
│   ├── SessionStorage.kt           # Persistência de sessão
│   ├── ErrorHandler.kt             # Tratamento centralizado de erros
│   ├── InputValidator.kt           # Validação de formulários
│   ├── FirestoreFlowHelper.kt      # Converte snapshots Firestore em Flow
│   └── NotificationManager.kt      # Utilitários de notificação
│
├── TakStudApplication.kt           # Application — inicializa Hilt e Timber
├── MainActivity.kt                 # Entry point — NavHost Compose
├── TakStudNavGraph.kt              # Grafo de navegação completo
└── StudentAuthRepository.kt        # Autenticação de alunos via código de turma
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
    └─→ Remote (Firestore)    ← sincronização em background
```

**Offline-first**: leituras sempre vêm do Room. Escritas vão para o Room imediatamente e para o Firestore de forma assíncrona. Operações que falham ficam na `SyncQueue` e são reenviadas pelo WorkManager.

---

## Módulo de Segurança

- **`SecureSessionManager`**: armazena token de sessão em `EncryptedSharedPreferences` (AES-256)
- **`LoginRateLimiter`**: bloqueia tentativas após N falhas consecutivas
- **`AccessValidator`**: verifica permissões antes de executar operações críticas
- **`AuthGuard`**: composable que redireciona para login caso a sessão seja inválida

---

## Papéis de Usuário (Roles)

| Role | Acesso |
|---|---|
| `TEACHER` | Dashboard, turmas, alunos, tarefas, frequência, horários, comunicados, analytics |
| `PARENT` | Tarefas, comunicados, horários e analytics do filho selecionado |
| `STUDENT` | Submissão de tarefas |
| `ADMIN` | Dashboard administrativo com visão geral |

---

## Banco de Dados Local (Room)

**16 DAOs** cobrindo as entidades:

- `ClassSchedule`, `TimeSlot`, `Subject` — horários
- `Student`, `StudentStats`, `StudentTimeline` — alunos
- `Grade`, `StudentGrade` — notas
- `Attendance` — frequência
- `Task` — tarefas
- `Notice` — comunicados
- `Notification` — notificações internas
- `Event` — eventos do calendário
- `SyncQueue` — fila de sincronização

---

## Firebase

| Serviço | Uso |
|---|---|
| Firestore | Banco principal remoto (turmas, alunos, tarefas, horários) |
| Cloud Messaging | Push notifications para professores e responsáveis |
| Analytics | Rastreamento de eventos de uso |
| Storage | Armazenamento de arquivos anexados |
| Remote Config | Configurações remotas de feature flags |

**Regras de segurança**: definidas em `firestore.rules` — acesso autenticado por role com validação server-side.

---

## Configuração para Rodar

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
│   │   ├── java/com/example/takstud/   # Código-fonte (descrito acima)
│   │   ├── res/                         # Resources Android
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml              # Catálogo de versões (Gradle Version Catalog)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── detekt.yml                          # Regras de análise estática
├── firestore.rules                     # Regras de segurança Firestore
├── takstud_logo.svg
└── web-demo/                           # Demo web da interface (HTML/CSS/JS)
```
