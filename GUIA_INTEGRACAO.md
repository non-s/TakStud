# 🔌 GUIA DE INTEGRAÇÃO DAS MELHORIAS

Este guia fornece instruções passo a passo para integrar cada melhoria implementada.

---

## 1️⃣ INTEGRAR LOGIN RATE LIMITER

### Passo 1: Preparar SharedPreferences em MainActivity
```kotlin
// Em MainActivity ou App class
private val loginPrefs by lazy {
    getSharedPreferences("login_security", Context.MODE_PRIVATE)
}
private val rateLimiter by lazy {
    LoginRateLimiter(loginPrefs)
}
```

### Passo 2: Proteger TeacherLoginScreen
Arquivo: `app/src/main/java/com/example/takstud/ui/login/TeacherLoginScreen.kt`

```kotlin
// Em TeacherLoginScreen composable, antes de permitir login:
val rateLimiter = remember { /* acessar do contexto */ }

Button(
    onClick = {
        val ra = teacherRA.trim()

        // Verificar rate limit
        if (!rateLimiter.isAllowedToLogin(ra)) {
            val secondsLeft = rateLimiter.getSecondsUntilRetry(ra)
            _errorMessage.value = "Muitas tentativas. Tente novamente em ${secondsLeft}s"
            return@Button
        }

        // Tentar login
        if (adminSecretValidator(adminCode)) {
            rateLimiter.clearAttempts(ra)  // Limpar após sucesso
            onLoginSuccess()
        } else {
            rateLimiter.recordFailedAttempt(ra)  // Registrar falha
            val remaining = rateLimiter.getRemainingAttempts(ra)
            _errorMessage.value = "Código inválido. ${remaining} tentativas restantes"
        }
    }
) { Text("Entrar") }
```

### Passo 3: Fazer mesmo para ParentLoginScreen e AdminLoginScreen
- Seguir padrão idêntico
- Usar RA do pai/admin como identifier

---

## 2️⃣ INTEGRAR SECURE SESSION MANAGER

### Passo 1: Criar SessionManager singleton em App
```kotlin
// app/src/main/java/com/example/takstud/TakStudApp.kt
object AppContext {
    var context: Context? = null

    fun getSessionManager(): SecureSessionManager {
        return SecureSessionManager(context!!)
    }
}

// Em MainActivity onCreate()
AppContext.context = applicationContext
```

### Passo 2: Salvar sessão após login bem-sucedido
```kotlin
// Em TeacherLoginScreen, após validar admin code
val sessionManager = SecureSessionManager(context)
sessionManager.saveSession(UserSession(
    userId = UUID.randomUUID().toString(),
    role = "TEACHER",
    name = "Professor"  // Do banco de dados
))
navigationActions.navigateToTeacher()
```

### Passo 3: Verificar sessão ao abrir App
```kotlin
// Em MainActivity onCreate() ou TakStudApp
val sessionManager = SecureSessionManager(context)
if (!sessionManager.isSessionActive()) {
    // Não há sessão ativa - ir para login
    navigationActions.navigateToHome()
} else {
    // Há sessão - verificar qual role
    val session = sessionManager.getActiveSession()
    when (session?.role) {
        "TEACHER" -> navigationActions.navigateToTeacher()
        "PARENT" -> navigationActions.navigateToParent()
        else -> navigationActions.navigateToHome()
    }
}
```

### Passo 4: Limpar sessão ao fazer logout
```kotlin
// Em TeacherScreen ou qualquer logout
val sessionManager = SecureSessionManager(context)
sessionManager.clearSession()
navigationActions.navigateToHome()
```

---

## 3️⃣ INTEGRAR ADVANCED VALIDATOR

### Passo 1: Validar nome em ManageStudentsScreen
```kotlin
// Em ManageStudentsScreen.kt - no campo de nome
var newStudentName by remember { mutableStateOf("") }
var nameError by remember { mutableStateOf<String?>(null) }

OutlinedTextField(
    value = newStudentName,
    onValueChange = { newName ->
        newStudentName = newName
        // Validar em tempo real
        nameError = when (val result = AdvancedValidator.validateName(newName)) {
            is ValidationResult.Valid -> null
            is ValidationResult.Invalid -> result.message
        }
    },
    label = { Text("Nome do Aluno") },
    isError = nameError != null
)
if (nameError != null) {
    Text(nameError!!, color = Color.Red, fontSize = 12.sp)
}
```

### Passo 2: Validar RA
```kotlin
var newStudentRa by remember { mutableStateOf("") }
var raError by remember { mutableStateOf<String?>(null) }

OutlinedTextField(
    value = newStudentRa,
    onValueChange = { newRA ->
        newStudentRa = newRA
        raError = when (val result = AdvancedValidator.validateRA(newRA)) {
            is ValidationResult.Valid -> null
            is ValidationResult.Invalid -> result.message
        }
    },
    label = { Text("RA (Registro Acadêmico)") },
    isError = raError != null
)
```

### Passo 3: Validação ao Salvar
```kotlin
Button(
    onClick = {
        // Validar todos os campos
        val nameValidation = AdvancedValidator.validateName(newStudentName)
        val raValidation = AdvancedValidator.validateRA(newStudentRa)

        when {
            nameValidation is ValidationResult.Invalid -> {
                showError(nameValidation.message)
            }
            raValidation is ValidationResult.Invalid -> {
                showError(raValidation.message)
            }
            else -> {
                // Todos válidos - proceder
                onRegisterStudent(
                    newStudentName,
                    newStudentRa,
                    selectedClass
                )
            }
        }
    }
) { Text("Cadastrar Aluno") }
```

### Passo 4: Fazer similar para outras telas
- AddTaskScreen: Validar título (validateTitle) e descrição (validateDescription)
- ManageScheduleScreen: Validar intervalo de tempo (validateTimeRange)
- Qualquer tela com telefone: Usar validatePhone()
- Qualquer tela com email: Usar validateEmail()

---

## 4️⃣ INTEGRAR ERROR HANDLER

### Passo 1: Substituir try-catch em Repository
```kotlin
// ANTES:
fun getTasks(): Flow<List<Task>> = callbackFlow {
    val listener = db.collection("tasks").addSnapshotListener { snapshots, e ->
        if (e != null) {
            Log.w("TakStud", "getTasks falhou", e)
            close(e)
            return@addSnapshotListener
        }
        // ... processar
    }
    awaitClose { listener.remove() }
}

// DEPOIS:
fun getTasks(): Flow<List<Task>> = callbackFlow {
    val listener = db.collection("tasks").addSnapshotListener { snapshots, e ->
        if (e != null) {
            ErrorHandler.logError("Carregamento de tarefas", e)
            close(e)
            return@addSnapshotListener
        }
        // ... processar
    }
    awaitClose { listener.remove() }
}
```

### Passo 2: Usar withRetry em operações críticas
```kotlin
// Em SyncWorker ou função de sincronização
suspend fun syncAttendance() {
    val result = ErrorHandler.withRetry(
        maxAttempts = 3,
        delayMillis = 500,
        operationName = "Sincronização de Frequência"
    ) {
        repository.syncAttendance()
    }

    when (result) {
        is ErrorHandler.Result.Success -> {
            Log.i("Sync", "Frequência sincronizada com sucesso")
        }
        is ErrorHandler.Result.Error -> {
            Log.e("Sync", result.message)
            // Notificar usuário
        }
        ErrorHandler.Result.Loading -> {}
    }
}
```

### Passo 3: Validação centralizada
```kotlin
// Em ViewModel
fun saveStudent(name: String, ra: String, className: String) {
    val nameValidation = AdvancedValidator.validateName(name)
    val raValidation = AdvancedValidator.validateRA(ra)

    // Combinar com ErrorHandler
    if (nameValidation is ValidationResult.Invalid) {
        _errorMessage.value = nameValidation.message
        return
    }

    if (raValidation is ValidationResult.Invalid) {
        _errorMessage.value = raValidation.message
        return
    }

    // Proceder com salvamento
    viewModelScope.launch {
        val result = ErrorHandler.withErrorHandling(
            "Salvamento de aluno",
            "Erro ao registrar aluno"
        ) {
            val student = Student(
                id = UUID.randomUUID().toString(),
                name = (nameValidation as ValidationResult.Valid).data,
                ra = (raValidation as ValidationResult.Valid).data,
                studentClass = className
            )
            repository.saveStudent(student)
        }

        when (result) {
            is ErrorHandler.Result.Success -> {
                _successMessage.value = "Aluno registrado com sucesso"
            }
            is ErrorHandler.Result.Error -> {
                _errorMessage.value = result.message
            }
            ErrorHandler.Result.Loading -> {}
        }
    }
}
```

---

## 🔒 FIREBASE REMOTE CONFIG SETUP (Crítico!)

### Passo 1: Acessar Firebase Console
1. Vá para [https://console.firebase.google.com](https://console.firebase.google.com)
2. Selecione seu projeto TakStud
3. Vá para: **Remote Config**

### Passo 2: Adicionar admin_secret
1. Clique em "Create configuration"
2. **Parameter key**: `admin_secret`
3. **Default value**: `seu_codigo_aqui` (mude para algo seguro, ex: "abc123xyz789")
4. Clique "Publish"

### Passo 3: Testar
- A app carregará automaticamente o valor
- Se não configurado, verá erro: "Código admin não configurado"

**IMPORTANTE**: Mude esse código frequentemente por segurança!

---

## 📱 TESTANDO AS INTEGRAÇÕES

### Teste 1: Rate Limiter
```
1. Abrir TeacherLoginScreen
2. Tentar login 5x com RA/código errado
3. 6ª tentativa deve bloquear com "Muitas tentativas"
4. Aguardar 1 segundo da última tentativa (simulado)
```

### Teste 2: Session Manager
```
1. Fazer login com sucesso
2. Fechar app
3. Abrir app novamente
4. Deve ir direto para TeacherScreen (sessão recuperada)
```

### Teste 3: Advanced Validator
```
1. ManageStudentsScreen
2. Tentar cadastrar com nome vazio → erro "Nome não pode estar vazio"
3. Tentar com "A" → erro "Nome deve ter pelo menos 3 caracteres"
4. Tentar com RA "1" → erro "RA deve ter pelo menos 2 caracteres"
5. Valores válidos → aceita
```

### Teste 4: Error Handler
```
1. (Opcional) Desconectar internet
2. Tentar sincronizar frequência
3. Deve tentar 3x com delay exponencial
4. Mostrar erro amigável ao usuário
```

---

## 🚀 CHECKLIST DE INTEGRAÇÃO

- [ ] LoginRateLimiter integrado em TeacherLoginScreen
- [ ] LoginRateLimiter integrado em ParentLoginScreen
- [ ] SecureSessionManager integrado em MainActivity
- [ ] SecureSessionManager integrado em logout
- [ ] AdvancedValidator em ManageStudentsScreen
- [ ] AdvancedValidator em AddTaskScreen
- [ ] AdvancedValidator em ManageScheduleScreen
- [ ] ErrorHandler em Repository
- [ ] ErrorHandler em SyncWorker
- [ ] Firebase Remote Config configurado com admin_secret
- [ ] Todos os testes passando
- [ ] App compila sem warnings relacionados

---

## 🐛 TROUBLESHOOTING

### Erro: "No context available"
**Solução**: Garantir que AppContext.context foi inicializado em MainActivity.onCreate()

### Erro: "EncryptedSharedPreferences not found"
**Solução**: Adicionar dependência `androidx.security:security-crypto:1.1.0-alpha06`

### Erro: "admin_secret não configurado"
**Solução**: Ir para Firebase Console → Remote Config → Adicionar parameter

### Erro: "Cannot cast to ValidationResult.Valid"
**Solução**: Sempre usar `is ValidationResult.Valid<*>` ao fazer cast

### App lento após rate limiter
**Solução**: Rate limiter usa SharedPreferences que é rápido. Se ainda lento, checar se LoginRateLimiter está em thread principal

---

## 📚 REFERÊNCIAS

- SecurityCrypto: https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
- Firebase Remote Config: https://firebase.google.com/docs/remote-config
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
- Type-safe Result pattern: https://www.baeldung.com/kotlin/sealed-classes

---

**Última atualização**: 12/11/2025
