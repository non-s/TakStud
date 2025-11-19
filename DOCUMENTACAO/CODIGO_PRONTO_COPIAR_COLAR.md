# 💻 Código Pronto para Copiar e Colar

Este documento contém **código 100% pronto** para implementar Firebase Authentication com Google Sign-In.

Basta copiar, colar e adaptar para seu projeto!

---

## 📦 PASSO 1: Adicionar Dependências

### Arquivo: `app/build.gradle.kts`

```kotlin
dependencies {
    // ... suas dependências existentes ...

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Para coroutines (se não tiver já)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

**Depois:** Clique em "Sync Now"

---

## 📄 PASSO 2: Criar AuthRepository.kt

### Caminho: `app/src/main/java/com/example/takstud/AuthRepository.kt`

```kotlin
package com.example.takstud

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    private val TAG = "AuthRepository"

    // ========================
    // LOGIN PROFESSOR GOOGLE
    // ========================

    /**
     * Login do professor com Google Sign-In
     *
     * Exemplo de uso:
     * ```
     * val result = authRepository.loginTeacherWithGoogle(
     *     idToken = "xxx",
     *     email = "professor@gmail.com"
     * )
     * result.onSuccess { user ->
     *     println("Login sucesso: ${user["email"]}")
     * }
     * result.onFailure { error ->
     *     println("Erro: ${error.message}")
     * }
     * ```
     */
    suspend fun loginTeacherWithGoogle(
        idToken: String,
        email: String
    ): Result<Map<String, Any>> {
        return try {
            Log.d(TAG, "Tentando login professor com email: $email")

            // PASSO 1: Autenticar com Google no Firebase
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val uid = authResult.user?.uid ?: throw Exception("UID não obtido")

            Log.d(TAG, "Autenticado no Firebase com UID: $uid")

            // PASSO 2: Verificar se email está autorizado
            if (!isEmailTeacherAuthorized(email)) {
                Log.w(TAG, "Email $email não está autorizado")
                auth.signOut()
                return Result.failure(
                    Exception("❌ Email não está autorizado como professor")
                )
            }

            Log.d(TAG, "Email $email está autorizado")

            // PASSO 3: Criar documento do professor no Firestore
            val teacherData = mapOf(
                "uid" to uid,
                "email" to email,
                "name" to (authResult.user?.displayName ?: ""),
                "photoUrl" to (authResult.user?.photoUrl?.toString() ?: ""),
                "role" to "TEACHER",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to System.currentTimeMillis()
            )

            db.collection("teachers").document(uid).set(teacherData).await()

            Log.d(TAG, "Documento professor criado em Firestore")

            // PASSO 4: Retornar sucesso
            Result.success(teacherData)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer login professor", e)
            Result.failure(e)
        }
    }

    // ========================
    // LOGIN RESPONSÁVEL (RA)
    // ========================

    /**
     * Login do responsável usando RA do aluno
     *
     * Exemplo de uso:
     * ```
     * val result = authRepository.loginParentWithRA("123456")
     * result.onSuccess { user ->
     *     println("Login responsável sucesso")
     * }
     * ```
     */
    suspend fun loginParentWithRA(studentRA: String): Result<Map<String, Any>> {
        return try {
            Log.d(TAG, "Tentando login responsável com RA: $studentRA")

            // PASSO 1: Buscar aluno no Firestore
            val snapshot = db.collection("students")
                .whereEqualTo("ra", studentRA)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.w(TAG, "RA $studentRA não encontrado")
                return Result.failure(Exception("❌ RA não encontrado"))
            }

            val studentDoc = snapshot.documents[0]
            val studentId = studentDoc.id
            val studentName = studentDoc.getString("name") ?: ""
            val studentClass = studentDoc.getString("studentClass") ?: ""
            val parentName = studentDoc.getString("parent") ?: ""

            Log.d(TAG, "Aluno encontrado: $studentName (turma: $studentClass)")

            // PASSO 2: Fazer login anônimo no Firebase
            val authResult = auth.signInAnonymously().await()
            val uid = authResult.user?.uid ?: throw Exception("Erro ao criar sessão")

            Log.d(TAG, "Login anônimo realizado, UID: $uid")

            // PASSO 3: Criar documento do responsável
            val parentData = mapOf(
                "uid" to uid,
                "studentRa" to studentRA,
                "studentId" to studentId,
                "studentName" to studentName,
                "studentClass" to studentClass,
                "parentName" to parentName,
                "role" to "PARENT",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to System.currentTimeMillis()
            )

            db.collection("parents").document(uid).set(parentData).await()

            Log.d(TAG, "Documento responsável criado")

            // PASSO 4: Retornar sucesso
            Result.success(parentData)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer login responsável", e)
            Result.failure(e)
        }
    }

    // ========================
    // LOGOUT
    // ========================

    fun logout() {
        Log.d(TAG, "Logout realizado")
        auth.signOut()
    }

    // ========================
    // FUNÇÕES AUXILIARES
    // ========================

    /**
     * Verifica se email está na lista de professores autorizados
     *
     * Busca em: Firestore → config → teacherEmails
     */
    private suspend fun isEmailTeacherAuthorized(email: String): Boolean {
        return try {
            val snapshot = db.collection("config")
                .document("teacherEmails")
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            val authorizedEmails = snapshot.get("emails") as? List<String> ?: emptyList()

            Log.d(TAG, "Emails autorizados: $authorizedEmails")

            return email in authorizedEmails
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar emails autorizados", e)
            // Se houver erro, nega acesso (seguro)
            false
        }
    }

    /**
     * Obter usuário atual
     */
    fun getCurrentUser() = auth.currentUser

    /**
     * Verificar se há usuário logado
     */
    fun isUserAuthenticated() = auth.currentUser != null

    /**
     * Obter UID do usuário atual
     */
    fun getCurrentUID() = auth.currentUser?.uid
}
```

---

## 🎨 PASSO 3: Criar GoogleSignInHelper.kt

### Caminho: `app/src/main/java/com/example/takstud/GoogleSignInHelper.kt`

```kotlin
package com.example.takstud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleSignInHelper(context: Context) {

    private val googleSignInClient: GoogleSignInClient

    init {
        // ⚠️ IMPORTANTE: Substitua "YOUR_WEB_CLIENT_ID" pelo seu Web Client ID
        // Veja: https://console.cloud.google.com/ → Credentials → Web Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID_AQUI")  // 👈 MUDE ISSO
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Retorna a intent para abrir a janela de login do Google
     */
    fun getSignInIntent() = googleSignInClient.signInIntent

    /**
     * Processa o resultado do Google Sign-In
     *
     * Retorna pair de (idToken, email) ou null se houver erro
     */
    fun handleSignInResult(data: android.content.Intent?): Pair<String, String>? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account?.idToken != null && account.email != null) {
                Pair(account.idToken!!, account.email!!)
            } else {
                null
            }
        } catch (e: ApiException) {
            null
        }
    }

    /**
     * Fazer logout do Google
     */
    fun signOut() {
        googleSignInClient.signOut()
    }
}
```

---

## 🔐 PASSO 4: Atualizar LoginViewModel.kt

### Arquivo existente - Adicione este código

```kotlin
package com.example.takstud.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleSignInHelper: GoogleSignInHelper
) : ViewModel() {

    // ========================
    // PROFESSOR LOGIN STATE
    // ========================

    private val _teacherLoginLoading = MutableStateFlow(false)
    val teacherLoginLoading: StateFlow<Boolean> = _teacherLoginLoading.asStateFlow()

    private val _teacherLoginError = MutableStateFlow<String?>(null)
    val teacherLoginError: StateFlow<String?> = _teacherLoginError.asStateFlow()

    private val _teacherLoginSuccess = MutableStateFlow(false)
    val teacherLoginSuccess: StateFlow<Boolean> = _teacherLoginSuccess.asStateFlow()

    // ========================
    // RESPONSÁVEL LOGIN STATE
    // ========================

    private val _parentLoginRA = MutableStateFlow("")
    val parentLoginRA: StateFlow<String> = _parentLoginRA.asStateFlow()

    private val _parentLoginLoading = MutableStateFlow(false)
    val parentLoginLoading: StateFlow<Boolean> = _parentLoginLoading.asStateFlow()

    private val _parentLoginError = MutableStateFlow<String?>(null)
    val parentLoginError: StateFlow<String?> = _parentLoginError.asStateFlow()

    private val _parentLoginSuccess = MutableStateFlow(false)
    val parentLoginSuccess: StateFlow<Boolean> = _parentLoginSuccess.asStateFlow()

    // ========================
    // GOOGLE SIGN-IN
    // ========================

    /**
     * Obter intent para abrir Google Sign-In
     */
    fun getGoogleSignInIntent(): Intent {
        return googleSignInHelper.getSignInIntent()
    }

    /**
     * Processar resultado do Google Sign-In
     *
     * Chamado após usuário fazer login no Google
     */
    fun handleGoogleSignInResult(intent: Intent?) {
        val result = googleSignInHelper.handleSignInResult(intent)

        if (result == null) {
            _teacherLoginError.value = "❌ Erro ao fazer login com Google"
            return
        }

        val (idToken, email) = result
        loginTeacherWithGoogle(idToken, email)
    }

    /**
     * Login do professor com Google
     */
    private fun loginTeacherWithGoogle(idToken: String, email: String) {
        _teacherLoginLoading.value = true
        _teacherLoginError.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.loginTeacherWithGoogle(idToken, email)

                result.onSuccess {
                    _teacherLoginSuccess.value = true
                    _teacherLoginLoading.value = false
                }

                result.onFailure { error ->
                    _teacherLoginError.value = error.message
                    _teacherLoginLoading.value = false
                }
            } catch (e: Exception) {
                _teacherLoginError.value = "Erro: ${e.message}"
                _teacherLoginLoading.value = false
            }
        }
    }

    // ========================
    // LOGIN RESPONSÁVEL (RA)
    // ========================

    fun updateParentRA(ra: String) {
        _parentLoginRA.value = ra
    }

    fun loginParentWithRA() {
        val ra = _parentLoginRA.value.trim()

        if (ra.isBlank()) {
            _parentLoginError.value = "❌ Digite um RA válido"
            return
        }

        _parentLoginLoading.value = true
        _parentLoginError.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.loginParentWithRA(ra)

                result.onSuccess {
                    _parentLoginSuccess.value = true
                    _parentLoginLoading.value = false
                }

                result.onFailure { error ->
                    _parentLoginError.value = error.message
                    _parentLoginLoading.value = false
                }
            } catch (e: Exception) {
                _parentLoginError.value = "Erro: ${e.message}"
                _parentLoginLoading.value = false
            }
        }
    }

    // ========================
    // LOGOUT
    // ========================

    fun logout() {
        authRepository.logout()
        googleSignInHelper.signOut()
        resetStates()
    }

    private fun resetStates() {
        _parentLoginRA.value = ""
        _teacherLoginError.value = null
        _parentLoginError.value = null
        _teacherLoginSuccess.value = false
        _parentLoginSuccess.value = false
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

## 🎨 PASSO 5: Criar Composable de Google Button

### Arquivo: `app/src/main/java/com/example/takstud/ui/components/GoogleSignInButton.kt`

```kotlin
package com.example.takstud.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        modifier = Modifier.fillMaxWidth()
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

## 📱 PASSO 6: Atualizar LoginScreen.kt

### Arquivo existente - Substitua a tela de professor

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToTeacher: () -> Unit,
    onNavigateToParent: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Estados Professor
    val teacherLoading by viewModel.teacherLoginLoading.collectAsState()
    val teacherError by viewModel.teacherLoginError.collectAsState()
    val teacherSuccess by viewModel.teacherLoginSuccess.collectAsState()

    // Estados Responsável
    val parentRA by viewModel.parentLoginRA.collectAsState()
    val parentLoading by viewModel.parentLoginLoading.collectAsState()
    val parentError by viewModel.parentLoginError.collectAsState()
    val parentSuccess by viewModel.parentLoginSuccess.collectAsState()

    // Navegar após login
    if (teacherSuccess) {
        onNavigateToTeacher()
    }

    if (parentSuccess) {
        onNavigateToParent()
    }

    // Launcher para Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ========================
            // TAB 1: RESPONSÁVEL (RA)
            // ========================
            if (selectedTab == 0) {
                Text(
                    "Login - Responsável",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                TextField(
                    value = parentRA,
                    onValueChange = { viewModel.updateParentRA(it) },
                    label = { Text("RA do Aluno") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    enabled = !parentLoading
                )

                if (parentError != null) {
                    Text(
                        parentError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = { viewModel.loginParentWithRA() },
                    enabled = !parentLoading && parentRA.isNotBlank(),
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

            // ========================
            // TAB 2: PROFESSOR (GOOGLE)
            // ========================
            if (selectedTab == 1) {
                Text(
                    "Login - Professor",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (teacherError != null) {
                    Text(
                        teacherError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                GoogleSignInButton(
                    onClick = {
                        val intent = viewModel.getGoogleSignInIntent()
                        launcher.launch(intent)
                    },
                    isLoading = teacherLoading,
                    text = "Entrar com Google"
                )

                Text(
                    "Você receberá acesso automaticamente\nse seu email estiver autorizado",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========================
            // ABAS
            // ========================
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

## 🔧 PASSO 7: Configurar Firestore (lista de emails autorizados)

### No Firebase Console:

1. Vá para **Firestore Database**
2. Clique em **+** para criar nova coleção
3. Nome: `config`
4. Clique em **Auto ID** para criar novo documento (não mude)
5. No formulário que abriu:
   - Campo: `emails`
   - Tipo: **Array**
   - Valores: Adicione seus emails de professores

Exemplo:
```
[
  "maria@gmail.com",
  "joao@escola.com.br",
  "admin@takstud.com"
]
```

---

## 🔐 PASSO 8: Obter Web Client ID

1. Vá para [Google Cloud Console](https://console.cloud.google.com/)
2. Selecione seu projeto
3. Vá para **APIs & Services** → **Credentials**
4. Procure por **Web Client** na lista de credentials
5. Copie o **Client ID**
6. Em `GoogleSignInHelper.kt`, substitua:

```kotlin
.requestIdToken("COLE_SEU_WEB_CLIENT_ID_AQUI")
```

---

## ✅ Checklist de Implementação

- [ ] Passo 1: Adicionar dependências Firebase Auth
- [ ] Passo 2: Criar `AuthRepository.kt`
- [ ] Passo 3: Criar `GoogleSignInHelper.kt`
- [ ] Passo 4: Atualizar `LoginViewModel.kt`
- [ ] Passo 5: Criar `GoogleSignInButton.kt`
- [ ] Passo 6: Atualizar `LoginScreen.kt`
- [ ] Passo 7: Criar documento em Firestore
- [ ] Passo 8: Obter e configurar Web Client ID
- [ ] Testar login do professor com Google
- [ ] Testar login do aluno com RA

---

## 🐛 Se der erro...

### Erro: "Unable to find module with gradle path..."

**Solução:** Sincronize Gradle
```
Build → Clean Build Folder → Rebuild Project
```

### Erro: "Web Client ID inválido"

**Solução:** Você não preencheu a configuração. Vá no Passo 8

### Erro: "Email não autorizado"

**Solução:** Adicione seu email em Firestore → config → teacherEmails

### Erro: "RA não encontrado"

**Solução:** Verifique se o RA existe na base de dados

---

## 🎉 Pronto!

Seu app agora tem:
✅ Login de professor com Google (cadastro automático)
✅ Login de aluno com RA (simples)
✅ Segurança com lista de emails autorizado
✅ Sem senhas para lembrar

Aproveita! 🚀
