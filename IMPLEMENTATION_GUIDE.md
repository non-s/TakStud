# 📖 GUIA DE IMPLEMENTAÇÃO DAS MELHORIAS

Guia prático para implementar as melhorias restantes no projeto TakStud.

## 1. INTEGRAR INPUTVALIDATOR NOS FORMULÁRIOS

### 1.1 LoginScreen - Validar RA

**Arquivo:** `app/src/main/java/com/example/takstud/ui/login/LoginScreen.kt`

**Padrão a implementar:**

```kotlin
import com.example.takstud.util.InputValidator

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onTeacherLogin: () -> Unit,
    onParentLogin: (String) -> Unit,
    onAdminLogin: () -> Unit
) {
    var parentRA by remember { mutableStateOf("") }
    var raError by remember { mutableStateOf<String?>(null) }  // ← Novo

    Column(...) {
        OutlinedTextField(
            value = parentRA,
            onValueChange = {
                parentRA = it
                // Validar ao digitar
                raError = if (!InputValidator.isValidRA(it)) {
                    "RA deve ter 2-20 caracteres (letras, números, - e _)"
                } else null
            },
            label = { Text("RA do Aluno") },
            isError = raError != null,  // ← Novo: mostrar erro visualmente
            modifier = Modifier.fillMaxWidth()
        )

        if (raError != null) {
            Text(raError!!, color = Color.Red, fontSize = 12.sp)  // ← Novo: mensagem de erro
        }

        Button(
            onClick = {
                // Validar antes de fazer login
                if (InputValidator.isValidRA(parentRA)) {
                    onParentLogin(parentRA)
                } else {
                    raError = "RA inválido"
                }
            },
            enabled = raError == null && parentRA.isNotEmpty()  // ← Novo: desabilitar se inválido
        ) { Text("Entrar") }
    }
}
```

### 1.2 AdminLoginScreen - Validar Código

**Arquivo:** `app/src/main/java/com/example/takstud/ui/login/AdminLoginScreen.kt`

**Implementar:**

```kotlin
var adminCode by remember { mutableStateOf("") }
var codeError by remember { mutableStateOf<String?>(null) }

// Validação:
if (!InputValidator.isValidAccessCode(adminCode)) {
    codeError = "Código deve ter 4-10 dígitos"
}
```

### 1.3 AddTaskScreen - Validar Campos

**Arquivo:** `app/src/main/java/com/example/takstud/ui/teacher/AddTaskScreen.kt`

**Implementar validação para:**
- Title: `InputValidator.isValidTitle(title)`
- Description: `InputValidator.isValidDescription(description)`
- Class: `InputValidator.isValidClass(studentClass)`
- DueDate: `InputValidator.isValidDate(dueDate)`

---

## 2. IMPLEMENTAR FIREBASE AUTHENTICATION

### 2.1 Atualizar build.gradle.kts

```gradle
dependencies {
    // Adicionar Firebase Auth
    implementation(libs.firebase.auth.ktx)
}
```

### 2.2 Atualizar libs.versions.toml

```toml
[versions]
firebase-auth = "23.0.0"

[libraries]
firebase-auth-ktx = { group = "com.google.firebase", name = "firebase-auth-ktx", version.ref = "firebase-auth" }
```

### 2.3 Criar AuthRepository

**Arquivo:** `app/src/main/java/com/example/takstud/repository/AuthRepository.kt`

```kotlin
import com.google.firebase.auth.FirebaseAuth
import com.example.takstud.util.Result
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}
```

### 2.4 Atualizar LoginScreen para usar AuthRepository

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),  // ← Novo
    onTeacherLogin: () -> Unit,
    onParentLogin: () -> Unit,
    onAdminLogin: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observar estado do login
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is Result.Success -> onParentLogin()
            is Result.Error -> errorMessage = loginState.exception.message
            is Result.Loading -> isLoading = true
        }
    }

    // UI com validação
    Column {
        // ... campos de entrada

        if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red)
        }

        Button(
            onClick = { viewModel.loginAsParent(ra) },
            enabled = !isLoading && InputValidator.isValidRA(ra)
        ) {
            if (isLoading) CircularProgressIndicator()
            else Text("Entrar")
        }
    }
}
```

---

## 3. SISTEMA DE ROLES E PERMISSÕES

### 3.1 Criar Enum de Roles

**Arquivo:** `app/src/main/java/com/example/takstud/model/UserRole.kt`

```kotlin
enum class UserRole {
    TEACHER,
    PARENT,
    ADMIN
}
```

### 3.2 Estender Modelo de Usuário

```kotlin
data class User(
    val id: String,
    val email: String,
    val role: UserRole,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 3.3 Criar PermissionManager

**Arquivo:** `app/src/main/java/com/example/takstud/util/PermissionManager.kt`

```kotlin
object PermissionManager {
    fun canCreateTask(userRole: UserRole): Boolean = userRole == UserRole.TEACHER
    fun canEditGrades(userRole: UserRole): Boolean = userRole == UserRole.TEACHER
    fun canViewAttendance(userRole: UserRole): Boolean = userRole in listOf(UserRole.TEACHER, UserRole.PARENT)
    fun canViewGrades(userRole: UserRole): Boolean = userRole in listOf(UserRole.TEACHER, UserRole.PARENT)
}
```

### 3.4 Usar PermissionManager nas Screens

```kotlin
@Composable
fun TeacherScreen(
    userRole: UserRole,
    // ...
) {
    Column {
        if (PermissionManager.canCreateTask(userRole)) {
            Button(onClick = onManageTasks) { Text("Gerenciar Atividades") }
        } else {
            Text("Sem permissão para gerenciar atividades")
        }
    }
}
```

---

## 4. ADICIONAR TESTES UNITÁRIOS

### 4.1 Testar InputValidator

**Arquivo:** `app/src/test/java/com/example/takstud/util/InputValidatorTest.kt`

```kotlin
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun testValidRA() {
        assertTrue(InputValidator.isValidRA("001"))
        assertTrue(InputValidator.isValidRA("ALU-001"))
        assertFalse(InputValidator.isValidRA(""))
        assertFalse(InputValidator.isValidRA("a")) // menor que 2
    }

    @Test
    fun testValidEmail() {
        assertTrue(InputValidator.isValidEmail("test@example.com"))
        assertFalse(InputValidator.isValidEmail("invalid-email"))
        assertFalse(InputValidator.isValidEmail(""))
    }

    @Test
    fun testValidScore() {
        assertTrue(InputValidator.isValidScore("85.5"))
        assertTrue(InputValidator.isValidScore("100"))
        assertFalse(InputValidator.isValidScore("150"))
        assertFalse(InputValidator.isValidScore("invalid"))
    }
}
```

### 4.2 Testar Result

**Arquivo:** `app/src/test/java/com/example/takstud/util/ResultTest.kt`

```kotlin
import org.junit.Test

class ResultTest {

    @Test
    fun testSuccessResult() {
        val result = Result.Success(42)
        assertTrue(result.isSuccess())
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun testErrorResult() {
        val exception = Exception("Test error")
        val result = Result.Error(exception)
        assertTrue(result.isError())
        assertNull(result.getOrNull())
    }

    @Test
    fun testMapTransformation() {
        val result = Result.Success(5)
        val mapped = result.map { it * 2 }
        assertEquals(10, (mapped as Result.Success).data)
    }
}
```

---

## 5. CONFIGURAR DETEKT

### 5.1 Atualizar build.gradle.kts

```gradle
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

detekt {
    config = files("detekt.yml")
    baseline = file("detekt-baseline.xml")
}
```

### 5.2 Criar detekt.yml

**Arquivo:** `detekt.yml`

```yaml
build:
  maxIssues: 0
  excludeCorrectable: false

style:
  active: true
  MagicNumber:
    active: true
    ignoreNumbers: [0, 1, 2, -1, -2]
  UnusedImports:
    active: true

complexity:
  active: true
  TooManyFunctions:
    thresholdInClasses: 15

comments:
  active: true
  CommentOverPrivateProperty:
    active: true
```

### 5.3 Executar Detekt

```bash
./gradlew detekt
```

---

## 6. IMPLEMENTAR ROOM DATABASE

### 6.1 Atualizar build.gradle.kts

```gradle
dependencies {
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
}
```

### 6.2 Criar Entidades Room

**Arquivo:** `app/src/main/java/com/example/takstud/database/entity/TaskEntity.kt`

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val studentClass: String,
    val syncedAt: Long = System.currentTimeMillis()
)
```

### 6.3 Criar DAO

```kotlin
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE studentClass = :className")
    fun getTasksByClass(className: String): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
```

---

## 7. FEEDBACK VISUAL - LOADING & ERRORS

### 7.1 Criar Composable de Loading

**Arquivo:** `app/src/main/java/com/example/takstud/ui/components/LoadingIndicator.kt`

```kotlin
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
```

### 7.2 Usar em Screens

```kotlin
@Composable
fun TaskListScreen(viewModel: TaskViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    when {
        isLoading -> LoadingIndicator()
        error != null -> ErrorMessage(error!!)
        else -> TaskList(tasks)
    }
}
```

---

## 8. FIRESTORE SECURITY RULES

### 8.1 Configurar Rules

**Firebase Console → Firestore → Rules**

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Permitir leitura de tarefas para usuários autenticados
    match /tasks/{document=**} {
      allow read: if request.auth != null;
      allow create, update, delete: if request.auth.token.role == 'TEACHER';
    }

    // Permitir notas apenas para professores
    match /grades/{document=**} {
      allow read: if request.auth != null;
      allow create, update, delete: if request.auth.token.role == 'TEACHER';
    }

    // Responsáveis só veem seus próprios dados
    match /attendance/{userId}/records/{document=**} {
      allow read: if request.auth.uid == userId || request.auth.token.role == 'TEACHER';
      allow write: if request.auth.token.role == 'TEACHER';
    }
  }
}
```

---

## 📋 CHECKLIST DE IMPLEMENTAÇÃO

- [ ] Integrar InputValidator em LoginScreen
- [ ] Integrar InputValidator em AdminLoginScreen
- [ ] Integrar InputValidator em AddTaskScreen
- [ ] Implementar Firebase Authentication
- [ ] Criar AuthRepository
- [ ] Criar enum UserRole
- [ ] Implementar PermissionManager
- [ ] Adicionar testes unitários (InputValidator, Result)
- [ ] Configurar Detekt
- [ ] Executar Detekt com sucesso
- [ ] Implementar Room Database
- [ ] Adicionar Feedback visual (loading)
- [ ] Configurar Firestore Security Rules
- [ ] Testar segurança end-to-end

---

## 🎯 PRÓXIMOS PASSOS

1. **Esta semana:**
   - Integrar InputValidator (2-3 horas)
   - Adicionar testes básicos (2-3 horas)

2. **Próxima semana:**
   - Firebase Authentication (4-6 horas)
   - Roles e permissões (2-3 horas)

3. **Semana 3:**
   - Room Database (3-4 horas)
   - Testes UI (3-4 horas)

---

**Última atualização:** 11 de Novembro de 2025