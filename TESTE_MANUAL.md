# 🧪 GUIA DE TESTE MANUAL - TAKSTUD

## ✅ APP ESTÁ RODANDO!

Parabéns! Você está vendo a **LoginScreen** do TakStud! 🎉

---

## 📱 TELA INICIAL (LOGIN)

Você deve ver:
- 🎨 Logo do TakStud (no topo)
- 📝 Campo de entrada para "RA do Aluno"
- 🔘 Botão "Entrar como Responsável"
- 🔘 Botão "Entrar como Professor"

---

## 🧪 TESTE 1: LOGIN COMO RESPONSÁVEL

### Passos:
1. No campo "RA do Aluno", digite qualquer valor (ex: `001`, `ALU-001`, etc)
2. Clique no botão "Entrar como Responsável"

### Esperado:
- ✅ Ir para a tela do Responsável (ParentScreen)
- ✅ Mostrar dados do aluno
- ✅ Exibir horários, avisos, tarefas, notas, frequência

### Se não funcionar:
- Verifique se o Firebase está conectado
- Confira os logs (Logcat)

---

## 🧪 TESTE 2: LOGIN COMO PROFESSOR

### Passos:
1. Clique no botão "Entrar como Professor"
2. Você verá a **AdminLoginScreen** pedindo código de acesso
3. Digite a senha: `58239617` (configurada em Remote Config)

### Esperado:
- ✅ Ir para a tela do Professor (TeacherScreen)
- ✅ Mostrar menu com opções:
  - Gerenciar Atividades/Provas
  - Gerenciar Avisos
  - Gerenciar Horários
  - Gerenciar Alunos
  - Controle de Presença

---

## 📋 TESTE 3: NAVEGAÇÃO PROFESSOR

Na tela do Professor, teste cada botão:

### 📚 Gerenciar Atividades
- [ ] Clique no botão
- [ ] Deve ir para TaskListScreen
- [ ] Mostrar lista de tarefas
- [ ] Deve ter botão para adicionar nova tarefa

### 📢 Gerenciar Avisos
- [ ] Clique no botão
- [ ] Deve ir para NoticeListScreen
- [ ] Mostrar lista de avisos
- [ ] Deve ter botão para adicionar novo aviso

### 🕐 Gerenciar Horários
- [ ] Clique no botão
- [ ] Deve ir para SchedulesListScreen
- [ ] Mostrar horários por turma

### 👥 Gerenciar Alunos
- [ ] Clique no botão
- [ ] Deve ir para ManageStudentsScreen
- [ ] Mostrar lista de alunos

### 📋 Controle de Presença
- [ ] Clique no botão
- [ ] Deve ir para AttendanceScreen
- [ ] Permitir selecionar turma e data

---

## 📋 TESTE 4: VALIDAÇÃO DE ENTRADA

Com a melhoria implementada, adicione validação:

### Teste no LoginScreen:
```
❌ ANTES: Aceita RA vazio
✅ DEPOIS: Deve rejeitar (adicione validation)
```

**Para adicionar validação (Semana 1):**
Veja: `IMPLEMENTATION_GUIDE.md` Seção 1

---

## 🔙 TESTE 5: BOTÃO VOLTAR/LOGOUT

- [ ] De qualquer tela, clique em "Voltar" ou no ícone ⬅️
- [ ] Deve retornar para tela anterior
- [ ] Clique em "Sair" para voltar ao login

---

## 🐛 CHECKLIST DE TESTES

### Funcionalidades Principais
- [ ] LoginScreen renderiza corretamente
- [ ] Botão "Responsável" funciona
- [ ] Botão "Professor" funciona
- [ ] AdminLoginScreen aparece após clicar "Professor"
- [ ] Código correto (58239617) permite acesso
- [ ] TeacherScreen mostra 5 botões
- [ ] ParentScreen mostra dados do aluno
- [ ] Navegação entre telas funciona
- [ ] Botões "Voltar" funcionam
- [ ] Botões "Sair" voltam ao login

### Interface
- [ ] Logo aparece no login
- [ ] Cores tema estão corretas
- [ ] Botões responsivos ao toque
- [ ] Textos legíveis
- [ ] Layout centralizado
- [ ] Sem crash de UI

### Firebase (se conectado)
- [ ] Dados carregam do Firestore
- [ ] Sem erro de conexão
- [ ] Sincronização em tempo real funciona

---

## 📊 RELATÓRIO DE TESTE

Depois de testar, anote:

```
Data: _______________
Dispositivo/Emulador: _______________
Android Version: _______________

Testes OK: _____ / 10
Testes com Problema: _____ / 10

Problemas encontrados:
1. _________________________________
2. _________________________________
3. _________________________________

Observações gerais:
_________________________________
_________________________________
```

---

## 🚨 PROBLEMAS COMUNS

### "App não abre"
- [ ] Compile novamente: `./gradlew build`
- [ ] Limpe build: `./gradlew clean build`
- [ ] Reinicie emulador

### "Tela em branco"
- [ ] Verifique Logcat para erros
- [ ] Confirme Firebase está configurado
- [ ] Verifique strings.xml tem recursos

### "Botão não responde"
- [ ] Verifique Logcat para exceções
- [ ] Confirme Firebase está conectado
- [ ] Tente novamente

### "Erro de Firebase"
- [ ] Verifique google-services.json
- [ ] Confirme projeto Firebase correto
- [ ] Verifique permissões Internet no AndroidManifest

---

## ✅ PRÓXIMO PASSO APÓS TESTES

Se tudo funciona:

1. **Integrar InputValidator** (Semana 1)
   - Adicione validação nos formulários
   - Teste com RA vazio
   - Teste com RA inválido

2. **Implementar Firebase Auth** (Semana 2)
   - Autenticação real
   - Login com email/senha
   - Sign out

3. **Adicionar Roles/Permissões** (Semana 3)
   - Sistema de acesso baseado em role
   - Firestore Security Rules

---

## 📞 DOCUMENTAÇÃO DE REFERÊNCIA

- `README.md` - Visão geral
- `IMPLEMENTATION_GUIDE.md` - Como implementar features
- `PLANO_ACAO.md` - Próximas semanas

---

## 🎉 SUCESSO!

Se todos os testes passaram:
✅ Seu app está funcionando perfeitamente!
✅ Próximo passo é integrar validação (Semana 1)

---

*Guia criado em 11 de Novembro de 2025*