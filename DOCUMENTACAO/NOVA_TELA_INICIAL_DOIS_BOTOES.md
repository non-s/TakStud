# 🎨 Nova Tela Inicial com 2 Botões

## Visão Geral

```
ANTIGA TELA:
┌─────────────────────────────────────┐
│  [TAB] Responsável  [TAB] Professor │
│                                     │
│  Digite RA:  [____________]        │
│  [ENTRAR]                           │
└─────────────────────────────────────┘

NOVA TELA:
┌─────────────────────────────────────┐
│                                     │
│        TakStud 📚                  │
│                                     │
│    [SOU PROFESSOR]                  │
│         OU                          │
│    [SOU ALUNO/RESPONSÁVEL]          │
│                                     │
└─────────────────────────────────────┘
```

---

## ✨ Fluxo Final

```
Tela Inicial
   │
   ├─ [SOU PROFESSOR]
   │    ↓
   │  Tela Login Professor
   │  ┌──────────────────────────┐
   │  │ Código de Acesso:       │
   │  │ [_________________]    │
   │  │ [ENTRAR]              │
   │  └──────────────────────────┘
   │    ↓
   │  TeacherScreen
   │
   └─ [SOU ALUNO/RESPONSÁVEL]
        ↓
      Tela Login Responsável
      ┌──────────────────────────┐
      │ RA do Aluno:            │
      │ [_________________]    │
      │ [ENTRAR]              │
      └──────────────────────────┘
        ↓
      ParentScreen
```

---

## 💻 PASSO 1: Criar HomeScreen (Tela Inicial)

### Arquivo: `app/src/main/java/com/example/takstud/ui/HomeScreen.kt`

```kotlin
package com.example.takstud.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onProfessorClick: () -> Unit,
    onAlunoClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Titulo
            Text(
                "TakStud",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Descrição
            Text(
                "Sistema de Gestão Acadêmica",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Botão Professor
            Button(
                onClick = onProfessorClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "SOU PROFESSOR",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "OU",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Aluno/Responsável
            Button(
                onClick = onAlunoClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    "SOU ALUNO/RESPONSÁVEL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Rodapé
            Text(
                "v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
```

---

## 💻 PASSO 2: Criar TeacherLoginScreen (Login Professor)

### Arquivo: `app/src/main/java/com/example/takstud/ui/login/TeacherLoginScreen.kt`

```kotlin
package com.example.takstud.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.takstud.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var codigoAcesso by remember { mutableStateOf("") }

    val isLoading by viewModel.teacherLoginLoading.collectAsState()
    val error by viewModel.teacherLoginError.collectAsState()
    val success by viewModel.teacherLoginSuccess.collectAsState()

    if (success) {
        onLoginSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login - Professor") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Titulo
            Text(
                "Acesso Professor",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Descricao
            Text(
                "Digite seu código de acesso para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Campo Código
            TextField(
                value = codigoAcesso,
                onValueChange = { codigoAcesso = it },
                label = { Text("Código de Acesso") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading,
                singleLine = true
            )

            // Mensagem de Erro
            if (error != null) {
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Entrar
            Button(
                onClick = {
                    viewModel.loginTeacherWithAccessCode(codigoAcesso)
                },
                enabled = !isLoading && codigoAcesso.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLoading) {
                    Text("Verificando...")
                } else {
                    Text("ENTRAR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info
            Text(
                "Peça o código ao administrador",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
```

---

## 💻 PASSO 3: Criar ParentLoginScreen (Login Aluno/Responsável)

### Arquivo: `app/src/main/java/com/example/takstud/ui/login/ParentLoginScreen.kt`

```kotlin
package com.example.takstud.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.takstud.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentLoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var ra by remember { mutableStateOf("") }

    val isLoading by viewModel.parentLoginLoading.collectAsState()
    val error by viewModel.parentLoginError.collectAsState()
    val success by viewModel.parentLoginSuccess.collectAsState()

    if (success) {
        onLoginSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login - Responsável") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Titulo
            Text(
                "Acesso Responsável",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Descricao
            Text(
                "Digite o RA do seu filho para acessar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Campo RA
            TextField(
                value = ra,
                onValueChange = { ra = it },
                label = { Text("RA do Aluno") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = !isLoading,
                singleLine = true
            )

            // Mensagem de Erro
            if (error != null) {
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Entrar
            Button(
                onClick = {
                    viewModel.loginParentWithRA(ra)
                },
                enabled = !isLoading && ra.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLoading) {
                    Text("Verificando...")
                } else {
                    Text("ENTRAR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info
            Text(
                "O RA está no cartão do aluno",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
```

---

## 🗺️ PASSO 4: Atualizar Navegação (TakStudNavGraph.kt)

### Arquivo: Adicione essas rotas

```kotlin
package com.example.takstud

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.takstud.ui.HomeScreen
import com.example.takstud.ui.login.TeacherLoginScreen
import com.example.takstud.ui.login.ParentLoginScreen
import com.example.takstud.ui.teacher.TeacherScreen
import com.example.takstud.ui.parent.ParentScreen

// Defina as rotas
object TakStudDestinations {
    const val HOME = "home"
    const val TEACHER_LOGIN = "teacher_login"
    const val PARENT_LOGIN = "parent_login"
    const val TEACHER = "teacher"
    const val PARENT = "parent"
}

@Composable
fun TakStudNavGraph(
    navController: NavHostController,
    startDestination: String = TakStudDestinations.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Tela Inicial (Home)
        composable(TakStudDestinations.HOME) {
            HomeScreen(
                onProfessorClick = {
                    navController.navigate(TakStudDestinations.TEACHER_LOGIN)
                },
                onAlunoClick = {
                    navController.navigate(TakStudDestinations.PARENT_LOGIN)
                }
            )
        }

        // Tela Login Professor
        composable(TakStudDestinations.TEACHER_LOGIN) {
            TeacherLoginScreen(
                onLoginSuccess = {
                    navController.navigate(TakStudDestinations.TEACHER) {
                        popUpTo(TakStudDestinations.HOME)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Tela Login Responsável
        composable(TakStudDestinations.PARENT_LOGIN) {
            ParentLoginScreen(
                onLoginSuccess = {
                    navController.navigate(TakStudDestinations.PARENT) {
                        popUpTo(TakStudDestinations.HOME)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Tela Professor
        composable(TakStudDestinations.TEACHER) {
            TeacherScreen(
                onLogout = {
                    navController.navigate(TakStudDestinations.HOME) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Tela Responsável
        composable(TakStudDestinations.PARENT) {
            ParentScreen(
                onLogout = {
                    navController.navigate(TakStudDestinations.HOME) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
```

---

## 🎨 PASSO 5: Atualizar MainActivity.kt

### Arquivo: Atualize o NavHost

```kotlin
package com.example.takstud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.takstud.ui.theme.TakStudTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TakStudTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    TakStudNavGraph(navController)
                }
            }
        }
    }
}
```

---

## 🎯 PASSO 6: Atualizar LoginViewModel

### Adicione este método para login com código de acesso

```kotlin
fun loginTeacherWithAccessCode(code: String) {
    _teacherLoginLoading.value = true
    _teacherLoginError.value = null

    viewModelScope.launch {
        try {
            val result = authRepository.validateTeacherAccessCode(code)

            result.onSuccess {
                _teacherLoginSuccess.value = true
                _teacherLoginLoading.value = false
            }

            result.onFailure { error ->
                _teacherLoginError.value = error.message
                _teacherLoginLoading.value = false
            }
        } catch (e: Exception) {
            _teacherLoginError.value = "Erro ao fazer login"
            _teacherLoginLoading.value = false
        }
    }
}
```

---

## 📝 PASSO 7: Atualizar AuthRepository

### Adicione este método para validar código

```kotlin
suspend fun validateTeacherAccessCode(code: String): Result<Map<String, Any>> = try {
    // Buscar código correto do Remote Config ou Firestore
    val snapshot = db.collection("config")
        .document("teacherAccess")
        .get()
        .await()

    val correctCode = snapshot.getString("code") ?: "58239617"

    if (code == correctCode) {
        // Criar sessão anônima
        val authResult = auth.signInAnonymously().await()
        val uid = authResult.user?.uid ?: throw Exception("Erro ao criar sessão")

        // Criar documento do professor
        val teacherData = mapOf(
            "uid" to uid,
            "role" to "TEACHER",
            "loginTime" to System.currentTimeMillis()
        )

        db.collection("teachers").document(uid).set(teacherData).await()

        Result.success(teacherData)
    } else {
        Result.failure(Exception("Código de acesso inválido"))
    }
} catch (e: Exception) {
    Result.failure(e)
}
```

---

## ✅ Checklist de Implementação

- [ ] Criar `HomeScreen.kt`
- [ ] Criar `TeacherLoginScreen.kt`
- [ ] Criar `ParentLoginScreen.kt`
- [ ] Atualizar `TakStudNavGraph.kt`
- [ ] Atualizar `MainActivity.kt`
- [ ] Atualizar `LoginViewModel.kt`
- [ ] Atualizar `AuthRepository.kt`
- [ ] Compilar projeto
- [ ] Testar fluxo completo

---

## 🎬 Fluxo Final Esperado

```
App abre
    ↓
HomeScreen (2 botões grandes)
    │
    ├─ [SOU PROFESSOR]
    │    ↓
    │  TeacherLoginScreen
    │  Código: [58239617]
    │    ↓
    │  TeacherScreen ✅
    │
    └─ [SOU ALUNO/RESPONSÁVEL]
         ↓
       ParentLoginScreen
       RA: [123456]
         ↓
       ParentScreen ✅
```

---

## 🎨 Customizações Possíveis

### Mudar cores dos botões

```kotlin
// HomeScreen.kt
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF1976D2),  // Azul
        contentColor = Color.White
    )
)
```

### Mudar tamanho da fonte do titulo

```kotlin
Text(
    "TakStud",
    fontSize = 56.sp  // Mudar para 56, 64, etc
)
```

### Adicionar logo/imagem

```kotlin
Image(
    painter = painterResource(id = R.drawable.logo),
    contentDescription = "Logo",
    modifier = Modifier.size(100.dp)
)
```

---

## ✨ Resultado Visual

```
┌──────────────────────────────────────┐
│                                      │
│         TakStud 📚                  │
│                                      │
│  Sistema de Gestão Acadêmica         │
│                                      │
│                                      │
│      ┌──────────────────────┐       │
│      │ SOU PROFESSOR        │       │
│      └──────────────────────┘       │
│                                      │
│              OU                      │
│                                      │
│      ┌──────────────────────┐       │
│      │ SOU ALUNO/RESPONSÁVEL│       │
│      └──────────────────────┘       │
│                                      │
│            v1.0                      │
│                                      │
└──────────────────────────────────────┘
```

---

## 🚀 Próxima Etapa

1. Copie os 3 arquivos de tela (Home, TeacherLogin, ParentLogin)
2. Atualize a navegação
3. Compile e teste

Pronto! Tela inicial bem mais intuitiva! ✅
