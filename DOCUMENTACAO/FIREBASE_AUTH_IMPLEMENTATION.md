# 🔐 Firebase Authentication - Guia Completo de Implementação

## 📌 Problema a Resolver

**Alunos (Pais):** Login simples com RA (já funciona)
**Professores:** Login com Google Sign-In (cadastro automático)

---

## 🎯 Solução Final

### Fluxo de Autenticação

```
┌─────────────────────────────────────────────────────────┐
│                  LOGIN SCREEN                           │
└──────────────┬──────────────────────────────────────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
   ┌────────────┐   ┌──────────────────────┐
   │ LOGIN PAIS │   │ LOGIN PROFESSOR      │
   │ (RA)       │   │ (Google Sign-In)     │
   └─────┬──────┘   └──────────┬───────────┘
         │                     │
         │                     ▼
         │          ┌─────────────────────┐
         │          │ Google OAuth 2.0    │
         │          │ (conta google)      │
         │          └──────────┬──────────┘
         │                     │
         │                     ▼
         │          ┌─────────────────────────┐
         │          │ Firebase Auth          │
         │          │ (cria usuario auto)    │
         │          └──────────┬──────────────┘
         │                     │
         ▼                     ▼
    ┌─────────────────────────────────────┐
    │  Firestore                          │
    │  - Validar RA (alunos)              │
    │  - Validar email professor          │
    │  - Criar documento de perfil        │
    └─────────────────────────────────────┘
```

---

## 🔧 PASSO 1: Configuração no Console Firebase

### 1.1 Ativar Firebase Authentication

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Selecione seu projeto
3. **Authentication** → **Sign-in method**
4. Ative:
   - ✅ **Email/Password** (para admin/testes)
   - ✅ **Google**

### 1.2 Configurar Google Sign-In

1. Na aba **Sign-in method** → **Google**
2. Ative a chave
3. Configure:
   - **Nome público do projeto:** TakStud
   - **Email de suporte:** seu-email@gmail.com

### 1.3 Obter SHA-1 do seu projeto

```bash
# No terminal do Android Studio
./gradlew signingReport

# Procure por: "SHA1"
# Exemplo: SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90
```

4. Copie o SHA-1 e adicione em **Firebase Console** → **Configurações do Projeto** → **Apps Android**

### 1.4 Configurar Firestore Security Rules

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Usuários podem ler apenas seu próprio documento
    match /teachers/{userId} {
      allow read, write: if request.auth.uid == userId;
    }

    match /students/{userId} {
      allow read, write: if request.auth.uid == userId;
    }

    // Dados públicos (tarefas, avisos) - leitura por turma
    match /tasks/{document=**} {
      allow read: if request.auth != null;
      allow write: if isTeacher();
    }

    match /notices/{document=**} {
      allow read: if request.auth != null;
      allow write: if isTeacher();
    }

    match /schedules/{document=**} {
      allow read: if request.auth != null;
      allow write: if isTeacher();
    }

    match /students/{document=**} {
      allow read: if request.auth != null;
      allow write: if isTeacher();
    }

    match /grades/{document=**} {
      allow read: if isStudent() || isTeacher();
      allow write: if isTeacher();
    }

    match /attendance/{document=**} {
      allow read: if isStudent() || isTeacher();
      allow write: if isTeacher();
    }

    // Funções auxiliares
    function isTeacher() {
      return get(/databases/$(database)/documents/teachers/$(request.auth.uid)).data.role == 'TEACHER';
    }

    function isStudent() {
      return get(/databases/$(database)/documents/students/$(request.auth.uid)).data.role == 'STUDENT';
    }
  }
}
```

---

## 📦 PASSO 2: Adicionar Dependências

### build.gradle.kts (app)

```kotlin
// No bloco dependencies, adicione:
implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
implementation("com.google.android.gms:play-services-auth:21.0.0")
implementation("androidx.credentials:credentials:1.2.0")
implementation("androidx.credentials:credentials-play-services-auth:1.2.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
```

---

## 🔨 PASSO 3: Criar o AuthRepository

### Arquivo: AuthRepository.kt

```kotlin
package com.example.takstud

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.takstud.model.UserSession
import com.example.takstud.model.Role

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    // ===== AUTENTICAÇÃO PROFESSOR COM GOOGLE =====

    /**
     * Login de professor com Google
     *
     * Fluxo:
     * 1. Google OAuth 2.0 (usuário faz login)
     * 2. Firebase cria documento do usuário
     * 3. Verifica se email está na lista de professores
     * 4. Cria documento em /teachers/{uid}
     * 5. Retorna sucesso
     */
    suspend fun loginTeacherWithGoogle(
        idToken: String,
        email: String
    ): Result<UserSession> = try {
        // 1. Autenticar com Firebase usando Google ID token
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val uid = authResult.user?.uid ?: throw Exception("UID não obtido")

        // 2. Validar se email está autorizado como professor
        if (!isEmailTeacherAuthorized(email)) {
            auth.signOut()
            return Result.failure(Exception("Email não autorizado como professor"))
        }

        // 3. Criar documento do professor no Firestore
        val teacherData = mapOf(
            "uid" to uid,
            "email" to email,
            "name" to (authResult.user?.displayName ?: ""),
            "photoUrl" to (authResult.user?.photoUrl?.toString() ?: ""),
            "role" to "TEACHER",
            "createdAt" to System.currentTimeMillis(),
            "class" to ""  // Vai ser preenchido depois
        )
        db.collection("teachers").document(uid).set(teacherData).await()

        // 4. Criar sessão
        val session = UserSession(
            userId = uid,
            email = email,
            role = Role.TEACHER,
            name = authResult.user?.displayName ?: "",
            photoUrl = authResult.user?.photoUrl?.toString() ?: ""
        )

        Result.success(session)
    } catch (e: FirebaseAuthException) {
        Result.failure(Exception("Erro Firebase Auth: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ===== AUTENTICAÇÃO ALUNO COM RA =====

    /**
     * Login de responsável (pai) com RA do aluno
     *
     * Fluxo:
     * 1. Buscar aluno no Firestore por RA
     * 2. Se encontrou, criar sessão anônima com RA
     * 3. Retorna documento do aluno
     */
    suspend fun loginParentWithRA(studentRA: String): Result<UserSession> = try {
        // 1. Buscar aluno por RA
        val snapshot = db.collection("students")
            .whereEqualTo("ra", studentRA)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            return Result.failure(Exception("RA não encontrado"))
        }

        val studentDoc = snapshot.documents[0]
        val studentId = studentDoc.id
        val studentName = studentDoc.getString("name") ?: ""
        val studentClass = studentDoc.getString("studentClass") ?: ""
        val parentName = studentDoc.getString("parent") ?: ""

        // 2. Fazer login anônimo no Firebase
        val authResult = auth.signInAnonymously().await()
        val anonUid = authResult.user?.uid ?: throw Exception("Erro ao criar sessão anônima")

        // 3. Criar documento do responsável
        val parentData = mapOf(
            "uid" to anonUid,
            "studentRa" to studentRA,
            "studentId" to studentId,
            "studentName" to studentName,
            "studentClass" to studentClass,
            "parentName" to parentName,
            "role" to "PARENT",
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("parents").document(anonUid).set(parentData).await()

        // 4. Criar sessão
        val session = UserSession(
            userId = anonUid,
            email = "",
            role = Role.PARENT,
            name = parentName,
            studentRa = studentRA,
            studentClass = studentClass
        )

        Result.success(session)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ===== LOGOUT =====

    fun logout() {
        auth.signOut()
    }

    // ===== FUNÇÕES AUXILIARES =====

    /**
     * Valida se email está autorizado como professor
     *
     * Você tem 3 opções:
     * 1. Lista de emails em Firestore (RECOMENDADO)
     * 2. Domínio de email (ex: @escola.com.br)
     * 3. Qualquer email Google (inseguro)
     */
    private suspend fun isEmailTeacherAuthorized(email: String): Boolean = try {
        // OPÇÃO 1: Verificar em Firestore (RECOMENDADO)
        val snapshot = db.collection("config")
            .document("teacherEmails")
            .get()
            .await()

        @Suppress("UNCHECKED_CAST")
        val authorizedEmails = snapshot.get("emails") as? List<String> ?: emptyList()

        return email in authorizedEmails
    } catch (e: Exception) {
        // Se houver erro, negar acesso
        false
    }

    // Alternativa: verificar por domínio
    fun isEmailFromAuthenticatedDomain(email: String): Boolean {
        return email.endsWith("@escola.com.br") ||
               email.endsWith("@seu-dominio.com.br")
    }

    // Alternativa: qualquer email Google (menos seguro)
    fun isGoogleEmail(email: String): Boolean {
        return email.endsWith("@gmail.com")
    }

    /**
     * Obter usuário atual
     */
    fun getCurrentUser() = auth.currentUser

    /**
     * Verificar se usuário está autenticado
     */
    fun isUserAuthenticated() = auth.currentUser != null
}
```

---

## 🎨 PASSO 4: Criar Composable de Login com Google

### Arquivo: GoogleSignInButton.kt

```kotlin
package com.example.takstud.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    text: String = "Entrar com Google"
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = "Google",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
        if (isLoading) {
            Spacer(modifier = Modifier.width(8.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}
```

---

## 🔑 PASSO 5: Atualizar LoginViewModel

### Arquivo: LoginViewModel.kt (ATUALIZADO)

```kotlin
package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.AuthRepository
import com.example.takstud.model.UserSession
import com.example.takstud.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val studentAuthRepository: StudentAuthRepository  // Ainda precisamos dessa
) : ViewModel() {

    // ===== PROFESSOR LOGIN STATE =====

    private val _teacherLoginEmail = MutableStateFlow("")
    val teacherLoginEmail: StateFlow<String> = _teacherLoginEmail.asStateFlow()

    private val _teacherLoginError = MutableStateFlow<String?>(null)
    val teacherLoginError: StateFlow<String?> = _teacherLoginError.asStateFlow()

    private val _teacherLoginLoading = MutableStateFlow(false)
    val teacherLoginLoading: StateFlow<Boolean> = _teacherLoginLoading.asStateFlow()

    // ===== ALUNO (PAI) LOGIN STATE =====

    private val _parentLoginRA = MutableStateFlow("")
    val parentLoginRA: StateFlow<String> = _parentLoginRA.asStateFlow()

    private val _parentLoginError = MutableStateFlow<String?>(null)
    val parentLoginError: StateFlow<String?> = _parentLoginError.asStateFlow()

    private val _parentLoginLoading = MutableStateFlow(false)
    val parentLoginLoading: StateFlow<Boolean> = _parentLoginLoading.asStateFlow()

    // ===== SESSÃO GLOBAL =====

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // ===== LOGIN DO PROFESSOR COM GOOGLE =====

    /**
     * Iniciar login do professor com Google
     *
     * Retorna um Intent para ser usado em Activity:
     * ```
     * val intent = loginViewModel.getGoogleSignInIntent()
     * launcher.launch(intent)
     * ```
     */
    fun initiateGoogleSignIn(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID")  // Ver passo 6
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    /**
     * Completar login do professor com Google
     *
     * Chamado após usuário retornar do Google Sign-In
     */
    fun handleGoogleSignInResult(idToken: String, email: String) {
        _teacherLoginLoading.value = true
        _teacherLoginError.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.loginTeacherWithGoogle(idToken, email)

                result.onSuccess { session ->
                    _currentSession.value = session
                    _isLoggedIn.value = true
                    _teacherLoginLoading.value = false
                }

                result.onFailure { error ->
                    _teacherLoginError.value = error.message ?: "Erro desconhecido"
                    _teacherLoginLoading.value = false
                }
            } catch (e: Exception) {
                _teacherLoginError.value = e.message
                _teacherLoginLoading.value = false
            }
        }
    }

    // ===== LOGIN DO ALUNO COM RA =====

    fun updateParentRA(ra: String) {
        _parentLoginRA.value = ra
    }

    fun loginWithRA() {
        if (_parentLoginRA.value.isBlank()) {
            _parentLoginError.value = "Digite um RA válido"
            return
        }

        _parentLoginLoading.value = true
        _parentLoginError.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.loginParentWithRA(_parentLoginRA.value)

                result.onSuccess { session ->
                    _currentSession.value = session
                    _isLoggedIn.value = true
                    _parentLoginLoading.value = false
                }

                result.onFailure { error ->
                    _parentLoginError.value = error.message ?: "Erro ao fazer login"
                    _parentLoginLoading.value = false
                }
            } catch (e: Exception) {
                _parentLoginError.value = e.message
                _parentLoginLoading.value = false
            }
        }
    }

    // ===== LOGOUT =====

    fun logout() {
        authRepository.logout()
        _currentSession.value = null
        _isLoggedIn.value = false
        _teacherLoginEmail.value = ""
        _parentLoginRA.value = ""
        _teacherLoginError.value = null
        _parentLoginError.value = null
    }

    fun clearError(isTeacher: Boolean) {
        if (isTeacher) {
            _teacherLoginError.value = null
        } else {
            _parentLoginError.value = null
        }
    }
}
```

---

## 📱 PASSO 6: Obter Web Client ID do Google

### Para ativar Google Sign-In no Android, você precisa do Web Client ID:

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Selecione seu projeto TakStud
3. **APIs & Services** → **Credentials**
4. Procure por **"Web client"** na lista
5. Copie o **Client ID**
6. Substitua em `LoginViewModel.kt`:

```kotlin
.requestIdToken("SEU_WEB_CLIENT_ID_AQUI")
```

---

## 🎯 PASSO 7: Atualizar LoginScreen para Google Sign-In

### Arquivo: LoginScreen.kt (ATUALIZADO)

```kotlin
@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onNavigateToTeacher: () -> Unit,
    onNavigateToParent: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val parentRA by loginViewModel.parentLoginRA.collectAsState()
    val parentError by loginViewModel.parentLoginError.collectAsState()
    val parentLoading by loginViewModel.parentLoginLoading.collectAsState()

    val teacherEmail by loginViewModel.teacherLoginEmail.collectAsState()
    val teacherError by loginViewModel.teacherLoginError.collectAsState()
    val teacherLoading by loginViewModel.teacherLoginLoading.collectAsState()

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken ?: return@rememberLauncherForActivityResult

            loginViewModel.handleGoogleSignInResult(
                idToken = idToken,
                email = account.email ?: return@rememberLauncherForActivityResult
            )

            if (loginViewModel.isLoggedIn.value) {
                onNavigateToTeacher()
            }
        } catch (e: ApiException) {
            // Erro no Google Sign-In
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TAB 1: LOGIN RESPONSÁVEL
            if (selectedTab == 0) {
                Text("Login - Responsável", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = parentRA,
                    onValueChange = { loginViewModel.updateParentRA(it) },
                    label = { Text("RA do Aluno") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (parentError != null) {
                    Text(parentError!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { loginViewModel.loginWithRA() },
                    enabled = !parentLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (parentLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Entrar")
                    }
                }
            }

            // TAB 2: LOGIN PROFESSOR
            if (selectedTab == 1) {
                Text("Login - Professor", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(24.dp))

                if (teacherError != null) {
                    Text(teacherError!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                GoogleSignInButton(
                    onClick = {
                        val intent = loginViewModel.initiateGoogleSignIn()
                        launcher.launch(intent)
                    },
                    isLoading = teacherLoading,
                    text = "Entrar com Google"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Você receberá acesso automaticamente se seu email estiver autorizado",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ABAS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                    colors = if (selectedTab == 0) ButtonDefaults.buttonColors()
                             else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Responsável")
                }

                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    colors = if (selectedTab == 1) ButtonDefaults.buttonColors()
                             else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Professor")
                }
            }
        }
    }
}
```

---

## 📊 Resumo: 3 Opções de Autenticação de Professor

| Opção | Segurança | Facilidade | Custo |
|-------|-----------|-----------|-------|
| **1. Google Sign-In (Recomendado)** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Grátis |
| **2. Email + Senha customizado** | ⭐⭐⭐ | ⭐⭐ | Grátis |
| **3. Código de acesso (Atual)** | ⭐ | ⭐⭐⭐⭐⭐ | Grátis |

---

## ✅ Checklist de Implementação

- [ ] Ativar Firebase Authentication no console
- [ ] Configurar Google Sign-In
- [ ] Obter SHA-1 e adicionar em Firebase
- [ ] Obter Web Client ID do Google Cloud
- [ ] Adicionar dependências Firebase Auth
- [ ] Criar AuthRepository com métodos de login
- [ ] Atualizar LoginViewModel
- [ ] Atualizar LoginScreen com Google Button
- [ ] Configurar Firestore Security Rules
- [ ] Testar login do professor com Google
- [ ] Testar login do aluno com RA
- [ ] Configurar lista de emails autorizados em Firestore

---

## 🔒 Configuração da Lista de Emails Autorizados

No Firestore, crie um documento assim:

**Caminho:** `config/teacherEmails`

```json
{
  "emails": [
    "professor1@gmail.com",
    "professor2@gmail.com",
    "diretor@escola.com.br"
  ]
}
```

Assim você controla quem pode ser professor sem ter que mexer no código!

---

## 🚀 Benefícios da Solução

✅ **Professor:** Login automático com Google (cadastro automático)
✅ **Aluno/Responsável:** Login simples com RA
✅ **Segurança:** Firebase Auth + Security Rules
✅ **Escalabilidade:** Adiciona professores editando um documento
✅ **Sem customização:** Google cuida da autenticação