package com.example.takstud.ui.teacher.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.takstud.model.student.*
import com.example.takstud.ui.components.student.*
import com.example.takstud.viewmodel.StudentViewModel
import com.example.takstud.viewmodel.StudentViewModel
import java.util.*

/**
 * 📝 Dialogs e Sheets para gerenciamento de alunos
 */

// ==================== FILTER DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    filters: StudentFilters,
    onDismiss: () -> Unit,
    onApply: (StudentFilters) -> Unit,
    onClear: () -> Unit
) {
    var className by remember { mutableStateOf(filters.className) }
    var grade by remember { mutableStateOf(filters.grade) }
    var period by remember { mutableStateOf(filters.period) }
    var status by remember { mutableStateOf(filters.status) }
    var isScholarship by remember { mutableStateOf(filters.isScholarship) }
    var hasSpecialNeeds by remember { mutableStateOf(filters.hasSpecialNeeds) }
    var sortBy by remember { mutableStateOf(filters.sortBy) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros e Ordenação") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Class filter
                OutlinedTextField(
                    value = className ?: "",
                    onValueChange = { className = it.takeIf { it.isNotBlank() } },
                    label = { Text("Turma") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.School, null) }
                )

                // Grade filter
                OutlinedTextField(
                    value = grade ?: "",
                    onValueChange = { grade = it.takeIf { it.isNotBlank() } },
                    label = { Text("Série") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Grade, null) }
                )

                // Period filter
                OutlinedTextField(
                    value = period ?: "",
                    onValueChange = { period = it.takeIf { it.isNotBlank() } },
                    label = { Text("Período") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Manhã, Tarde, Noite") },
                    leadingIcon = { Icon(Icons.Default.Schedule, null) }
                )

                // Status filter
                Text("Status:", style = MaterialTheme.typography.titleSmall)
                StudentStatus.values().forEach { statusOption ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = status == statusOption,
                            onClick = { status = statusOption }
                        )
                        Text(statusOption.displayName)
                    }
                }
                TextButton(onClick = { status = null }) {
                    Text("Limpar status")
                }

                Divider()

                // Scholarship filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Apenas bolsistas")
                    Switch(
                        checked = isScholarship == true,
                        onCheckedChange = {
                            isScholarship = if (it) true else null
                        }
                    )
                }

                // Special needs filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Necessidades especiais")
                    Switch(
                        checked = hasSpecialNeeds,
                        onCheckedChange = { hasSpecialNeeds = it }
                    )
                }

                Divider()

                // Sort options
                Text("Ordenar por:", style = MaterialTheme.typography.titleSmall)
                StudentSortOption.values().forEach { sortOption ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortBy == sortOption,
                            onClick = { sortBy = sortOption }
                        )
                        Text(getSortLabel(sortOption))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        StudentFilters(
                            className = className,
                            grade = grade,
                            period = period,
                            status = status,
                            isScholarship = isScholarship,
                            hasSpecialNeeds = hasSpecialNeeds,
                            sortBy = sortBy
                        )
                    )
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClear) {
                    Text("Limpar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

private fun getSortLabel(option: StudentSortOption): String {
    return when (option) {
        StudentSortOption.NAME_ASC -> "Nome (A-Z)"
        StudentSortOption.NAME_DESC -> "Nome (Z-A)"
        StudentSortOption.REGISTRATION_ASC -> "Matrícula (crescente)"
        StudentSortOption.REGISTRATION_DESC -> "Matrícula (decrescente)"
        StudentSortOption.GPA_DESC -> "Média (maior primeiro)"
        StudentSortOption.GPA_ASC -> "Média (menor primeiro)"
        StudentSortOption.ATTENDANCE_DESC -> "Frequência (maior primeiro)"
        StudentSortOption.ATTENDANCE_ASC -> "Frequência (menor primeiro)"
    }
}

// ==================== CREATE STUDENT DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStudentDialog(
    onDismiss: () -> Unit,
    onCreate: (StudentExtended) -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }

    // Form state
    var fullName by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var rg by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Long?>(null) }
    var gender by remember { mutableStateOf(Gender.NOT_SPECIFIED) }

    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var registrationNumber by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }

    var guardianName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var guardianEmail by remember { mutableStateOf("") }
    var guardianRelationship by remember { mutableStateOf(GuardianRelationship.OTHER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Aluno - Passo ${currentStep + 1} de 4") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (currentStep) {
                    0 -> {
                        // Personal Info
                        Text("Dados Pessoais", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nome Completo *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )

                        OutlinedTextField(
                            value = cpf,
                            onValueChange = { cpf = it },
                            label = { Text("CPF") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Badge, null) },
                            placeholder = { Text("000.000.000-00") }
                        )

                        OutlinedTextField(
                            value = rg,
                            onValueChange = { rg = it },
                            label = { Text("RG") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.CreditCard, null) }
                        )

                        // Gender selector
                        Text("Gênero:", style = MaterialTheme.typography.bodyMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                selected = gender == Gender.MALE,
                                onClick = { gender = Gender.MALE },
                                label = { Text("Masculino") }
                            )
                            FilterChip(
                                selected = gender == Gender.FEMALE,
                                onClick = { gender = Gender.FEMALE },
                                label = { Text("Feminino") }
                            )
                            FilterChip(
                                selected = gender == Gender.OTHER,
                                onClick = { gender = Gender.OTHER },
                                label = { Text("Outro") }
                            )
                        }
                    }

                    1 -> {
                        // Contact Info
                        Text("Contato", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Telefone *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            placeholder = { Text("(00) 00000-0000") }
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Email, null) }
                        )
                    }

                    2 -> {
                        // Academic Info
                        Text("Dados Acadêmicos", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = registrationNumber,
                            onValueChange = { registrationNumber = it },
                            label = { Text("Matrícula (RA) *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Numbers, null) }
                        )

                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Turma *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.School, null) },
                            placeholder = { Text("Ex: 1A, 2B") }
                        )

                        OutlinedTextField(
                            value = grade,
                            onValueChange = { grade = it },
                            label = { Text("Série") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ex: 1º Ano, 2ª Série") }
                        )

                        OutlinedTextField(
                            value = period,
                            onValueChange = { period = it },
                            label = { Text("Período") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Manhã, Tarde, Noite") }
                        )
                    }

                    3 -> {
                        // Guardian Info
                        Text("Responsável", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Pelo menos um responsável é obrigatório",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = guardianName,
                            onValueChange = { guardianName = it },
                            label = { Text("Nome do Responsável *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )

                        // Relationship selector
                        Text("Parentesco:", style = MaterialTheme.typography.bodyMedium)
                        Column {
                            listOf(
                                GuardianRelationship.MOTHER,
                                GuardianRelationship.FATHER,
                                GuardianRelationship.GRANDMOTHER,
                                GuardianRelationship.GRANDFATHER
                            ).forEach { relationship ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = guardianRelationship == relationship,
                                        onClick = { guardianRelationship = relationship }
                                    )
                                    Text(relationship.displayName)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = guardianPhone,
                            onValueChange = { guardianPhone = it },
                            label = { Text("Telefone do Responsável *") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            placeholder = { Text("(00) 00000-0000") }
                        )

                        OutlinedTextField(
                            value = guardianEmail,
                            onValueChange = { guardianEmail = it },
                            label = { Text("Email do Responsável") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Email, null) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (currentStep < 3) {
                Button(onClick = { currentStep++ }) {
                    Text("Próximo")
                }
            } else {
                Button(
                    onClick = {
                        // Create student
                        val student = StudentExtended(
                            personalInfo = PersonalInfo(
                                fullName = fullName,
                                cpf = cpf,
                                rg = rg,
                                birthDate = birthDate,
                                gender = gender
                            ),
                            contactInfo = ContactInfo(
                                phone = phone,
                                email = email
                            ),
                            guardians = listOf(
                                Guardian(
                                    name = guardianName,
                                    relationship = guardianRelationship,
                                    phone = guardianPhone,
                                    email = guardianEmail,
                                    isFinancialResponsible = true
                                )
                            ),
                            academicInfo = AcademicInfo(
                                registrationNumber = registrationNumber,
                                className = className,
                                grade = grade,
                                period = period,
                                enrollmentDate = System.currentTimeMillis()
                            )
                        )
                        onCreate(student)
                    },
                    enabled = fullName.isNotBlank() &&
                            phone.isNotBlank() &&
                            registrationNumber.isNotBlank() &&
                            className.isNotBlank() &&
                            guardianName.isNotBlank() &&
                            guardianPhone.isNotBlank()
                ) {
                    Text("Criar")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("Voltar")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

// ==================== STUDENT DETAILS SHEET ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailsSheet(
    student: StudentExtended,
    stats: StudentStats?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: StudentViewModel
) {
    val timeline by viewModel.timeline.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentAvatar(
                        photoUrl = student.personalInfo.photoUrl,
                        name = student.personalInfo.fullName,
                        size = 64.dp
                    )

                    Column {
                        Text(
                            text = student.personalInfo.fullName,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "RA: ${student.academicInfo.registrationNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            "Deletar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Fechar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Informações") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Responsáveis") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Timeline") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Estatísticas") }
                )
            }

            // Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Basic info
                        item {
                            InfoSection(
                                title = "Dados Pessoais",
                                items = buildList {
                                    add("CPF" to student.personalInfo.getFormattedCpf())
                                    add("RG" to student.personalInfo.rg)
                                    add("Gênero" to student.personalInfo.gender.displayName)
                                    student.personalInfo.birthDate?.let {
                                        student.getAge()?.let { age ->
                                            add("Idade" to "$age anos")
                                        }
                                    }
                                }
                            )
                        }

                        item {
                            InfoSection(
                                title = "Contato",
                                items = listOf(
                                    "Telefone" to student.contactInfo.getFormattedPhone(),
                                    "Email" to student.contactInfo.email
                                )
                            )
                        }

                        item {
                            InfoSection(
                                title = "Acadêmico",
                                items = listOf(
                                    "Turma" to student.academicInfo.className,
                                    "Série" to student.academicInfo.grade,
                                    "Período" to student.academicInfo.period,
                                    "Status" to student.academicInfo.status.displayName
                                )
                            )
                        }
                    }

                    1 -> {
                        // Guardians
                        items(student.guardians) { guardian ->
                            GuardianCard(guardian = guardian)
                        }
                    }

                    2 -> {
                        // Timeline
                        items(timeline) { event ->
                            TimelineEventCard(event = event)
                        }
                    }

                    3 -> {
                        // Stats
                        if (stats != null) {
                            item {
                                StudentStatsCard(stats = stats)
                            }
                        } else {
                            item {
                                Text("Sem estatísticas disponíveis")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            items.forEach { (label, value) ->
                if (value.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
