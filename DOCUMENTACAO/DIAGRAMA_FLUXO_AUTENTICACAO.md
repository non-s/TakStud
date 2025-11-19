# 📊 Diagrama Completo do Fluxo de Autenticação

## Visão Geral do Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                      APLIKATIVO TAKSTUD                         │
└─────────────────────────────────────────────────────────────────┘
        │                                              │
        │                                              │
   ┌────▼───────┐                            ┌────────▼──────┐
   │ LoginScreen │                            │  MainScreen   │
   │ (Tela 1)    │                            │ (Tela 2+)     │
   └────┬────┬──┘                             └───────────────┘
        │    │
        │    │
┌───────▼─┐  │
│   PAI   │  │
│ (RA)    │  │
└────┬────┘  │
     │       │
     │       └─────────────┐
     │                     │
     │              ┌──────▼────────┐
     │              │  PROFESSOR    │
     │              │ (Google)      │
     │              └──────┬────────┘
     │                     │
     │                     │
     ▼                     ▼
┌────────────────────────────────────┐
│   FIREBASE AUTHENTICATION          │
│   (Autenticação)                   │
│                                    │
│  - Aluno: signInAnonymously()      │
│  - Professor: signInWithGoogle()   │
│                                    │
└────┬─────────────────────┬────────┘
     │                     │
     │                     │
     ▼                     ▼
┌──────────────────────────────────────┐
│   FIRESTORE DATABASE                 │
│   (Banco de dados)                   │
│                                      │
│  ┌─ collections/                     │
│  │  ├─ students/                     │
│  │  ├─ teachers/                     │
│  │  ├─ parents/                      │
│  │  ├─ tasks/                        │
│  │  ├─ notices/                      │
│  │  ├─ grades/                       │
│  │  ├─ attendance/                   │
│  │  └─ config/                       │
│  │     └─ teacherEmails             │
│  │        └─ emails: [...]          │
│                                      │
└──────────────────────────────────────┘
```

---

## Fluxo 1: Login do Responsável com RA

```
┌──────────────────────────────────────────────────────────────┐
│ FLUXO: Login Responsável com RA                              │
└──────────────────────────────────────────────────────────────┘

┌─────────────┐
│   INÍCIO    │
│  LoginScreen│
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ Responsável clica na aba "Responsável"  │
│ Digite RA: [123456]                     │
│ Clica em [ENTRAR]                       │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ LoginViewModel.loginParentWithRA()      │
│ Valida se RA não está vazio             │
│ Inicia loading = true                   │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ AuthRepository.loginParentWithRA(RA)    │
│                                         │
│ 1️⃣  Query Firestore:                    │
│   WHERE students.ra == "123456"         │
│   LIMIT 1                               │
└──────┬──────────────────────────────────┘
       │
       ├─ SIM ✅ Student encontrado
       │
       ▼
┌─────────────────────────────────────────┐
│ 2️⃣  Extrair dados do aluno:             │
│   - studentId = "abc123"                │
│   - name = "João Silva"                 │
│   - studentClass = "8A"                 │
│   - parent = "Maria Silva"              │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ 3️⃣  Firebase Auth Anonymous:            │
│   auth.signInAnonymously()              │
│   Cria UID único para sessão            │
│   uid = "xyz789"                        │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ 4️⃣  Criar documento no Firestore:       │
│   Caminho: parents/{uid}                │
│   Dados:                                │
│   {                                     │
│     uid: "xyz789"                       │
│     studentRa: "123456"                 │
│     studentId: "abc123"                 │
│     studentName: "João Silva"           │
│     studentClass: "8A"                  │
│     parentName: "Maria Silva"           │
│     role: "PARENT"                      │
│     createdAt: 1699564800               │
│   }                                     │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ Result.success(parentData)              │
│ viewModel.parentLoginSuccess = true     │
│ viewModel.parentLoginLoading = false    │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ LoginScreen observa:                    │
│ if (parentSuccess) {                    │
│   onNavigateToParent()                  │
│ }                                       │
└──────┬──────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────┐
│ 🎉 SUCESSO!                                              │
│ Navega para ParentScreen                                 │
│ Responsável agora vê:                                    │
│ - Horários do aluno                                      │
│ - Avisos da turma                                        │
│ - Tarefas e notas                                        │
│ - Frequência                                             │
└──────────────────────────────────────────────────────────┘
```

**Caminho Alternativo (RA NÃO encontrado):**

```
Firestore query retorna: snapshot.isEmpty == true
    ↓
Result.failure(Exception("RA não encontrado"))
    ↓
viewModel.parentLoginError = "❌ RA não encontrado"
    ↓
LoginScreen mostra mensagem de erro
    ↓
Responsável tenta novamente
```

---

## Fluxo 2: Login do Professor com Google

```
┌──────────────────────────────────────────────────────────────┐
│ FLUXO: Login Professor com Google                            │
└──────────────────────────────────────────────────────────────┘

┌─────────────┐
│   INÍCIO    │
│  LoginScreen│
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ Professor clica na aba "Professor"      │
│ Clica em [ENTRAR COM GOOGLE]            │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ LoginViewModel.getGoogleSignInIntent()  │
│ GoogleSignInHelper.getSignInIntent()    │
│ launcher.launch(intent)                 │
└──────┬──────────────────────────────────┘
       │
       ▼
┌───────────────────────────────────────────────────────────┐
│ 🌐 GOOGLE LOGIN SCREEN (externa)                          │
│                                                           │
│ Professor vê tela do Google com:                          │
│ - Lista de contas Google disponíveis                      │
│ - Ou opção para fazer login novo                          │
│                                                           │
│ Professor seleciona: professor@gmail.com                  │
└──────┬────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ Google gera:                            │
│ - idToken (token de autenticação)       │
│ - email: professor@gmail.com            │
│ - displayName: "Prof. João"             │
│ - photoUrl: "..."                       │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ LoginViewModel.handleGoogleSignInResult │
│ (result.data)                           │
│                                         │
│ Extrai:                                 │
│ - idToken = "xxx.yyy.zzz"              │
│ - email = "professor@gmail.com"        │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ viewModel.loginTeacherWithGoogle(       │
│   idToken,                              │
│   email                                 │
│ )                                       │
│                                         │
│ viewModel.teacherLoginLoading = true    │
└──────┬──────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│ AuthRepository.loginTeacherWithGoogle()  │
│                                          │
│ 1️⃣  Criar credencial Google:             │
│   credential = GoogleAuthProvider        │
│   .getCredential(idToken, null)          │
└──────┬───────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│ 2️⃣  Autenticar no Firebase:              │
│   auth.signInWithCredential(credential)  │
│   Firebase cria usuário automaticamente  │
│   uid = "abc123def456"                   │
└──────┬───────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│ 3️⃣  VALIDAR EMAIL (crucial!)             │
│                                          │
│   isEmailTeacherAuthorized(              │
│     "professor@gmail.com"                │
│   )                                      │
│                                          │
│   Query Firestore:                       │
│   Caminho: config/teacherEmails         │
│   Campo: emails (array)                  │
└──────┬───────────────────────────────────┘
       │
       ├─ NÃO (email não está na lista)
       │         │
       │         ▼
       │  ┌─────────────────────────────┐
       │  │ auth.signOut()              │
       │  │ Result.failure(              │
       │  │  "Email não autorizado"    │
       │  │ )                           │
       │  └─────┬───────────────────────┘
       │        │
       │        ▼
       │  ┌─────────────────────────────┐
       │  │ Mostrar erro na tela        │
       │  │ "❌ Email não autorizado"   │
       │  └─────────────────────────────┘
       │
       │
       ├─ SIM ✅ (email está na lista)
       │         │
       │         ▼
       ▼  ┌──────────────────────────────┐
       │  │ 4️⃣  Criar documento professor│
       │  │ Caminho: teachers/          │
       │  │          {uid}              │
       │  │                             │
       │  │ Dados:                      │
       │  │ {                           │
       │  │   uid: "abc123..."          │
       │  │   email: "prof@gmail.com"   │
       │  │   name: "Prof. João"        │
       │  │   photoUrl: "..."           │
       │  │   role: "TEACHER"           │
       │  │   createdAt: 1699564800     │
       │  │   lastLogin: 1699564800     │
       │  │ }                           │
       │  └──────┬──────────────────────┘
       │         │
       │         ▼
       ▼  ┌──────────────────────────────┐
          │ Result.success(teacherData)  │
          │ viewModel.teacherLoginSuccess│
          │   = true                     │
          │ viewModel.teacherLoading     │
          │   = false                    │
          └──────┬───────────────────────┘
                 │
                 ▼
          ┌──────────────────────────────┐
          │ LoginScreen observa:         │
          │ if (teacherSuccess) {        │
          │   onNavigateToTeacher()      │
          │ }                            │
          └──────┬───────────────────────┘
                 │
                 ▼
          ┌──────────────────────────────────────┐
          │ 🎉 SUCESSO!                          │
          │ Navega para TeacherScreen            │
          │ Professor agora pode:                │
          │ - Gerenciar tarefas                  │
          │ - Lançar notas                       │
          │ - Postar avisos                      │
          │ - Fazer chamada                      │
          │ - Gerenciar horários                 │
          └──────────────────────────────────────┘
```

---

## Visão da Estrutura Firestore

```
📦 Firestore Database (Banco de Dados)
│
├── 📂 students/               (Alunos)
│   ├── doc1: { ra, name, studentClass, parent, phone }
│   ├── doc2: { ra, name, studentClass, parent, phone }
│   └── doc3: { ... }
│
├── 📂 teachers/               (Professores autenticados)
│   ├── "abc123def456"/        ← uid do Firebase Auth
│   │   {
│   │     uid: "abc123def456",
│   │     email: "maria@gmail.com",
│   │     name: "Maria Silva",
│   │     role: "TEACHER",
│   │     photoUrl: "...",
│   │     createdAt: 1699564800
│   │   }
│   └── "xyz789abc123"/
│       { ... }
│
├── 📂 parents/                (Responsáveis autenticados)
│   ├── "anonymous_uid_1"/
│   │   {
│   │     uid: "anonymous_uid_1",
│   │     studentRa: "123456",
│   │     studentId: "doc1",
│   │     studentName: "João Silva",
│   │     studentClass: "8A",
│   │     parentName: "Maria da Silva",
│   │     role: "PARENT",
│   │     createdAt: 1699564800
│   │   }
│   └── "anonymous_uid_2"/
│       { ... }
│
├── 📂 tasks/                  (Tarefas/Provas)
│   ├── doc1: { title, description, dueDate, studentClass }
│   └── doc2: { ... }
│
├── 📂 notices/                (Avisos)
│   ├── doc1: { title, description, studentClass }
│   └── doc2: { ... }
│
├── 📂 grades/                 (Notas)
│   ├── doc1: { taskId, studentId, studentRa, score }
│   └── doc2: { ... }
│
├── 📂 attendance/             (Presença)
│   ├── doc1: { date, studentId, studentRa, isPresent, studentClass }
│   └── doc2: { ... }
│
├── 📂 schedules/              (Horários)
│   ├── doc1: { studentClass, periodo, details }
│   └── doc2: { ... }
│
└── 📂 config/                 (Configurações)
    └── teacherEmails/        ← LISTA DE EMAILS AUTORIZADOS
        {
          emails: [
            "maria@gmail.com",
            "joao@escola.com.br",
            "admin@takstud.com"
          ]
        }
```

---

## Fluxo de Dados em Tempo Real

```
┌─────────────────────────────────────────────────────────┐
│ COMO FUNCIONA A SINCRONIZAÇÃO EM TEMPO REAL            │
└─────────────────────────────────────────────────────────┘

User A (Professor Maria)         User B (Professor João)
opens app                        opens app
        │                               │
        ▼                               ▼
   Repository                    Repository
   .getTasks()                   .getTasks()
        │                               │
        └───────────────┬───────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │  Firestore Listener           │
        │  (WebSocket conectado)        │
        │                               │
        │  Observando: tasks/           │
        │  Pronto para mudanças         │
        └───────────────┬───────────────┘
                        │
    ┌───────────────────┼───────────────────┐
    │                   │                   │
    ▼                   ▼                   ▼
Alguns minutos depois...

Professor Maria:          Google               Professor João:
cria nova tarefa  ────→ Firestore ←──── recebe atualização
                        (documento)        automaticamente
                        criado)
                          │
                          ▼
                    Listener detecta
                    mudança
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
    Maria vê      João vê          Responsável vê
    atualizado    atualizado       atualizado
    INSTANTANEAMENTE!
```

---

## Sequência Temporal de Eventos

```
TEMPO          PROFESSOR MARIA                 PROFESSOR JOÃO
────────────────────────────────────────────────────────────────

T=0s    Maria abre app
        LoginScreen
        Clica "Entrar com Google"
           │
           ▼
        Google authentication
        Firebase cria conta

T=2s    Navega para TeacherScreen        João abre app
        Vê lista vazia de tarefas         Vê lista vazia

T=5s    Maria clica "Adicionar Tarefa"
        Preenche formulário
        Clica "Salvar"
           │
           ▼
        AuthRepository
        Firestore.set(task)
           │
        T=6s Firebase salva
             e notifica listeners
                                           T=6.5s João recebe
                                           atualização via listener
                                           Flow emite novo valor

T=7s    Maria vê atualizado          João vê atualizado
        (já viu na hora)             (quase instantâneo!)

T=10s   Maria clica "Editar Tarefa"
        Muda título e clica "Salvar"
           │
           ▼
        Firestore.update(task)
           │
        T=11s Firebase notifica

                                      T=11.5s João vê mudança
```

---

## Segurança: Validação de Emails

```
┌────────────────────────────────────────────────────┐
│ COMO A VALIDAÇÃO PROTEGE O APP                     │
└────────────────────────────────────────────────────┘

Cenário 1: Email autorizado ✅

Professor: joao@gmail.com
Tenta logar com Google
       │
       ▼
Firebase Auth cria conta
       │
       ▼
App query: "joao@gmail.com" em config/teacherEmails
       │
       ▼
┌──────────────────────┐
│ emails: [            │
│   "maria@gmail.com"  │
│   "joao@gmail.com"   │ ← ENCONTRADO!
│   "admin@takstud.com"│
│ ]                    │
└──────────────────────┘
       │
       ▼
Result.success()
Professor entra ✅

─────────────────────────────────────

Cenário 2: Email NÃO autorizado ❌

Hacker: hacker@gmail.com
Tenta logar com Google
       │
       ▼
Firebase Auth cria conta
       │
       ▼
App query: "hacker@gmail.com" em config/teacherEmails
       │
       ▼
┌──────────────────────┐
│ emails: [            │
│   "maria@gmail.com"  │
│   "joao@gmail.com"   │
│   "admin@takstud.com"│
│ ]                    │ ← NÃO ENCONTRADO!
└──────────────────────┘
       │
       ▼
auth.signOut()
Result.failure("Email não autorizado")
Mostra erro: "❌ Email não está autorizado"
Hacker não entra ❌

─────────────────────────────────────

Conclusão: Sua lista em Firestore é o guardião!
Você controla completamente quem entra.
```

---

## Tamanho das Requisições e Custo

```
┌──────────────────────────────────────────────────────┐
│ ESTIMATIVA DE CUSTO FIRESTORE (free tier)           │
└──────────────────────────────────────────────────────┘

Operação                    Custo      Limite Free
─────────────────────────────────────────────────────
Read (verifica email)       0.06/100k  50k/dia ✅
Write (cria documento)      0.18/100k  20k/dia ✅
Listener (conexão)          Grátis     Ilimitado
Listeners simultâneos       Grátis     100
─────────────────────────────────────────────────────

Cenário: 100 professores logando em 1 dia

100 logins = 100 reads
Custo = 100 × (0.06/100000) = ~R$0.00

Conclusão: COMPLETAMENTE GRÁTIS! 🎉
```

---

## Fluxo Simplificado para Referência Rápida

```
┌──────────────────────────────────────────┐
│ ALUNO (Responsável)                      │
│                                          │
│ Input: RA = "123456"                    │
│   ↓                                      │
│ Firestore: students{ra=="123456"}       │
│   ↓                                      │
│ Auth: signInAnonymously()                │
│   ↓                                      │
│ Firestore: create parents/{uid}          │
│   ↓                                      │
│ Output: Sessão criada ✅                 │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│ PROFESSOR                                │
│                                          │
│ Input: Google token + email              │
│   ↓                                      │
│ Auth: signInWithGoogle(token)            │
│   ↓                                      │
│ Validate: email em config/teacherEmails  │
│   ↓ ✅ Sim / ❌ Não                      │
│   ↓                                      │
│ Firestore: create teachers/{uid}         │
│   ↓                                      │
│ Output: Sessão criada ✅                 │
└──────────────────────────────────────────┘
```

Muito melhor assim, né? 🚀
