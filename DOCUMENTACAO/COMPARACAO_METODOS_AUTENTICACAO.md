# 🔐 Comparação: Métodos de Autenticação de Professor

## Seu Dúvida Específica

> "Se eu colocar login com email do Google, eu tenho que cadastrar o professor antes, ou ele faz sozinho o cadastro?"

**Resposta:** Depende da estratégia escolhida. Aqui estão as **3 opções** que você pode usar:

---

## 📌 OPÇÃO 1: Cadastro Automático (RECOMENDADO ✅)

### Como Funciona

1. Professor clica "Entrar com Google"
2. Google abre janela de login
3. Professor faz login com sua conta Google
4. ✅ **App cria conta automaticamente no Firebase**
5. ✅ **App cria documento no Firestore**
6. ✅ Professor entra direto, sem você fazer nada!

### Código Exemplo

```kotlin
// Usuario loga com Google, Firebase cria a conta automaticamente
val result = auth.signInWithCredential(googleCredential).await()
val uid = result.user?.uid  // Firebase já criou a conta aqui
```

### Pros & Contras

| Pros | Contras |
|------|---------|
| ✅ Qualquer pessoa com Google pode logar | ❌ Inseguro - qualquer pessoa entra |
| ✅ Zero configuração no admin | ⚠️ Precisa validar email depois |
| ✅ Rápido | |

### Quando Usar

- ✅ Teste/desenvolvimento
- ✅ Grupo pequeno de professores confiáveis
- ❌ Não é seguro para produção

---

## 📌 OPÇÃO 2: Lista de Emails Autorizados (SUPER RECOMENDADO ⭐)

### Como Funciona

1. Você cadastra emails autorizados em **um documento Firestore**
2. Professor tenta logar com Google
3. App verifica: "Este email está autorizado?"
4. ✅ Se SIM → Cria conta automaticamente
5. ❌ Se NÃO → Rejeita e mostra erro

### Código Exemplo

```kotlin
// Firestore document: config/teacherEmails
{
  "emails": [
    "maria@gmail.com",
    "joao@escola.com.br",
    "admin@takstud.com"
  ]
}

// No login, verifica:
suspend fun isEmailTeacherAuthorized(email: String): Boolean {
    val snapshot = db.collection("config")
        .document("teacherEmails")
        .get()
        .await()

    val authorizedEmails = snapshot.get("emails") as List<String>
    return email in authorizedEmails  // Retorna true/false
}
```

### Fluxo Detalhado

```
Professor clica "Entrar com Google"
    ↓
Google: "Qual seu email?"
Professor: maria@gmail.com
    ↓
Firebase cria conta
    ↓
App verifica em Firestore
    ↓
config/teacherEmails contém maria@gmail.com?
    ↓
SIM ✅ → Entra direto
NÃO ❌ → "Email não autorizado"
```

### Pros & Contras

| Pros | Contras |
|------|---------|
| ✅ Seguro - controla quem entra | 📋 Precisa manter lista atualizada |
| ✅ Cadastro automático | (Mas é facil editar um documento) |
| ✅ Zero friction para professor |  |
| ✅ Adicionar professor = editar um JSON | |

### Quando Usar

- ✅ **RECOMENDADO PARA PRODUÇÃO**
- ✅ Escola com 5-100 professores
- ✅ Você controla os emails

---

## 📌 OPÇÃO 3: Pré-Cadastro Manual (Admin Cria Conta)

### Como Funciona

1. **Você (admin) cria manualmente** cada conta de professor
2. Envia email com senha temporária
3. Professor loga com email + senha
4. Muda para usar Google Sign-In depois (opcional)

### Código Exemplo

```kotlin
// Admin cria conta manualmente
fun createTeacherAccount(email: String, password: String) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->
            val uid = result.user?.uid
            // Cria documento no Firestore
            db.collection("teachers").document(uid!!).set(
                mapOf("email" to email, "role" to "TEACHER")
            )
        }
}

// Professor loga com email + senha
fun loginWithEmailPassword(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password)
}
```

### Pros & Contras

| Pros | Contras |
|------|---------|
| ✅ Muito seguro | ❌ Você tem muito trabalho |
| ✅ Completo controle | ❌ Professor precisa lembrar senha |
| | ❌ Resets de senha chatos |
| | ❌ Mais suporte necessário |

### Quando Usar

- ❌ Não recomendado (é chato)
- ✅ Apenas se você quer controle máximo

---

## 🎯 Minha Recomendação

### Para Seu Caso: **OPÇÃO 2 (Lista de Emails)** ⭐⭐⭐⭐⭐

```
┌─────────────────────────────────────────┐
│  SEU FLUXO IDEAL                        │
└─────────────────────────────────────────┘

1. SETUP (uma vez)
   ✅ Cadastrar lista de emails em Firestore

2. PARA CADA PROFESSOR NOVO
   ❌ Não precisa fazer nada!
   Professor:
     1. Clica "Entrar com Google"
     2. Faz login com Google
     3. ✅ Entra direto!
   Você:
     1. Adiciona email na lista
     2. Pronto!

3. ALUNO/RESPONSÁVEL (já funciona)
   ✅ Continua com RA (não muda nada)
```

### Por que essa opção?

✅ **Segurança:** Controla quem pode entrar
✅ **Facilidade:** Professor entra automaticamente
✅ **Escalabilidade:** Adiciona/remove editando um JSON
✅ **Zero fricção:** Sem senhas pra lembrar
✅ **Profissional:** Como apps reais fazem

---

## 📋 Passo a Passo Implementação (Opção 2)

### Passo 1: No Firebase Console

1. Vá para **Firestore Database**
2. Crie uma coleção chamada `config`
3. Crie um documento chamado `teacherEmails`
4. Adicione um campo `emails` (array) com seus professores:

```json
{
  "emails": ["maria@gmail.com", "joao@escola.com.br"]
}
```

### Passo 2: No Android Studio

Adicione este arquivo:

**Arquivo: `AuthRepository.kt`**

```kotlin
package com.example.takstud

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    /**
     * Login de professor com Google
     *
     * Se o email estiver na lista de autorizados,
     * cria conta automaticamente
     */
    suspend fun loginTeacherWithGoogle(
        idToken: String,
        email: String
    ): Result<Map<String, Any>> = try {
        // 1. Autenticar com Google
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val uid = authResult.user?.uid ?: throw Exception("UID não obtido")

        // 2. Verificar se email está autorizado
        if (!isEmailAuthorized(email)) {
            auth.signOut()  // Faz logout se não está autorizado
            throw Exception("Email não está na lista de professores autorizados")
        }

        // 3. Criar documento do professor
        val teacherData = mapOf(
            "uid" to uid,
            "email" to email,
            "name" to (authResult.user?.displayName ?: ""),
            "role" to "TEACHER",
            "photoUrl" to (authResult.user?.photoUrl?.toString() ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("teachers").document(uid).set(teacherData).await()

        // 4. Retornar sucesso
        Result.success(teacherData)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Verifica se email está na lista autorizada
     */
    private suspend fun isEmailAuthorized(email: String): Boolean = try {
        val snapshot = db.collection("config")
            .document("teacherEmails")
            .get()
            .await()

        @Suppress("UNCHECKED_CAST")
        val authorizedEmails = snapshot.get("emails") as? List<String> ?: emptyList()

        return email in authorizedEmails
    } catch (e: Exception) {
        // Se houver erro ao buscar, nega acesso (seguro)
        false
    }

    /**
     * Login de responsável com RA
     */
    suspend fun loginParentWithRA(studentRA: String): Result<Map<String, Any>> = try {
        // Buscar aluno no Firestore
        val snapshot = db.collection("students")
            .whereEqualTo("ra", studentRA)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            throw Exception("RA não encontrado")
        }

        // Fazer login anônimo
        val authResult = auth.signInAnonymously().await()
        val uid = authResult.user?.uid ?: throw Exception("Erro ao criar sessão")

        // Criar documento do responsável
        val parentData = mapOf(
            "uid" to uid,
            "studentRa" to studentRA,
            "role" to "PARENT",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("parents").document(uid).set(parentData).await()

        Result.success(parentData)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() {
        auth.signOut()
    }
}
```

### Passo 3: No LoginScreen

```kotlin
// Ao usuário clicar em "Entrar com Google"
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    try {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = task.getResult(ApiException::class.java)

        if (account != null) {
            // Chamar função do ViewModel
            viewModel.loginTeacherWithGoogle(
                idToken = account.idToken!!,
                email = account.email!!
            )
        }
    } catch (e: Exception) {
        // Erro no login
    }
}
```

---

## 🚨 Importante: Setup Inicial (Tudo que você precisa fazer UMA VEZ)

### 1. Ativar Firebase Authentication

```
Firebase Console → Authentication → Sign-in method
✅ Email/Password
✅ Google
```

### 2. Configurar Google Sign-In

```
Google Cloud Console → APIs & Services → OAuth 2.0 IDs
Copiar: Web Client ID
```

### 3. Criar documento no Firestore

```
Firestore Database → Create Collection
Nome: config
Documento: teacherEmails
Campo: emails (array)
Valor: ["seu@email.com", "outro@email.com"]
```

### 4. Adicionar dependências (build.gradle.kts)

```kotlin
implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
implementation("com.google.android.gms:play-services-auth:21.0.0")
```

**Depois disso?** Cada novo professor se auto-registra! ✅

---

## 📊 Tabela Resumida

| Aspecto | Opção 1 | Opção 2 ⭐ | Opção 3 |
|---------|---------|----------|---------|
| **Professor precisa lembrar senha?** | Não | Não | Sim |
| **Você precisa cadastrar?** | Não | Sim (1x) | Sim (sempre) |
| **Segurança** | Baixa | Alta | Alta |
| **Facilidade para professor** | Máxima | Máxima | Média |
| **Recomendação** | Teste | **PRODUÇÃO** | Legacy |

---

## 🤔 FAQ

### P: E se eu adicionar um email novo? Quando ele consegue entrar?

**R:** Instantâneo! Assim que você adiciona o email em `config/teacherEmails`, na próxima tentativa de login ele consegue entrar.

### P: E se eu remover um email da lista?

**R:** Ele não consegue mais fazer login. Qualquer tentativa nova será rejeitada. (Os já logados só desligam no próximo logout)

### P: Posso usar domínio de email?

**R:** Sim! Mude a validação de:

```kotlin
// Validação por lista exata
return email in authorizedEmails

// Para validação por domínio
return email.endsWith("@escola.com.br")
```

### P: E se o professor não tiver Google?

**R:** Ele não consegue logar como professor. Mas você pode:
1. Criar uma conta Google grátis para ele
2. Usar email + senha também (Opção 3 híbrida)
3. Usar outro provedor (GitHub, Microsoft, etc.)

### P: Firestore vai cobrar por essas queries?

**R:** Sim, mas muito pouco:
- Cada login = 1 read do documento `config/teacherEmails`
- ~$0.06 por 100.000 logins
- Praticamente grátis

---

## ✅ Checklist Final

- [ ] Ativar Google Sign-In no Firebase
- [ ] Obter Web Client ID
- [ ] Adicionar dependências Firebase Auth
- [ ] Criar `config/teacherEmails` no Firestore
- [ ] Implementar `AuthRepository.kt`
- [ ] Atualizar `LoginScreen.kt` com Google button
- [ ] Testar login com um professor
- [ ] Adicionar mais emails conforme necessário

---

## 🎉 Resultado Final

```
Aluno/Responsável:
┌─────────────────────────────┐
│ Digite seu RA               │
│ [____________]              │
│ [ENTRAR]                    │
└─────────────────────────────┘

Professor:
┌─────────────────────────────┐
│                             │
│ [ENTRAR COM GOOGLE]         │
│ (usa sua conta Google)      │
│                             │
└─────────────────────────────┘
```

Pronto! 🚀