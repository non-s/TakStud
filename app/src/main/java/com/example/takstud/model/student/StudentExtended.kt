package com.example.takstud.model.student

import java.util.UUID

/**
 * 👨‍🎓 Aluno Completo - Versão Expandida
 * Perfil completo com todas as informações necessárias
 */
data class StudentExtended(
    val id: String = UUID.randomUUID().toString(),

    // ===== DADOS PESSOAIS =====
    val personalInfo: PersonalInfo,

    // ===== DADOS DE CONTATO =====
    val contactInfo: ContactInfo,

    // ===== RESPONSÁVEIS (múltiplos) =====
    val guardians: List<Guardian> = emptyList(),

    // ===== DADOS ACADÊMICOS =====
    val academicInfo: AcademicInfo,

    // ===== DADOS DE SAÚDE =====
    val healthInfo: HealthInfo? = null,

    // ===== DOCUMENTOS =====
    val documents: List<StudentDocument> = emptyList(),

    // ===== OBSERVAÇÕES E ANOTAÇÕES =====
    val observations: List<Observation> = emptyList(),

    // ===== TAGS/ETIQUETAS =====
    val tags: List<String> = emptyList(),

    // ===== METADATA =====
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val isActive: Boolean = true
) {
    /**
     * Retorna nome completo do aluno
     */
    fun getFullName(): String = personalInfo.fullName

    /**
     * Retorna idade do aluno
     */
    fun getAge(): Int? {
        return personalInfo.birthDate?.let { birthDate ->
            val today = System.currentTimeMillis()
            val age = (today - birthDate) / (1000L * 60 * 60 * 24 * 365)
            age.toInt()
        }
    }

    /**
     * Retorna responsável principal (financeiro)
     */
    fun getPrimaryGuardian(): Guardian? {
        return guardians.find { it.isFinancialResponsible }
            ?: guardians.firstOrNull()
    }

    /**
     * Retorna todos os contatos de emergência
     */
    fun getEmergencyContacts(): List<EmergencyContact> {
        return healthInfo?.emergencyContacts ?: emptyList()
    }

    /**
     * Verifica se tem necessidades especiais
     */
    fun hasSpecialNeeds(): Boolean {
        return healthInfo?.specialNeeds?.isNotEmpty() == true
    }

    /**
     * Validação completa
     */
    fun isValid(): Boolean {
        return personalInfo.isValid() &&
               contactInfo.isValid() &&
               academicInfo.isValid() &&
               guardians.isNotEmpty() &&
               guardians.any { it.isValid() }
    }
}

/**
 * 📝 Informações Pessoais
 */
data class PersonalInfo(
    val fullName: String = "",
    val preferredName: String = "",           // Nome social/preferido
    val birthDate: Long? = null,              // Timestamp
    val birthPlace: String = "",              // Local de nascimento
    val cpf: String = "",
    val rg: String = "",
    val rgIssuer: String = "",                // Órgão emissor
    val rgIssueDate: Long? = null,
    val birthCertificate: String = "",        // Número da certidão
    val gender: Gender = Gender.NOT_SPECIFIED,
    val nationality: String = "Brasileiro(a)",
    val photoUrl: String = "",                // URL da foto
    val bloodType: BloodType? = null
) {
    fun isValid(): Boolean {
        return fullName.isNotBlank()
    }

    fun getFormattedCpf(): String {
        if (cpf.length != 11) return cpf
        return "${cpf.substring(0, 3)}.${cpf.substring(3, 6)}.${cpf.substring(6, 9)}-${cpf.substring(9)}"
    }
}

enum class Gender(val displayName: String) {
    MALE("Masculino"),
    FEMALE("Feminino"),
    OTHER("Outro"),
    NOT_SPECIFIED("Não especificado")
}

enum class BloodType(val displayName: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-")
}

/**
 * 📞 Informações de Contato
 */
data class ContactInfo(
    val phone: String = "",                   // Telefone principal
    val phoneSecondary: String = "",          // Telefone secundário
    val email: String = "",
    val address: Address? = null
) {
    fun isValid(): Boolean {
        return phone.isNotBlank() || email.isNotBlank()
    }

    fun getFormattedPhone(): String {
        if (phone.length != 11) return phone
        return "(${phone.substring(0, 2)}) ${phone.substring(2, 7)}-${phone.substring(7)}"
    }
}

/**
 * 🏠 Endereço
 */
data class Address(
    val zipCode: String = "",                 // CEP
    val street: String = "",                  // Rua
    val number: String = "",                  // Número
    val complement: String = "",              // Complemento
    val neighborhood: String = "",            // Bairro
    val city: String = "",
    val state: String = "",
    val country: String = "Brasil"
) {
    fun getFullAddress(): String {
        return buildString {
            if (street.isNotBlank()) append("$street, ")
            if (number.isNotBlank()) append("$number ")
            if (complement.isNotBlank()) append("($complement) ")
            if (neighborhood.isNotBlank()) append("- $neighborhood, ")
            if (city.isNotBlank()) append("$city")
            if (state.isNotBlank()) append("/$state")
        }
    }

    fun getFormattedZipCode(): String {
        if (zipCode.length != 8) return zipCode
        return "${zipCode.substring(0, 5)}-${zipCode.substring(5)}"
    }
}

/**
 * 👨‍👩‍👧 Responsável
 */
data class Guardian(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val relationship: GuardianRelationship = GuardianRelationship.OTHER,
    val cpf: String = "",
    val rg: String = "",
    val phone: String = "",
    val phoneSecondary: String = "",
    val email: String = "",
    val address: Address? = null,              // Pode ser diferente do aluno
    val occupation: String = "",                // Profissão
    val workplace: String = "",                 // Local de trabalho
    val workPhone: String = "",                 // Telefone comercial
    val isFinancialResponsible: Boolean = false, // Responsável financeiro
    val isAuthorizedToPickup: Boolean = true,   // Autorizado a buscar
    val photoUrl: String = ""
) {
    fun isValid(): Boolean {
        return name.isNotBlank() &&
               phone.isNotBlank() &&
               relationship != GuardianRelationship.OTHER
    }

    fun getFormattedPhone(): String {
        if (phone.length != 11) return phone
        return "(${phone.substring(0, 2)}) ${phone.substring(2, 7)}-${phone.substring(7)}"
    }
}

enum class GuardianRelationship(val displayName: String) {
    MOTHER("Mãe"),
    FATHER("Pai"),
    GRANDMOTHER("Avó"),
    GRANDFATHER("Avô"),
    AUNT("Tia"),
    UNCLE("Tio"),
    SIBLING("Irmão/Irmã"),
    LEGAL_GUARDIAN("Tutor Legal"),
    OTHER("Outro")
}

/**
 * 🎓 Informações Acadêmicas
 */
data class AcademicInfo(
    val registrationNumber: String = "",      // RA/Matrícula
    val enrollmentDate: Long? = null,         // Data de matrícula
    val className: String = "",               // Turma atual
    val grade: String = "",                   // Série/Ano
    val period: String = "",                  // Manhã/Tarde/Noturno
    val status: StudentStatus = StudentStatus.ACTIVE,
    val previousSchool: String = "",          // Escola de origem
    val previousGrade: String = "",           // Série anterior
    val transferDate: Long? = null,           // Data de transferência (se aplicável)
    val graduationDate: Long? = null,         // Data de conclusão
    val isScholarship: Boolean = false,       // Bolsista?
    val scholarshipPercentage: Int = 0,       // Percentual de bolsa
    val gpa: Double = 0.0,                    // Média geral
    val attendanceRate: Double = 0.0          // Taxa de presença
) {
    fun isValid(): Boolean {
        return registrationNumber.isNotBlank() && className.isNotBlank()
    }

    fun getStatusColor(): String {
        return when (status) {
            StudentStatus.ACTIVE -> "#4CAF50"
            StudentStatus.INACTIVE -> "#9E9E9E"
            StudentStatus.TRANSFERRED -> "#2196F3"
            StudentStatus.DROPPED_OUT -> "#F44336"
            StudentStatus.GRADUATED -> "#FF9800"
            StudentStatus.SUSPENDED -> "#FFC107"
        }
    }
}

enum class StudentStatus(val displayName: String) {
    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    TRANSFERRED("Transferido"),
    DROPPED_OUT("Evadido"),
    GRADUATED("Concluído"),
    SUSPENDED("Suspenso")
}

/**
 * 🏥 Informações de Saúde
 */
data class HealthInfo(
    val bloodType: BloodType? = null,
    val allergies: List<String> = emptyList(),
    val medicalConditions: List<String> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val specialNeeds: List<SpecialNeed> = emptyList(),
    val disabilities: List<String> = emptyList(),
    val specializedCare: String = "",         // Acompanhamento especializado
    val dietaryRestrictions: List<String> = emptyList(),
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val healthInsurance: String = "",         // Plano de saúde
    val healthInsuranceNumber: String = "",
    val observations: String = ""
) {
    fun hasCriticalConditions(): Boolean {
        return medicalConditions.isNotEmpty() ||
               medications.any { it.isContinuous } ||
               specialNeeds.isNotEmpty()
    }
}

data class Medication(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val isContinuous: Boolean = false,
    val observations: String = ""
)

data class SpecialNeed(
    val type: SpecialNeedType = SpecialNeedType.OTHER,
    val description: String = "",
    val accommodations: List<String> = emptyList() // Acomodações necessárias
)

enum class SpecialNeedType(val displayName: String) {
    PHYSICAL("Física"),
    VISUAL("Visual"),
    HEARING("Auditiva"),
    INTELLECTUAL("Intelectual"),
    LEARNING("Aprendizagem"),
    BEHAVIORAL("Comportamental"),
    AUTISM("Autismo"),
    ADHD("TDAH"),
    DYSLEXIA("Dislexia"),
    OTHER("Outra")
}

data class EmergencyContact(
    val name: String = "",
    val relationship: String = "",
    val phone: String = "",
    val phoneSecondary: String = "",
    val isPriority: Boolean = false           // Contato prioritário
)

/**
 * 📄 Documento
 */
data class StudentDocument(
    val id: String = UUID.randomUUID().toString(),
    val type: DocumentType = DocumentType.OTHER,
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val uploadedBy: String = "",
    val expirationDate: Long? = null          // Data de validade
) {
    fun isExpired(): Boolean {
        return expirationDate?.let { it < System.currentTimeMillis() } ?: false
    }

    fun getFormattedSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }
}

enum class DocumentType(val displayName: String) {
    BIRTH_CERTIFICATE("Certidão de Nascimento"),
    RG("RG"),
    CPF("CPF"),
    PHOTO("Foto"),
    MEDICAL_CERTIFICATE("Atestado Médico"),
    MEDICAL_REPORT("Laudo Médico"),
    VACCINATION_CARD("Cartão de Vacinação"),
    PROOF_OF_RESIDENCE("Comprovante de Residência"),
    TRANSCRIPT("Histórico Escolar"),
    TRANSFER_DOCUMENT("Documento de Transferência"),
    DECLARATION("Declaração"),
    OTHER("Outro")
}

/**
 * 📝 Observação/Anotação
 */
data class Observation(
    val id: String = UUID.randomUUID().toString(),
    val type: ObservationType = ObservationType.GENERAL,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val createdByName: String = "",
    val isPrivate: Boolean = false,           // Visível apenas para professores/admin
    val attachments: List<String> = emptyList()
)

enum class ObservationType(val displayName: String, val icon: String) {
    GENERAL("Geral", "📝"),
    ACADEMIC("Acadêmica", "📚"),
    BEHAVIORAL("Comportamental", "🎭"),
    HEALTH("Saúde", "🏥"),
    SOCIAL("Social", "👥"),
    ACHIEVEMENT("Conquista", "🏆"),
    CONCERN("Preocupação", "⚠️"),
    MEETING("Reunião", "🤝")
}

/**
 * 📊 Estatísticas do Aluno
 */
data class StudentStats(
    val totalClasses: Int = 0,
    val attendedClasses: Int = 0,
    val absentClasses: Int = 0,
    val attendanceRate: Double = 0.0,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val taskCompletionRate: Double = 0.0,
    val averageGrade: Double = 0.0,
    val highestGrade: Double = 0.0,
    val lowestGrade: Double = 0.0,
    val totalDisciplines: Int = 0,
    val approvedDisciplines: Int = 0,
    val failedDisciplines: Int = 0
) {
    fun getPerformanceLevel(): PerformanceLevel {
        return when {
            averageGrade >= 9.0 -> PerformanceLevel.EXCELLENT
            averageGrade >= 7.0 -> PerformanceLevel.GOOD
            averageGrade >= 5.0 -> PerformanceLevel.AVERAGE
            else -> PerformanceLevel.BELOW_AVERAGE
        }
    }
}

enum class PerformanceLevel(val displayName: String, val color: String) {
    EXCELLENT("Excelente", "#4CAF50"),
    GOOD("Bom", "#8BC34A"),
    AVERAGE("Regular", "#FFC107"),
    BELOW_AVERAGE("Abaixo da Média", "#F44336")
}

/**
 * 📅 Evento da Timeline
 */
data class StudentTimelineEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: TimelineEventType = TimelineEventType.OTHER,
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val metadata: Map<String, String> = emptyMap()
)

enum class TimelineEventType(val displayName: String, val icon: String) {
    ENROLLMENT("Matrícula", "✅"),
    CLASS_CHANGE("Mudança de Turma", "🔄"),
    GRADE_CHANGE("Mudança de Série", "📈"),
    DISCIPLINARY("Ocorrência Disciplinar", "⚠️"),
    ACHIEVEMENT("Conquista", "🏆"),
    MEETING("Reunião com Pais", "🤝"),
    MEDICAL("Ocorrência Médica", "🏥"),
    TRANSFER("Transferência", "🚚"),
    DROPOUT("Evasão", "😢"),
    GRADUATION("Conclusão", "🎓"),
    OTHER("Outro", "📌")
}
