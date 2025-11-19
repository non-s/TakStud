# 🔧 Exemplos Práticos de Implementação

## 1. Integração do Dashboard Components

### Exemplo Completo: Dashboard do Professor

```kotlin
@Composable
fun TeacherDashboardExample(
    tasksCount: Int = 12,
    studentsCount: Int = 25,
    averageAttendance: Float = 92.5f
) {
    Scaffold(
        containerColor = LightGray,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Área do Professor", fontWeight = FontWeight.Bold)
                        Text("Dashboard", fontSize = 12.sp, alpha = 0.7f)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Seção de Resumo
            item {
                Text(
                    "📊 Resumo Rápido",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Cards de Estatísticas em Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        title = "Tarefas",
                        value = tasksCount.toString(),
                        icon = "📋",
                        backgroundColor = AccentBlue.copy(alpha = 0.1f)
                    )
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        title = "Alunos",
                        value = studentsCount.toString(),
                        icon = "👥",
                        backgroundColor = SuccessGreen.copy(alpha = 0.1f)
                    )
                }
            }

            item {
                StatisticCard(
                    title = "Frequência",
                    value = String.format("%.1f", averageAttendance),
                    unit = "%",
                    icon = "✅",
                    backgroundColor = SuccessGreen.copy(alpha = 0.1f)
                )
            }

            // Seção de Ações
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🎯 Ações Principais",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Cards de Ação
            items(listOf(
                Triple("📋", "Gerenciar Tarefas", "Crie e edite tarefas"),
                Triple("👥", "Gerenciar Alunos", "Cadastre novos alunos"),
                Triple("✅", "Marcar Presença", "Realize chamadas")
            )) { (icon, title, desc) ->
                ActionCard(
                    title = title,
                    description = desc,
                    icon = icon,
                    onClick = { /* ação */ }
                )
            }
        }
    }
}
```

---

## 2. Formulário Completo com Validação

### Exemplo: Formulário de Cadastro de Aluno

```kotlin
@Composable
fun StudentRegistrationForm(
    onSubmit: (StudentData) -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var ra by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // Validadores customizados
    val nameValidator: (String) -> String? = { text ->
        when {
            text.isEmpty() -> null
            text.length < 3 -> "Mínimo 3 caracteres"
            text.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
    }

    val raValidator: (String) -> String? = { text ->
        when {
            text.isEmpty() -> null
            text.length < 2 -> "RA deve ter no mínimo 2 dígitos"
            text.toIntOrNull() == null -> "RA deve conter apenas números"
            else -> null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        item {
            Text(
                "Cadastrar Novo Aluno",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
        }

        // Campos de texto
        item {
            ValidatedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome Completo",
                placeholder = "Ex: João Silva",
                validator = nameValidator,
                maxLength = 100
            )
        }

        item {
            ValidatedTextField(
                value = ra,
                onValueChange = { ra = it },
                label = "Registro Acadêmico (RA)",
                placeholder = "Ex: 123456",
                validator = raValidator,
                keyboardType = KeyboardType.Number,
                maxLength = 20
            )
        }

        // Email
        item {
            EmailField(
                value = email,
                onValueChange = { email = it },
                label = "Email do Responsável"
            )
        }

        // Data
        item {
            DateField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = "Data de Nascimento"
            )
        }

        // Seleção de turma
        item {
            SelectField(
                value = studentClass,
                options = listOf("6º Ano", "7º Ano", "8º Ano", "9º Ano"),
                onValueChange = { studentClass = it },
                label = "Série/Turma"
            )
        }

        // Checkbox
        item {
            CheckboxField(
                value = isActive,
                onValueChange = { isActive = it },
                label = "Aluno ativo"
            )
        }

        // Botões
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PrimaryButton(
                text = "Cadastrar Aluno",
                isLoading = isLoading,
                onClick = {
                    // Validar antes de enviar
                    if (name.isNotEmpty() && ra.isNotEmpty() && email.isNotEmpty()) {
                        isLoading = true
                        // Simular salvamento
                        onSubmit(StudentData(name, ra, email, dateOfBirth, studentClass))
                        isLoading = false
                    }
                }
            )
        }

        item {
            SecondaryButton(
                text = "Cancelar",
                onClick = { /* voltar */ }
            )
        }
    }
}

data class StudentData(
    val name: String,
    val ra: String,
    val email: String,
    val dateOfBirth: String,
    val studentClass: String
)
```

---

## 3. Tela de Carregamento

### Exemplo: Loading State com Skeleton

```kotlin
@Composable
fun TeacherDashboardWithLoading(viewModel: TeacherViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val tasksCount by viewModel.tasksCount.collectAsState()
    val studentsCount by viewModel.studentsCount.collectAsState()

    if (isLoading) {
        Scaffold(
            containerColor = LightGray,
            topBar = {
                TopAppBar(
                    title = { Text("Carregando...") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skeleton do título
                item {
                    SkeletonShimmer(
                        width = Modifier.fillMaxWidth(0.5f),
                        height = Modifier.height(24.dp)
                    )
                }

                // Skeletons dos cards de estatística
                items(3) {
                    LoadingStatisticCard()
                }

                // Skeleton de seção
                item {
                    SkeletonShimmer(
                        width = Modifier.fillMaxWidth(0.6f),
                        height = Modifier.height(18.dp)
                    )
                }

                // Skeletons dos itens
                items(3) {
                    LoadingListItem()
                }
            }
        }
    } else {
        // Dashboard real com dados
        TeacherScreen(
            tasksCount = tasksCount,
            studentsCount = studentsCount,
            onManageTasks = { viewModel.navigateToTasks() }
            // ... outros callbacks
        )
    }
}
```

---

## 4. Cards Expandíveis para Detalhes

### Exemplo: Lista de Tarefas com Expandables

```kotlin
@Composable
fun TasksListExpandable(tasks: List<Task>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            ExpandableCard(
                title = task.title,
                icon = "📋",
                initiallyExpanded = false
            ) {
                // Detalhes expandíveis
                Text(
                    "Descrição: ${task.description}",
                    fontSize = 12.sp,
                    color = DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Prazo: ${task.dueDate}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WarningYellow
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryButton(
                        text = "Editar",
                        onClick = { /* editar */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    )
                    SecondaryButton(
                        text = "Deletar",
                        onClick = { /* deletar */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    )
                }
            }
        }
    }
}
```

---

## 5. Indicadores de Grade/Nota

### Exemplo: Visualização de Desempenho

```kotlin
@Composable
fun StudentPerformanceView(grades: List<Grade>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Desempenho do Aluno",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )

        // Média geral com card
        val averageGrade = grades.mapNotNull { it.score.toFloatOrNull() }.average().toFloat()

        ProgressBarCard(
            title = "Média Geral",
            progress = averageGrade / 100f,
            label = String.format("%.1f de 100", averageGrade),
            icon = "📊"
        )

        // Indicadores de nota individuais
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            grades.take(3).forEach { grade ->
                GradeIndicator(
                    grade = grade.score.toFloatOrNull() ?: 0f,
                    label = "Tarefa"
                )
            }
        }

        // Cards de detalhes expandíveis
        grades.forEach { grade ->
            ExpandableCard(
                title = "Tarefa: ${grade.taskId.take(8)}",
                icon = "📝"
            ) {
                Text("Nota: ${grade.score}/100", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Data: ${grade.dateSubmitted}", fontSize = 11.sp, color = DarkGray)
            }
        }
    }
}
```

---

## 6. Form com Multi-Step

### Exemplo: Formulário de Login

```kotlin
@Composable
fun EnhancedLoginForm(
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo/Título
        Text(
            "TakStud",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email
        EmailField(
            value = email,
            onValueChange = { email = it },
            label = "Email ou RA"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Senha
        PasswordField(
            value = password,
            onValueChange = { password = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lembrar-me
        CheckboxField(
            value = rememberMe,
            onValueChange = { rememberMe = it },
            label = "Manter-me conectado"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão de Login
        PrimaryButton(
            text = "Entrar",
            isLoading = isLoading,
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    onLogin(email, password)
                    isLoading = false
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Link de Recuperação
        Text(
            "Esqueceu a senha?",
            color = AccentBlue,
            fontSize = 12.sp,
            modifier = Modifier.clickable { /* recuperar */ }
        )
    }
}
```

---

## 7. List com Loading States

### Exemplo: Alunos com Skeleton

```kotlin
@Composable
fun StudentsList(
    students: List<Student>,
    isLoading: Boolean,
    onStudentClick: (Student) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        if (isLoading) {
            // Mostrar skeletons
            items(5) {
                LoadingListItem()
            }
        } else if (students.isEmpty()) {
            // Estado vazio
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📚", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhum aluno cadastrado",
                            color = DarkGray
                        )
                    }
                }
            }
        } else {
            // Mostrar dados reais
            items(students) { student ->
                ActionCard(
                    title = student.name,
                    description = "RA: ${student.ra}",
                    icon = "👤",
                    onClick = { onStudentClick(student) }
                )
            }
        }
    }
}
```

---

## 🎯 Checklist de Uso

Para usar esses exemplos:

- [ ] Copiar os componentes para `ui/components/`
- [ ] Importar nos arquivos de tela
- [ ] Conectar com ViewModel/StateFlow
- [ ] Ajustar cores conforme paleta
- [ ] Testar em dispositivos reais
- [ ] Implementar callbacks reais
- [ ] Adicionar navegação apropriada
- [ ] Testar acessibilidade

---

## 📚 Recursos Úteis

- [Material Design 3 Spec](https://m3.material.io/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Color Contrast Checker](https://webaim.org/resources/contrastchecker/)

---

**Versão**: 1.0
**Data**: 19/11/2025
**Status**: ✅ Pronto para Usar