# 🎯 Resposta Rápida: Autenticação de Professor com Google

## Sua Pergunta

> "Se eu colocar login com email do Google, eu tenho que cadastrar o professor antes, ou ele faz sozinho o cadastro?"

## Resposta Direta

**Depende de como você configurar:**

### Opção A: Cadastro Totalmente Automático ❌ (Inseguro)
```
Professor loga com Google → Entra direto
PROBLEMA: Qualquer um com Google entra!
```

### Opção B: Cadastro Automático + Validação ✅ (RECOMENDADO)
```
1. Você coloca lista de emails autorizados em Firestore
2. Professor tenta logar com Google
3. App verifica: "Seu email está na lista?"
4. Se SIM → Entra direto (cadastro automático)
5. Se NÃO → Rejeitado
VANTAGEM: Seguro + zero configuração para professor
```

### Opção C: Cadastro Manual (Você cria conta) ❌ (Chato)
```
1. Você cria conta do professor com email + senha
2. Envia senha temporária por email
3. Professor faz login com email + senha
PROBLEMA: Senhas pra lembrar, resets chatos
```

---

## 🚀 Como Implementar a Opção B (Recomendada)

### Setup Inicial (Faz UMA ÚNICA VEZ)

#### 1. Firebase Console

```
1. https://console.firebase.google.com/
2. Authentication → Sign-in method
3. Ativa Google
4. Ativa Email/Password
```

#### 2. Google Cloud Console

```
1. https://console.cloud.google.com/
2. Procura por "credentials" na barra de busca
3. Copia o "Web Client ID"
   (você usará no código)
```

#### 3. Firestore - Lista de Emails

```
1. Firebase Console → Firestore Database
2. Cria coleção "config"
3. Cria documento (auto ID)
4. Adiciona campo "emails" (tipo: Array)
5. Adiciona valores:
   - "maria@gmail.com"
   - "joao@escola.com.br"
   - "seu@email.com"
6. Salva
```

### Código - 4 Arquivos Principais

#### Arquivo 1: AuthRepository.kt

```kotlin
class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun loginTeacherWithGoogle(
        idToken: String,
        email: String
    ): Result<Map<String, Any>> = try {
        // 1. Autentica com Google
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val uid = result.user?.uid ?: throw Exception("Erro")

        // 2. Verifica se email está autorizado
        if (!isEmailAuthorized(email)) {
            auth.signOut()
            throw Exception("Email não autorizado")
        }

        // 3. Cria documento do professor
        val data = mapOf(
            "uid" to uid,
            "email" to email,
            "name" to (result.user?.displayName ?: ""),
            "role" to "TEACHER"
        )
        db.collection("teachers").document(uid).set(data).await()

        Result.success(data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun isEmailAuthorized(email: String): Boolean = try {
        val doc = db.collection("config")
            .document("teacherEmails")
            .get()
            .await()

        val emails = doc.get("emails") as? List<String> ?: emptyList()
        return email in emails
    } catch (e: Exception) {
        false
    }

    suspend fun loginParentWithRA(ra: String): Result<Map<String, Any>> = try {
        val snapshot = db.collection("students")
            .whereEqualTo("ra", ra)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) throw Exception("RA não encontrado")

        val result = auth.signInAnonymously().await()
        val uid = result.user?.uid ?: throw Exception("Erro")

        val data = mapOf(
            "uid" to uid,
            "studentRa" to ra,
            "role" to "PARENT"
        )
        db.collection("parents").document(uid).set(data).await()

        Result.success(data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() = auth.signOut()
}
```

#### Arquivo 2: GoogleSignInHelper.kt

```kotlin
class GoogleSignInHelper(context: Context) {
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("SEU_WEB_CLIENT_ID")  // 👈 MUDE!
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent() = googleSignInClient.signInIntent

    fun handleSignInResult(data: Intent?): Pair<String, String>? {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)

            if (account?.idToken != null && account.email != null) {
                Pair(account.idToken!!, account.email!!)
            } else null
        } catch (e: ApiException) {
            null
        }
    }

    fun signOut() = googleSignInClient.signOut()
}
```

#### Arquivo 3: LoginViewModel.kt

```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleHelper: GoogleSignInHelper
) : ViewModel() {

    private val _teacherLoading = MutableStateFlow(false)
    val teacherLoading: StateFlow<Boolean> = _teacherLoading.asStateFlow()

    private val _teacherError = MutableStateFlow<String?>(null)
    val teacherError: StateFlow<String?> = _teacherError.asStateFlow()

    private val _teacherSuccess = MutableStateFlow(false)
    val teacherSuccess: StateFlow<Boolean> = _teacherSuccess.asStateFlow()

    fun getGoogleSignInIntent() = googleHelper.getSignInIntent()

    fun handleGoogleSignInResult(intent: Intent?) {
        val result = googleHelper.handleSignInResult(intent) ?: run {
            _teacherError.value = "Erro ao fazer login"
            return
        }

        val (idToken, email) = result
        _teacherLoading.value = true

        viewModelScope.launch {
            authRepository.loginTeacherWithGoogle(idToken, email)
                .onSuccess {
                    _teacherSuccess.value = true
                    _teacherLoading.value = false
                }
                .onFailure { error ->
                    _teacherError.value = error.message
                    _teacherLoading.value = false
                }
        }
    }
}
```

#### Arquivo 4: LoginScreen.kt

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToTeacher: () -> Unit
) {
    val teacherLoading by viewModel.teacherLoading.collectAsState()
    val teacherError by viewModel.teacherError.collectAsState()
    val teacherSuccess by viewModel.teacherSuccess.collectAsState()

    if (teacherSuccess) {
        onNavigateToTeacher()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login - Professor", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        if (teacherError != null) {
            Text(teacherError!!, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val intent = viewModel.getGoogleSignInIntent()
                launcher.launch(intent)
            },
            enabled = !teacherLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (teacherLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Entrar com Google")
            }
        }
    }
}
```

### Adicionar Dependências

Em `app/build.gradle.kts`:

```kotlin
implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
implementation("com.google.android.gms:play-services-auth:21.0.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
```

---

## 📊 Fluxo Final

```
PROFESSOR clica "Entrar com Google"
    ↓
Google abre janela de login
    ↓
Professor faz login
    ↓
Firebase cria conta automaticamente
    ↓
App verifica: "Email está na lista?"
    ↓
SIM ✅ → Entra direto
NÃO ❌ → "Email não autorizado"
```

```
ALUNO/RESPONSÁVEL:
    ↓
Digite RA
    ↓
Verifica em Firestore
    ↓
Entra com sessão anônima
```

---

## ⏰ Tempo de Setup

- Setup Firebase: **10 minutos**
- Implementar código: **30 minutos**
- Testar: **5 minutos**

**Total: ~45 minutos**

---

## ✅ Resumo Executivo

| Item | Aluno/Responsável | Professor |
|------|------------------|-----------|
| **Como loga?** | RA | Google |
| **Você cadastra?** | Sim (no app ou Firestore) | Não (auto) |
| **Senha?** | Não | Não (usa Google) |
| **Setup?** | Uma vez por aluno | Uma vez por email |

---

## 🎯 Próximos Passos

1. Leia `SETUP_FIREBASE_STEP_BY_STEP.md` para instruções visuais
2. Leia `CODIGO_PRONTO_COPIAR_COLAR.md` para código completo
3. Copie e cole os 4 arquivos
4. Configure Web Client ID
5. Crie lista de emails em Firestore
6. Teste!

**Dúvidas?** Consulte `COMPARACAO_METODOS_AUTENTICACAO.md` 📖
