# 📚 TakStud - Sistema de Gerenciamento Acadêmico

Um aplicativo Android moderno para gerenciamento de atividades escolares, conectando professores e responsáveis de alunos através de uma plataforma centralizada em tempo real.

## ✨ Funcionalidades

### Para Professores 👨‍🏫
- ✅ Gerenciar turmas e horários
- ✅ Criar e gerenciar atividades/provas
- ✅ Lançar notas dos alunos
- ✅ Controle de presença (chamada)
- ✅ Publicar avisos para responsáveis
- ✅ Gerenciar banco de dados de alunos

### Para Responsáveis 👨‍👩‍👧
- ✅ Consultar horários da turma
- ✅ Visualizar avisos do professor
- ✅ Acompanhar atividades e provas
- ✅ Verificar notas
- ✅ Consultar frequência do aluno

## 🏗️ Arquitetura

**Padrão:** MVVM + Repository Pattern

```
UI (Jetpack Compose)
    ↓
ViewModel (StateFlow)
    ↓
Repository (Firestore Listeners)
    ↓
Firebase Firestore (Backend)
```

## 🛠️ Stack Técnico

| Componente | Versão | Descrição |
|-----------|--------|-----------|
| **Kotlin** | 2.0.10 | Linguagem principal |
| **Jetpack Compose** | 2024.09.00 | Framework UI reativo |
| **Firebase Firestore** | 25.1.4 | Banco de dados em tempo real |
| **Firebase Remote Config** | 22.1.2 | Configuração remota |
| **AndroidX Navigation** | 2.9.6 | Navegação entre telas |
| **Target SDK** | 36 | Android 15 |
| **Min SDK** | 29 | Android 10 |

## 📋 Pré-requisitos

- Android Studio Koala (2024.1.1) ou superior
- JDK 11 ou superior
- Emulador Android (API 29+) ou dispositivo físico

## 🚀 Como Configurar

### 1. Clone o Projeto
```bash
git clone https://github.com/seu-usuario/TakStud.git
cd TakStud
```

### 2. Configure o Firebase
```bash
# Coloque o arquivo google-services.json na pasta app/
# Disponível no Firebase Console (https://console.firebase.google.com)
```

### 3. Instale Dependências
```bash
./gradlew build
```

### 4. Execute no Emulador/Dispositivo
```bash
./gradlew installDebug
```

## 🔐 Segurança

### Login de Professor
- Código de acesso: `58239617` (configurável em Remote Config)

### Login de Responsável
- RA do aluno (ex: "001", "002")

⚠️ **IMPORTANTE:** Este é um projeto educacional. Antes de usar em produção:
- Implementar Firebase Authentication
- Usar hash seguro para senhas
- Configurar Firestore Security Rules
- Adicionar autenticação multi-fator

## 📦 Estrutura do Projeto

```
TakStud/
├── app/                              # Módulo da aplicação
│   ├── src/main/java/com/example/takstud/
│   │   ├── model/                   # Modelos de dados
│   │   ├── ui/
│   │   │   ├── login/               # Telas de autenticação
│   │   │   ├── teacher/             # Telas do professor
│   │   │   ├── parent/              # Telas do responsável
│   │   │   ├── components/          # Componentes reutilizáveis
│   │   │   └── theme/               # Temas e estilos
│   │   ├── MainActivity.kt          # Activity principal
│   │   ├── TakStudViewModel.kt      # ViewModel principal
│   │   ├── TakStudRepository.kt     # Repository (Firebase)
│   │   └── TakStudNavGraph.kt       # Configuração de navegação
│   └── res/                         # Recursos (strings, cores, etc)
├── data/                            # Módulo de biblioteca de dados
└── build.gradle.kts                 # Configuração de build
```

## 🧪 Testes

### Executar Testes Unitários
```bash
./gradlew test
```

### Executar Testes de Instrumentação
```bash
./gradlew connectedAndroidTest
```

## 📊 Coleções Firestore

| Coleção | Descrição | Campos |
|---------|-----------|--------|
| **tasks** | Atividades e provas | id, title, description, dueDate, studentClass |
| **notices** | Avisos dos professores | id, title, description, studentClass |
| **schedules** | Horários de aula | id, studentClass, periodo, details |
| **students** | Cadastro de alunos | id, ra, studentClass |
| **grades** | Notas dos alunos | id, taskId, studentId, score |
| **attendance** | Registros de presença | id, date, studentId, isPresent |

## 🔄 Fluxo de Dados

```
Firestore
    ↓
Listeners em Tempo Real
    ↓
Repository (atualiza StateFlows)
    ↓
ViewModel (expõe dados)
    ↓
Composables (renderizam)
```

## 🎯 Roadmap de Melhorias

- [ ] **Fase 1 - Segurança**
  - [ ] Implementar Firebase Authentication
  - [ ] Adicionar validação de entrada
  - [ ] Sistema de roles e permissões
  - [ ] Firestore Security Rules

- [ ] **Fase 2 - Qualidade**
  - [ ] Testes unitários e UI
  - [ ] Análise estática (Detekt)
  - [ ] CI/CD pipeline
  - [ ] Tratamento de erros robusto

- [ ] **Fase 3 - Performance**
  - [ ] Room Database (cache local)
  - [ ] Paging 3 (paginação)
  - [ ] Offline-first architecture

- [ ] **Fase 4 - UX**
  - [ ] Feedback visual (loading, snackbars)
  - [ ] Responsividade completa
  - [ ] Acessibilidade (TalkBack)
  - [ ] Internacionalização (i18n)

## 📝 Convenções de Código

### Nomenclatura
- Classes: `PascalCase` (ex: `ParentScreen`)
- Funções: `camelCase` (ex: `getTasksForStudent`)
- Constantes: `UPPER_SNAKE_CASE` (ex: `DEFAULT_TIMEOUT`)

### Estrutura de Composables
```kotlin
@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = hiltViewModel(),
    onNavigate: () -> Unit
) {
    // Implementação
}
```

## 🐛 Reportar Bugs

Encontrou um problema? Abra uma issue no GitHub com:
- Descrição clara do problema
- Passos para reproduzir
- Comportamento esperado vs actual
- Screenshots/logs se aplicável

## 📄 Licença

Este projeto é licenciado sob a [MIT License](LICENSE)

## 👥 Contribuidores

- Claude Code - Implementação e melhorias

## 📚 Recursos Úteis

- [Documentação Android](https://developer.android.com)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs)

---

**Desenvolvido com ❤️ usando Kotlin e Jetpack Compose**