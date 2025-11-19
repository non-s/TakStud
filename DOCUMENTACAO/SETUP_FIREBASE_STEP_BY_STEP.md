# 🚀 Setup Firebase Authentication - Passo a Passo Visual

Este é um guia **super detalhado** com prints/instruções de cada clique que você deve fazer.

---

## 📋 PARTE 1: Firebase Console

### PASSO 1.1: Ativar Google Sign-In

```
1. Acesse https://console.firebase.google.com/
2. Selecione seu projeto "TakStud"
3. No menu esquerdo, clique em: Authentication
4. Clique em: "Sign-in method" (segunda aba)

Você verá uma lista de provedores:

┌─────────────────────────────────────────┐
│ Email/Password    [DISABLED]            │
│ Google            [DISABLED]  ← CLIQUE! │
│ GitHub            [DISABLED]            │
│ Facebook          [DISABLED]            │
│ ...                                     │
└─────────────────────────────────────────┘

5. Clique em "Google"
```

### PASSO 1.2: Ativar Google

```
Uma caixa abrirá com:

┌─────────────────────────────────────────┐
│ HABILITAR GOOGLE SIGN-IN                │
│                                         │
│ Status: [DESATIVADO] ← Clique aqui    │
│                                         │
│ Nome público do projeto:                │
│ [TakStud____________________]          │
│                                         │
│ Email de suporte:                       │
│ [seu@email.com________________]        │
│                                         │
│ [SALVAR] [CANCELAR]                    │
└─────────────────────────────────────────┘

6. Clique no toggle para ativar
7. Preencha seu email
8. Clique em [SALVAR]
```

### PASSO 1.3: Ativar Email/Password (adicional)

```
Volte na tela anterior:

┌─────────────────────────────────────────┐
│ Email/Password    [DISABLED]  ← CLIQUE! │
│ Google            [ENABLED] ✓           │
│ GitHub            [DISABLED]            │
│ ...                                     │
└─────────────────────────────────────────┘

9. Clique em "Email/Password"
10. Ative o toggle
11. Clique em [SALVAR]
```

---

## 🌐 PARTE 2: Google Cloud Console

Agora você precisa obter o **Web Client ID** para autenticação Android.

### PASSO 2.1: Acessar Google Cloud Console

```
1. Acesse https://console.cloud.google.com/
2. No canto superior esquerdo, procure por um dropdown:

┌─────────────────────────────────────────┐
│ Google Cloud                            │
│ [TakStud____________▼]                 │ ← Confirme que TakStud está selecionado
└─────────────────────────────────────────┘

3. Se não estiver, clique no dropdown e selecione "TakStud"
```

### PASSO 2.2: Ativar Google Identity Services API

```
Na barra de busca superior, procure por "Google Identity Services API"

Você verá um resultado que diz:
"Google Identity Services API - APIs & Services"

Clique nele e depois clique em [ENABLE] (ativar)
```

### PASSO 2.3: Criar/Obter Web Client ID

```
Agora procure por "credentials" na barra de busca

Você verá:
"Credentials - APIs & Services"

Clique nele. Você verá:

┌─────────────────────────────────────────┐
│ CREATE CREDENTIALS ▼                    │
│                                         │
│ OAuth 2.0 Client IDs:                  │
│ ┌─────────────────────────────────────┐ │
│ │ Name: Web client 1                  │ │
│ │ Client ID: 123456.apps.googleuser.. │ │
│ │ Client Secret: xxxxxxxxxxx          │ │
│ └─────────────────────────────────────┘ │
│                                         │
└─────────────────────────────────────────┘

4. Se não ver nenhum Web Client, clique em [CREATE CREDENTIALS]
5. Selecione "OAuth Client ID"
6. Application type: "Web application"
7. Name: "TakStud Web Client"
8. Clique [CREATE]
```

### PASSO 2.4: Copiar Web Client ID

```
Você verá uma janela com:

┌─────────────────────────────────────────┐
│ OAuth 2.0 Client created               │
│                                         │
│ Client ID:                              │
│ 123456789.apps.googleusercontent.com   │ ← COPIE ISTO!
│                                         │
│ Client Secret:                          │
│ xxxxxxxxxxxxxxxxxxxxxxxxxxx             │
└─────────────────────────────────────────┘

9. Clique no ícone de copiar ao lado do Client ID
10. Guarde em um local seguro (você usará no código)
```

### PASSO 2.5: Adicionar SHA-1 do seu aplicativo

```
Volte para Firebase Console:
https://console.firebase.google.com/

1. Vá para "Project Settings" (engrenagem no canto superior esquerdo)
2. Clique em "Apps" (primeira aba)
3. Você verá seu app Android listado
4. Na linha do seu app, clique em "..."
5. Selecione "Download google-services.json" (se não tiver)

Agora você precisa obter o SHA-1:

No Android Studio, abra o terminal e digite:
./gradlew signingReport

Procure por: "SHA1"
Você verá algo como:
SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90

Copie todo o código (do "AB" até o final)
```

```
6. Voltando no Firebase (Project Settings → Apps)
7. Role para baixo até "SHA certificate fingerprints"
8. Clique em [+ Adicionar fingerprint]
9. Cole o SHA-1 que você copiou
10. Clique [SALVAR]
```

---

## 💾 PARTE 3: Configurar Firestore (Lista de Emails)

### PASSO 3.1: Criar Coleção "config"

```
1. Abra https://console.firebase.google.com/
2. Selecione seu projeto
3. Clique em "Firestore Database" no menu esquerdo
4. Se não tiver nada, clique em [+ START COLLECTION]

Se já tiver dados, clique em [+ COLEÇÃO]

┌─────────────────────────────────────────┐
│ Nova coleção                            │
│                                         │
│ ID da coleção:                          │
│ [config________________________]        │
│                                         │
│ [PRÓXIMO]                               │
└─────────────────────────────────────────┘

5. Digite: config
6. Clique em [PRÓXIMO]
```

### PASSO 3.2: Criar Documento "teacherEmails"

```
A próxima tela pedirá para criar um documento:

┌─────────────────────────────────────────┐
│ Novo documento                          │
│                                         │
│ ID do documento:                        │
│ (selecione "ID automático")             │
│         OU
│ [teacherEmails____________]            │
│                                         │
│ Primeiro campo:                         │
│ Nome: emails                            │
│ Tipo: Array                             │
│ Valor: [adicionar campo]                │
│                                         │
│ [SALVAR]                                │
└─────────────────────────────────────────┘

7. Selecione "ID automático" (deixa como está)
8. Clique em [AUTO ID]
```

### PASSO 3.3: Adicionar Emails

```
Agora adicione os campos:

Nome do campo: "emails"
Tipo: Array

Para adicionar valores no array:
1. Clique em [adicionar elemento]
2. Digite um email: "maria@gmail.com"
3. Clique em ✓
4. Clique novamente em [adicionar elemento]
5. Digite outro: "joao@escola.com.br"
6. Continue adicionando...

Seu documento ficará assim:

┌────────────────────────────────┐
│ config > (doc_id)              │
│                                │
│ emails: [                      │
│   "maria@gmail.com"            │
│   "joao@escola.com.br"         │
│   "admin@takstud.com"          │
│ ]                              │
└────────────────────────────────┘

7. Clique em [SALVAR]
```

---

## 💻 PARTE 4: Configurar Android Studio

### PASSO 4.1: Adicionar Dependências

```
Arquivo: app/build.gradle.kts

Procure por: dependencies {

Adicione DENTRO do bloco:

    // Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

Depois, clique em "Sync Now" no topo da tela
```

### PASSO 4.2: Copiar Web Client ID no Código

```
Arquivo: GoogleSignInHelper.kt

Procure por:
.requestIdToken("YOUR_WEB_CLIENT_ID_AQUI")

Substitua "YOUR_WEB_CLIENT_ID_AQUI" pelo Web Client ID que você copiou

Exemplo:
Antes:
.requestIdToken("YOUR_WEB_CLIENT_ID_AQUI")

Depois:
.requestIdToken("123456789.apps.googleusercontent.com")
```

### PASSO 4.3: Verificar google-services.json

```
Verificar se o arquivo google-services.json está em:
app/google-services.json

Se não estiver:
1. Firebase Console → Project Settings (engrenagem)
2. Apps → seu app Android
3. Clique em "..." → "Download google-services.json"
4. Copie para app/ (arrastar e soltar no Android Studio)
```

---

## 🧪 PARTE 5: Testar a Implementação

### PASSO 5.1: Compilar o Projeto

```
No Android Studio:
1. Build → Clean Project
2. Build → Rebuild Project

Espere terminar (pode levar alguns minutos)

Se der erro "google-services plugin not found":
  - Sincronize Gradle novamente
  - Limpe e reconstrua
```

### PASSO 5.2: Executar no Emulador

```
1. Abra o emulador do Android Studio
2. Clique em ▶ (play) para executar
3. Espere o app carregar
```

### PASSO 5.3: Testar Login do Professor

```
Quando o app abrir:

1. Clique na aba "Professor"
2. Clique em "Entrar com Google"
3. A tela de login do Google abrirá
4. Use uma conta Google (você pode criar uma fake de teste)
5. Após fazer login, o app dirá:
   ✓ Login sucesso! (se email estiver na lista)
   ✗ Email não autorizado (se não estiver)

Para adicionar seu email na lista:
1. Vá para Firebase Firestore
2. Abra config → (documento)
3. Clique em "emails"
4. Clique em [adicionar elemento]
5. Digite seu email
6. Clique em [SALVAR]
7. Tente fazer login novamente
```

### PASSO 5.4: Testar Login do Responsável

```
1. Clique na aba "Responsável"
2. Digite um RA válido (deve existir na base de dados)
3. Clique em "Entrar"
4. Se o RA existir, entrará com sucesso
5. Se não existir, mostrará erro
```

---

## ✅ Checklist Final

- [ ] Ativei Google Sign-In no Firebase Console
- [ ] Ativei Email/Password no Firebase Console
- [ ] Obti Web Client ID no Google Cloud Console
- [ ] Adicionei SHA-1 no Firebase Console
- [ ] Criei coleção "config" no Firestore
- [ ] Criei documento com meus emails autorizados
- [ ] Adicionei dependências no build.gradle.kts
- [ ] Colei o Web Client ID no código
- [ ] Compilei o projeto com sucesso
- [ ] Testei login do professor com Google
- [ ] Testei login do responsável com RA
- [ ] Adicionei/removi emails da lista com sucesso

---

## 🆘 Resolução de Problemas Comuns

### "Erro: Web Client ID inválido"

**Causa:** Você não adicionou o Web Client ID no código

**Solução:**
1. Copia o Web Client ID do Google Cloud Console
2. Em `GoogleSignInHelper.kt`, substitui:
   `.requestIdToken("COLA_AQUI")`

### "Erro: google-services.json não encontrado"

**Causa:** Arquivo não está no lugar certo

**Solução:**
1. Download de novo do Firebase Console
2. Coloca em `app/google-services.json`
3. Sincroniza Gradle

### "Erro: Email não autorizado (para professor valido)"

**Causa:** Email não está na lista do Firestore

**Solução:**
1. Abre Firestore → config
2. Clica no documento
3. Adiciona o email na array "emails"
4. Tenta fazer login novamente

### "Erro: RA não encontrado (para responsavel valido)"

**Causa:** O RA não existe na coleção "students"

**Solução:**
1. Verifica se o RA está correto
2. Verifica em Firestore → students → tem um documento com esse RA?
3. Se não tiver, cria um documento de teste com:
   - ra: "123456"
   - name: "João da Silva"
   - studentClass: "8A"
   - parent: "Maria da Silva"
   - phone: "11999999999"

---

## 🎉 Parabéns!

Se chegou até aqui sem erros, você tem:

✅ Firebase Authentication ativo
✅ Google Sign-In funcional
✅ Login com RA funcional
✅ Firestore configurado
✅ Segurança com lista de emails

Seu app está pronto para produção! 🚀
