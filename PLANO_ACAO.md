# 🎯 PLANO DE AÇÃO - PRÓXIMAS SEMANAS

## 📅 SEMANA 1: VALIDAÇÃO INTEGRADA

### Dia 1-2: Integrar InputValidator em Formulários
**Tempo estimado:** 4-5 horas

#### Tarefa 1: LoginScreen
- [ ] Importar `InputValidator`
- [ ] Adicionar variável `raError`
- [ ] Validar RA ao digitar
- [ ] Mostrar mensagem de erro
- [ ] Desabilitar botão se inválido
- [ ] Testar no emulador

**Arquivo:** `ui/login/LoginScreen.kt`

```kotlin
// Adicionar ao OutlinedTextField:
onValueChange = {
    parentRA = it
    raError = if (!InputValidator.isValidRA(it)) {
        "RA deve ter 2-20 caracteres"
    } else null
},
isError = raError != null
```

#### Tarefa 2: AdminLoginScreen
- [ ] Validar código (4-10 dígitos)
- [ ] Usar `InputValidator.isValidAccessCode()`
- [ ] Mostrar feedback

#### Tarefa 3: AddTaskScreen
- [ ] Validar title
- [ ] Validar description
- [ ] Validar class
- [ ] Validar dueDate

**Validações a usar:**
```kotlin
InputValidator.isValidTitle(title)
InputValidator.isValidDescription(desc)
InputValidator.isValidClass(cls)
InputValidator.isValidDate(date)
```

### Dia 3-4: Adicionar Testes Básicos
**Tempo estimado:** 2-3 horas

#### Criar InputValidatorTest.kt
- [ ] Testar `isValidRA()`
- [ ] Testar `isValidEmail()`
- [ ] Testar `isValidScore()`
- [ ] Testar `sanitize()`

**Executar:** `./gradlew test`

### Dia 5: Review e Deploy
- [ ] Compilar `./gradlew build`
- [ ] Testar no emulador
- [ ] Validar erros aparecem
- [ ] Comit: "feat: integrar InputValidator em formulários"

---

## 📅 SEMANA 2: FIREBASE AUTHENTICATION

### Dia 1-2: Setup Firebase
**Tempo estimado:** 3-4 horas

#### Tarefa 1: Configurar Dependências
- [ ] Adicionar `firebase-auth-ktx` em `build.gradle.kts`
- [ ] Sincronizar Gradle
- [ ] Verificar compilação

**build.gradle.kts:**
```gradle
implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
```

#### Tarefa 2: Criar AuthRepository
- [ ] Criar arquivo `repository/AuthRepository.kt`
- [ ] Implementar `signUp()`, `signIn()`, `signOut()`
- [ ] Usar `Result` wrapper

**Estrutura:**
```kotlin
class AuthRepository {
    suspend fun signUp(email: String, password: String): Result<String> { }
    suspend fun signIn(email: String, password: String): Result<String> { }
    fun signOut() { }
}
```

### Dia 3-4: Integrar em ViewModel
**Tempo estimado:** 2-3 horas

- [ ] Criar `LoginViewModel`
- [ ] Adicionar estados com `StateFlow`
- [ ] Conectar AuthRepository
- [ ] Implementar funções: `loginAsParent()`, `loginAsTeacher()`, `logout()`

### Dia 5: Integrar em UI
**Tempo estimado:** 2 horas

- [ ] Atualizar `LoginScreen.kt` para usar ViewModel
- [ ] Adicionar loading indicator
- [ ] Mostrar erros com `Snackbar`
- [ ] Testar fluxo completo

---

## 📅 SEMANA 3: ROLES E PERMISSÕES

### Dia 1: Criar Estrutura
**Tempo estimado:** 2 horas

- [ ] Criar `model/UserRole.kt` (enum)
- [ ] Criar `util/PermissionManager.kt` (object)
- [ ] Extender `User` com role

```kotlin
enum class UserRole {
    TEACHER, PARENT, ADMIN
}

object PermissionManager {
    fun canCreateTask(role: UserRole) = role == UserRole.TEACHER
    fun canEditGrades(role: UserRole) = role == UserRole.TEACHER
    fun canViewAttendance(role: UserRole) = role in listOf(UserRole.TEACHER, UserRole.PARENT)
}
```

### Dia 2-3: Integrar em Views
**Tempo estimado:** 3-4 horas

- [ ] Atualizar `TeacherScreen` com verificações
- [ ] Atualizar `ParentScreen` com verificações
- [ ] Desabilitar/ocultar botões sem permissão
- [ ] Testar diferentes roles

### Dia 4-5: Firestore Security Rules
**Tempo estimado:** 2-3 horas

- [ ] Ir para Firebase Console
- [ ] Configurar Security Rules
- [ ] Testar rules
- [ ] Documentar rules

---

## 📅 SEMANA 4: QUALIDADE & TESTES

### Dia 1-2: Detekt
**Tempo estimado:** 2 horas

- [ ] Adicionar plugin Detekt ao `build.gradle.kts`
- [ ] Criar `detekt.yml`
- [ ] Executar: `./gradlew detekt`
- [ ] Resolver issues

### Dia 3-4: Testes UI
**Tempo estimado:** 3-4 horas

- [ ] Criar `LoginScreenTest.kt`
- [ ] Testar validação de RA
- [ ] Testar login flow
- [ ] Testar error display

### Dia 5: CI/CD
**Tempo estimado:** 2-3 horas

- [ ] Criar `.github/workflows/build.yml`
- [ ] Pipeline: Lint → Build → Test → Release
- [ ] Testar pipeline

---

## 📋 COMANDO RÁPIDO PARA CADA DIA

### Compilar
```bash
./gradlew build
```

### Testar
```bash
./gradlew test
```

### Testar UI
```bash
./gradlew connectedAndroidTest
```

### Lint
```bash
./gradlew lint
```

### Detekt
```bash
./gradlew detekt
```

### Build Release
```bash
./gradlew assembleRelease
```

### Executar Emulador
```bash
# Android Studio → Select Device → Run
# Ou via CLI: emulator @device_name
```

---

## 🎯 CHECKLIST GERAL

### Semana 1
- [ ] Validação integrada nos 4 formulários principais
- [ ] Testes para InputValidator
- [ ] Build limpo com testes passando

### Semana 2
- [ ] Firebase Auth funcionando
- [ ] Login com email/senha
- [ ] Sign out
- [ ] Error handling adequado

### Semana 3
- [ ] Roles implementado (TEACHER, PARENT)
- [ ] Permissões checadas em cada screen
- [ ] Firestore Security Rules configuradas
- [ ] Testes de acesso

### Semana 4
- [ ] Detekt 0 issues
- [ ] Testes UI passando
- [ ] CI/CD pipeline funcionando
- [ ] App pronto para beta

---

## 📚 RECURSOS PARA CADA SEMANA

### Semana 1
- `IMPLEMENTATION_GUIDE.md` - Seção 1
- Kotlin regex documentation
- Material3 TextField docs

### Semana 2
- `IMPLEMENTATION_GUIDE.md` - Seção 2
- Firebase Auth documentation
- Coroutines guide

### Semana 3
- `IMPLEMENTATION_GUIDE.md` - Seção 3
- Firebase Security Rules guide
- Android security best practices

### Semana 4
- `IMPLEMENTATION_GUIDE.md` - Seção 4-5
- Detekt documentation
- Android testing guide

---

## ⚠️ ARMADILHAS COMUNS

### Semana 1
❌ Esquecer de desabilitar botão enquanto tiver erro
✅ Usar `enabled = raError == null && parentRA.isNotEmpty()`

❌ Validar apenas ao clicar no botão
✅ Validar também ao digitar (onValueChange)

### Semana 2
❌ Esquec de usar `await()` em suspend functions
✅ Lembrar `.await()` em Firebase tasks

❌ Não usar `Result` wrapper
✅ Sempre envolver operações Firebase em try-catch

### Semana 3
❌ Esquecer de buscar role do usuário do Firebase
✅ Armazenar role como custom claim do usuário

### Semana 4
❌ Testes falhando por não resetar state
✅ Limpar state em `setUp()` cada teste

---

## 💾 COMMITS RECOMENDADOS

```
Semana 1:
git commit -m "feat: integrar InputValidator em formulários"
git commit -m "test: adicionar testes para InputValidator"

Semana 2:
git commit -m "feat: implementar Firebase Authentication"
git commit -m "feat: criar AuthRepository e LoginViewModel"

Semana 3:
git commit -m "feat: implementar sistema de roles e permissões"
git commit -m "feat: configurar Firestore Security Rules"

Semana 4:
git commit -m "chore: configurar Detekt e CI/CD"
git commit -m "test: adicionar testes UI com Compose"
```

---

## 📊 PROGRESSO ESPERADO

```
Início:  [██████░░░░░░░░░░░░░░░░░░░░] 30% Seguro
Semana1: [████████░░░░░░░░░░░░░░░░░░] 40%
Semana2: [██████████░░░░░░░░░░░░░░░░] 50%
Semana3: [██████████████░░░░░░░░░░░░] 65%
Semana4: [████████████████░░░░░░░░░░] 75%
PRONTO:  [██████████████████████████] 100% 🚀
```

---

## 🎓 LEARNING PATH

Se novo em alguma área:

### Firebase Auth
1. Ler: [Firebase Auth Docs](https://firebase.google.com/docs/auth)
2. Watch: Tutorial "Firebase Authentication Kotlin"
3. Praticar: Fazer login funcionar

### Kotlin Coroutines
1. Ler: [Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)
2. Entender: suspend, launch, async
3. Praticar: Usar em AuthRepository

### Android Testing
1. Ler: [Android Testing Guide](https://developer.android.com/training/testing)
2. Entender: Unit tests vs Instrumented tests
3. Praticar: Escrever primeiro teste

---

## 🚀 DEPOIS DE 4 SEMANAS

App TakStud estará:
✅ Seguro (autenticação real)
✅ Validado (entrada validada)
✅ Autorizado (roles & permissões)
✅ Testado (cobertura de testes)
✅ Otimizado (minificado, offline-ready)
✅ Pronto para produção

---

**BOA SORTE! 🎉**

Você tem tudo que precisa. Cada passo está documentado em `IMPLEMENTATION_GUIDE.md`.

Dúvidas? Consulte a documentação criada ou a seção apropriada do guia!

---

*Criado em 11 de Novembro de 2025*