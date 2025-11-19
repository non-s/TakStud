# ❓ FAQ - Autenticação com Firebase e Google Sign-In

## Perguntas Mais Frequentes

---

## 📋 Perguntas sobre o Cadastro

### P1: Se eu colocar Google para professor, EU TENHO que cadastrar ele antes?

**R:** Depende da opção:

- **Opção A (Automático):** NÃO precisa cadastrar. Professor loga com Google e pronto.
  - ❌ Mas qualquer um com Google pode entrar!

- **Opção B (Recomendado):** SEMI-AUTOMÁTICO
  - Você adiciona email em `config/teacherEmails` uma vez
  - Professor loga com Google
  - App verifica se email está na lista
  - Se SIM → entra direto (cadastro automático)
  - Se NÃO → rejeita

**Resposta Curta:** Você não precisa criar conta no app. Você só adiciona o email em uma lista, e professor entra sozinho!

---

### P2: Como eu adiciono um novo professor?

**R:** Super simples:

1. Abra Firebase Console
2. Vá para Firestore → config → teacherEmails
3. Clique em "emails" → "adicionar elemento"
4. Digite o email: `professor@gmail.com`
5. Clique em "salvar"

Pronto! Na próxima vez que ele tentar logar com Google, entra direto.

---

### P3: E se eu remover um professor da lista?

**R:** Ele não consegue mais fazer login. Mas:
- Se ele já está logado, continua logado até fazer logout
- Na próxima tentativa de login, será rejeitado

---

### P4: Posso usar domínios de email? (Ex: @escola.com.br)

**R:** Sim! Mude a validação no código:

```kotlin
// Ao invés de lista exata:
// return email in authorizedEmails

// Use validação por domínio:
fun isEmailFromAuthenticatedDomain(email: String): Boolean {
    return email.endsWith("@escola.com.br") ||
           email.endsWith("@seu-dominio.com.br")
}
```

Assim qualquer pessoa com email @escola.com.br consegue entrar.

---

### P5: E se o professor não tiver Google?

**R:** Tem 3 opções:

1. **Criar Gmail grátis para ele** (5 minutos)
2. **Usar email + senha também** (mais chato)
3. **Usar outro provedor** (GitHub, Microsoft, etc.)

A mais simples é a opção 1: criar um Gmail grátis.

---

## 🔐 Perguntas sobre Segurança

### P6: Como isso é seguro? Qualquer um pode entrar?

**R:** NÃO! Você controla com a lista de emails:

```
┌────────────────────────────────┐
│ Firestore config/teacherEmails │
│ emails: [                      │
│   "maria@gmail.com"            │
│   "joao@gmail.com"             │
│ ]                              │
└────────────────────────────────┘

Hacker tenta logar:
  hacker@gmail.com ❌ NÃO está na lista

  auth.signOut()
  throw Exception("Email não autorizado")
  ❌ Acesso negado!
```

Você é quem controla quem entra. Ninguém mais consegue entrar sem estar na sua lista.

---

### P7: E se alguém roubar a senha Google do professor?

**R:** Se roubarem a CONTA Google do professor, conseguem logar. Mas:

1. Google tem 2FA (autenticação de dois fatores)
2. Professor será notificado de novo login
3. Pode fazer logout em todos lugares

É um risco normal de qualquer conta online. Google cuida bem da segurança deles.

---

### P8: Meus dados estão seguros no Firebase?

**R:** Sim, mas VOCÊ precisa configurar as Firestore Security Rules:

```firestore
// SEM regras:
Qualquer um consegue ler/escrever tudo ❌

// COM regras:
Cada usuário vê apenas seus dados ✅
```

Veja documento `FIREBASE_AUTH_IMPLEMENTATION.md` - Passo 1.4

---

### P9: Firebase vai vender meus dados?

**R:** NÃO. Google respeita privacidade. Mas:
- Google pode ver que você usa Firebase
- Google Analytics coleta dados anônimos
- Você pode desabilitar analytics se quiser

Tudo está legal. Se o app é para escola, avise os responsáveis.

---

## 💻 Perguntas sobre Implementação

### P10: Quanto tempo leva para implementar?

**R:** ~45 minutos:
- Setup Firebase: 10 min
- Copiar código: 20 min
- Testar: 15 min

Se tiver problema, pode demorar mais.

---

### P11: Preciso de internet para testar?

**R:** SIM. Firebase funciona só online.

Para testar sem internet, use Local Firebase Emulator (avançado).

---

### P12: Como obter o Web Client ID?

**R:** No Google Cloud Console:

1. https://console.cloud.google.com/
2. Procure "credentials" na barra de busca
3. Você verá "Web client 1" (ou similiar)
4. Copie o "Client ID"

Está em `SETUP_FIREBASE_STEP_BY_STEP.md` - Passo 2.3

---

### P13: Posso usar com Firebase Emulator?

**R:** SIM, para testes! Mas é avançado.

Para uso normal, use Firebase Cloud (grátis).

---

### P14: Preciso de APK diferente para teste vs produção?

**R:** NÃO. Mesma APK funciona nos dois. Basta mudar:
- Web Client ID (para teste: use um teste)
- Firebase Project (teste vs produção)

Mas no início, teste tudo no mesmo projeto.

---

## 🎯 Perguntas sobre Funcionalidades

### P15: Como professor loga se estiver sem internet?

**R:** Não consegue. Firebase é só online.

Para funcionar offline, você precisaria:
- Room Database (BD local)
- WorkManager (sincronizar quando voltar)

Veja `PLANO_ACAO.md` para roadmap.

---

### P16: Posso ter múltiplas turmas por professor?

**R:** DEPENDE de como você estruturar:

**Opção 1:** Campo "classes" no documento professor
```kotlin
teachers/{uid} = {
  email: "maria@gmail.com",
  classes: ["8A", "8B", "9A"]
}
```

**Opção 2:** Relação separada
```
teacher_classes/ = {
  doc1: { teacherId: "...", className: "8A" },
  doc2: { teacherId: "...", className: "8B" }
}
```

Escolha conforme seu modelo de dados.

---

### P17: Responsável pode logar múltiplas vezes?

**R:** SIM, mas cada login cria nova sessão anônima.

Para melhorar, use:
```kotlin
// Salvar token local no dispositivo
preferences.putString("parentToken", uid)

// Próxima vez, verificar se token ainda é válido
```

---

### P18: Posso logar como professor E responsável?

**R:** Sim! Você teria 2 sessões diferentes.

Mas no app, faria logout da primeira ao logar na segunda.

---

## 🐛 Perguntas sobre Troubleshooting

### P19: Erro "Web Client ID inválido"

**Causa:** Você não colou corretamente

**Solução:**
```kotlin
// Errado:
.requestIdToken("YOUR_WEB_CLIENT_ID_AQUI")

// Certo:
.requestIdToken("123456789.apps.googleusercontent.com")
```

---

### P20: Erro "Email não está autorizado" (mas deveria estar)

**Causa:** Email não está em `config/teacherEmails`

**Solução:**
1. Firestore → config → (documento)
2. Campo "emails" → "adicionar elemento"
3. Digite o email exato (case-sensitive)
4. Tente fazer login novamente

---

### P21: Erro "RA não encontrado" (mas o RA existe)

**Causa:** Dado digitado errado ou estrutura diferente

**Solução:**
1. Firestore → students → clique em um documento
2. Procure o campo "ra"
3. Copie o valor exato
4. Tente login novamente

Ou crie um RA de teste:
```
documents.collection("students").add({
  ra: "123456",
  name: "João Teste",
  studentClass: "8A"
})
```

---

### P22: Login funciona mas responsável não vê dados

**Causa:** Firestore Security Rules não configurado

**Solução:** Veja documento `FIREBASE_AUTH_IMPLEMENTATION.md` - Passo 1.4

Configure as regras de segurança corretas.

---

### P23: App loada infinitamente

**Causa:** Firestore listener não retorna dados

**Solução:**
1. Verificar internet
2. Verificar Firestore está ativado
3. Verificar dados existem no Firestore
4. Ver logs: `adb logcat | grep Firestore`

---

### P24: Múltiplos logins diferentes não funcionam

**Causa:** Não está fazendo logout antes de logar como outro

**Solução:** Sempre fazer logout antes:
```kotlin
authRepository.logout()  // Logout anterior
// Depois fazer novo login
```

---

## 💰 Perguntas sobre Custo

### P25: Quanto custa usar Firebase?

**R:** Para seu caso: GRÁTIS!

**Plano Free:**
- 50.000 reads por dia ✅
- 20.000 writes por dia ✅
- 100 conexões simultâneas ✅
- Ilimitados listeners

**Seu uso:**
- 100 alunos = 100 reads por login
- 5 professores = 5 reads por login
- Está FAR abaixo do limite ✅

Só paga se passar muito dos limites (improvável).

---

### P26: E se o app viralizar?

**R:** Google roda contadores:

```
Hoje: 10 reads (R$ 0)
Semana: 700 reads (R$ 0)
Mês: 3.000 reads (R$ 0)
Ano: 36.500 reads (R$ 0)

Pagar só quando passar:
- 50.000 reads/dia = ~R$0.03/dia
```

Improvável, mas se viralizar, você tem dinheiro para pagar R$1 por dia 😄

---

## 🚀 Perguntas sobre Próximos Passos

### P27: Depois disso, qual é a próxima melhoria?

**R:** Ordem recomendada:

1. **Offline support** (Room + WorkManager)
2. **Mais testes** (aumentar cobertura)
3. **Paging** (carregar dados em páginas)
4. **Internacionalização** (outros idiomas)
5. **CI/CD** (testes automáticos)

Veja `PLANO_ACAO.md` para detalhes.

---

### P28: Posso usar outro provedor além de Google?

**R:** SIM! Firebase suporta:
- ✅ Email/Senha
- ✅ Google
- ✅ Facebook
- ✅ GitHub
- ✅ Microsoft
- ✅ Apple
- ✅ PhoneNumber
- ✅ Anônimo

Para professor, recomendo Google ou Email+Senha.

---

### P29: Posso fazer auto-login?

**R:** SIM! Com SharedPreferences:

```kotlin
// Após login bem-sucedido:
preferences.putString("userId", uid)
preferences.putString("role", "TEACHER")

// Na próxima abertura:
val savedUid = preferences.getString("userId")
if (savedUid != null) {
  // Pula LoginScreen, vai direto para TeacherScreen
}
```

---

### P30: Como fazer logout automático por timeout?

**R:** Implemente um timer:

```kotlin
// Logout após 30 minutos de inatividade
val idleTimeoutMinutes = 30
var lastActivityTime = System.currentTimeMillis()

fun resetIdleTimer() {
    lastActivityTime = System.currentTimeMillis()
}

// Em uma coroutine:
while (true) {
    delay(60000)  // Checar a cada 1 minuto
    val idleTime = System.currentTimeMillis() - lastActivityTime
    if (idleTime > idleTimeoutMinutes * 60000) {
        authRepository.logout()
        navigateToLogin()
    }
}
```

---

## 📞 Mais Dúvidas?

Se sua pergunta não está aqui:

1. Procure em `COMPARACAO_METODOS_AUTENTICACAO.md`
2. Procure em `SETUP_FIREBASE_STEP_BY_STEP.md`
3. Procure em `DIAGRAMA_FLUXO_AUTENTICACAO.md`
4. Consulte [Firebase Docs](https://firebase.google.com/docs)

---

## ✅ Checklist Antes de Começar

- [ ] Li todos estes FAQs
- [ ] Entendi a diferença entre as 3 opções
- [ ] Decidi qual opção usar (recomendo Opção B)
- [ ] Criei projeto no Firebase
- [ ] Tenho internet para testar
- [ ] Tenho 1 hora disponível
- [ ] Pronto para começar!

**Vamo lá!** 🚀
