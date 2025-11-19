# 💻 Código Pronto - Tela Inicial com 2 Botões

## 📋 Resumo Rápido

```
NOVO FLUXO:
HomeScreen (2 botões)
    ├─ SOU PROFESSOR → TeacherLoginScreen → Código → TeacherScreen
    └─ SOU ALUNO → ParentLoginScreen → RA → ParentScreen
```

---

## 🔥 ARQUIVO 1: HomeScreen.kt

### Caminho: `app/src/main/java/com/example/takstud/ui/HomeScreen.kt`

```kotlin
package com.example.takstud.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Descrição
            Text(
                "Sistema de Gestão Acadêmica",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "OU",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

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

## 🔥 ARQUIVO 2: TeacherLoginScreen.kt

### Caminho: `app/src/main/java/com/example/takstud/ui/login/TeacherLoginScreen.kt`

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

## 🔥 ARQUIVO 3: ParentLoginScreen.kt

### Caminho: `app/src/main/java/com/example/takstud/ui/login/ParentLoginScreen.kt`

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

## 🔥 ARQUIVO 4: TakStudNavGraph.kt (ATUALIZADO)

### Caminho: `app/src/main/java/com/example/takstud/TakStudNavGraph.kt`

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
        // TELA INICIAL
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

        // TELA LOGIN PROFESSOR
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

        // TELA LOGIN RESPONSÁVEL
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

        // TELA PROFESSOR
        composable(TakStudDestinations.TEACHER) {
            TeacherScreen(
                onLogout = {
                    navController.navigate(TakStudDestinations.HOME) {
                        popUpTo(0)
                    }
                }
            )
        }

        // TELA RESPONSÁVEL
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

## 🔥 ARQUIVO 5: MainActivity.kt (ATUALIZADO)

### Caminho: `app/src/main/java/com/example/takstud/MainActivity.kt`

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

## 🔥 ARQUIVO 6: LoginViewModel.kt (ADICIONE ESTE MÉTODO)

### Adicione ao seu arquivo existente:

```kotlin
// Adicione este método à classe LoginViewModel

fun loginTeacherWithAccessCode(code: String) {
    _teacherLoginLoading.value = true
    _teacherLoginError.value = null

    viewModelScope.launch {
        try {
            val result = studentAuthRepository.validateTeacherAccessCode(code)

            result.onSuccess {
                _teacherLoginSuccess.value = true
                _teacherLoginLoading.value = false
            }

            result.onFailure { error ->
                _teacherLoginError.value = error.message ?: "Código inválido"
                _teacherLoginLoading.value = false
            }
        } catch (e: Exception) {
            _teacherLoginError.value = "Erro: ${e.message}"
            _teacherLoginLoading.value = false
        }
    }
}

fun loginParentWithRA(ra: String) {
    val raLimpo = ra.trim()

    if (raLimpo.isBlank()) {
        _parentLoginError.value = "Digite um RA válido"
        return
    }

    _parentLoginLoading.value = true
    _parentLoginError.value = null

    viewModelScope.launch {
        try {
            val result = studentAuthRepository.loginParentWithRA(raLimpo)

            result.onSuccess {
                _parentLoginSuccess.value = true
                _parentLoginLoading.value = false
            }

            result.onFailure { error ->
                _parentLoginError.value = error.message ?: "RA não encontrado"
                _parentLoginLoading.value = false
            }
        } catch (e: Exception) {
            _parentLoginError.value = "Erro: ${e.message}"
            _parentLoginLoading.value = false
        }
    }
}
```

---

## ✅ Checklist de Implementação

- [ ] Criar `HomeScreen.kt`
- [ ] Criar `TeacherLoginScreen.kt`
- [ ] Criar `ParentLoginScreen.kt`
- [ ] Atualizar `TakStudNavGraph.kt`
- [ ] Atualizar `MainActivity.kt`
- [ ] Adicionar métodos em `LoginViewModel.kt`
- [ ] Compilar sem erros
- [ ] Testar fluxo completo

---

## 🧪 Como Testar

1. Abra o app
2. Você vê a tela com 2 botões grandes
3. Clique em "SOU PROFESSOR"
4. Digite código: `58239617`
5. Clique em "ENTRAR"
6. Você deve ver a TeacherScreen ✅
7. Volte (botão voltar)
8. Clique em "SOU ALUNO/RESPONSÁVEL"
9. Digite um RA que existe (ex: 001)
10. Você deve ver a ParentScreen ✅

---

## 🎨 Resultado Visual

```
TELA INICIAL (HomeScreen)
┌─────────────────────────────────┐
│                                 │
│       TakStud 📚               │
│  Sistema de Gestão Acadêmica   │
│                                 │
│  ┌───────────────────────────┐ │
│  │   SOU PROFESSOR           │ │
│  └───────────────────────────┘ │
│                                 │
│           OU                    │
│                                 │
│  ┌───────────────────────────┐ │
│  │ SOU ALUNO/RESPONSÁVEL     │ │
│  └───────────────────────────┘ │
│                                 │
│            v1.0                 │
└─────────────────────────────────┘

TELA LOGIN PROFESSOR (TeacherLoginScreen)
┌─────────────────────────────────┐
│ ← Login - Professor             │
├─────────────────────────────────┤
│        Acesso Professor         │
│                                 │
│ Digite seu código de acesso    │
│                                 │
│ Código: [______________]       │
│                                 │
│ ┌───────────────────────────┐ │
│ │      ENTRAR              │ │
│ └───────────────────────────┘ │
│                                 │
│  Peça o código ao admin        │
└─────────────────────────────────┘

TELA LOGIN RESPONSÁVEL (ParentLoginScreen)
┌─────────────────────────────────┐
│ ← Login - Responsável           │
├─────────────────────────────────┤
│       Acesso Responsável        │
│                                 │
│ Digite o RA do seu filho       │
│                                 │
│ RA: [______________]           │
│                                 │
│ ┌───────────────────────────┐ │
│ │      ENTRAR              │ │
│ └───────────────────────────┘ │
│                                 │
│  RA está no cartão do aluno   │
└─────────────────────────────────┘
```

---

## 🎉 Pronto!

Seu app agora tem:

✅ Tela inicial com 2 botões grandes
✅ Fluxo professor separado (código de acesso)
✅ Fluxo aluno/responsável separado (RA)
✅ Transições suaves entre telas
✅ Botão voltar em cada login

Aproveita! 🚀
