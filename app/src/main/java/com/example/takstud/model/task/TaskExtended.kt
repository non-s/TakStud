package com.example.takstud.model.task

import java.util.UUID

/**
 * 📝 Tarefa Completa - Versão Expandida
 * Sistema completo de gerenciamento de tarefas e atividades
 */
data class TaskExtended(
    val id: String = UUID.randomUUID().toString(),

    // ===== INFORMAÇÕES BÁSICAS =====
    val title: String = "",
    val description: String = "",
    val type: TaskType = TaskType.EXERCISE,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,

    // ===== DATAS E PRAZOS =====
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,                    // Data de entrega
    val availableFrom: Long? = null,              // Disponível a partir de
    val closeDate: Long? = null,                  // Data de fechamento (não aceita mais)

    // ===== TURMA E DISCIPLINA =====
    val classId: String = "",
    val className: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val teacherId: String = "",
    val teacherName: String = "",

    // ===== PONTUAÇÃO =====
    val maxPoints: Double = 10.0,                 // Pontuação máxima
    val weight: Double = 1.0,                     // Peso na média
    val passingGrade: Double = 6.0,               // Nota mínima para aprovação
    val allowLateSubmission: Boolean = true,      // Permite entrega atrasada?
    val latePenalty: Double = 0.0,                // Penalidade por atraso (%)

    // ===== CONFIGURAÇÕES =====
    val allowMultipleAttempts: Boolean = false,   // Permite múltiplas tentativas?
    val maxAttempts: Int = 1,                     // Número máximo de tentativas
    val showGradeAfterSubmission: Boolean = true, // Mostra nota após submissão?
    val requireFile: Boolean = false,             // Exige anexo de arquivo?
    val allowedFileTypes: List<String> = emptyList(), // Tipos de arquivo permitidos
    val maxFileSize: Long = 10 * 1024 * 1024,    // Tamanho máximo (10MB)

    // ===== CONTEÚDO =====
    val instructions: String = "",                // Instruções detalhadas
    val attachments: List<TaskAttachment> = emptyList(), // Anexos do professor
    val rubric: TaskRubric? = null,               // Rubrica de avaliação
    val tags: List<String> = emptyList(),         // Tags para organização

    // ===== SUBMISSÕES =====
    val submissions: List<TaskSubmission> = emptyList(), // Submissões dos alunos
    val totalSubmissions: Int = 0,                // Total de submissões
    val gradedSubmissions: Int = 0,               // Submissões corrigidas
    val pendingGrading: Int = 0,                  // Pendentes de correção

    // ===== ESTATÍSTICAS =====
    val averageGrade: Double = 0.0,               // Média das notas
    val highestGrade: Double = 0.0,               // Maior nota
    val lowestGrade: Double = 0.0,                // Menor nota
    val completionRate: Double = 0.0,             // Taxa de conclusão (%)

    // ===== METADATA =====
    val createdBy: String = "",
    val lastModifiedBy: String = "",
    val isActive: Boolean = true,
    val isPublished: Boolean = false,             // Publicada para os alunos?
    val isDraft: Boolean = false                  // Rascunho?
) {
    /**
     * Verifica se a tarefa está atrasada
     */
    fun isOverdue(): Boolean {
        val now = System.currentTimeMillis()
        return dueDate?.let { it < now && status != TaskStatus.COMPLETED } ?: false
    }

    /**
     * Verifica se a tarefa está disponível
     */
    fun isAvailable(): Boolean {
        val now = System.currentTimeMillis()
        val afterStart = availableFrom?.let { now >= it } ?: true
        val beforeClose = closeDate?.let { now <= it } ?: true
        return afterStart && beforeClose && isPublished
    }

    /**
     * Verifica se ainda aceita submissões
     */
    fun acceptsSubmissions(): Boolean {
        if (!isPublished || !isActive) return false
        if (!allowLateSubmission && isOverdue()) return false

        val now = System.currentTimeMillis()
        return closeDate?.let { now <= it } ?: true
    }

    /**
     * Calcula o tempo restante em milissegundos
     */
    fun timeRemaining(): Long? {
        return dueDate?.let { it - System.currentTimeMillis() }
    }

    /**
     * Retorna o tempo restante formatado
     */
    fun getFormattedTimeRemaining(): String {
        val remaining = timeRemaining() ?: return "-"

        if (remaining < 0) return "Atrasada"

        val days = remaining / (1000 * 60 * 60 * 24)
        val hours = (remaining % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (remaining % (1000 * 60 * 60)) / (1000 * 60)

        return when {
            days > 0 -> "$days dia(s)"
            hours > 0 -> "$hours hora(s)"
            else -> "$minutes minuto(s)"
        }
    }

    /**
     * Validação completa
     */
    fun isValid(): Boolean {
        return title.isNotBlank() &&
               className.isNotBlank() &&
               subjectName.isNotBlank() &&
               maxPoints > 0 &&
               weight > 0
    }

    /**
     * Calcula nota com penalidade de atraso
     */
    fun calculateFinalGrade(originalGrade: Double, submittedAt: Long): Double {
        if (!allowLateSubmission || latePenalty == 0.0) return originalGrade

        dueDate?.let { due ->
            if (submittedAt > due) {
                val penalty = (latePenalty / 100.0)
                return (originalGrade * (1.0 - penalty)).coerceAtLeast(0.0)
            }
        }

        return originalGrade
    }
}

// ==================== TASK TYPE ====================

enum class TaskType(val displayName: String, val icon: String) {
    EXERCISE("Exercício", "📝"),
    HOMEWORK("Dever de Casa", "📚"),
    TEST("Prova", "📄"),
    QUIZ("Quiz", "❓"),
    PROJECT("Projeto", "💡"),
    PRESENTATION("Apresentação", "🎤"),
    ESSAY("Redação", "✍️"),
    RESEARCH("Pesquisa", "🔍"),
    LAB("Laboratório", "🔬"),
    READING("Leitura", "📖"),
    VIDEO("Vídeo", "🎥"),
    DISCUSSION("Discussão", "💬"),
    OTHER("Outro", "📌")
}

// ==================== TASK STATUS ====================

enum class TaskStatus(val displayName: String, val color: String) {
    PENDING("Pendente", "#FFC107"),
    IN_PROGRESS("Em Andamento", "#2196F3"),
    SUBMITTED("Enviada", "#9C27B0"),
    GRADED("Corrigida", "#4CAF50"),
    OVERDUE("Atrasada", "#F44336"),
    COMPLETED("Concluída", "#4CAF50"),
    CANCELLED("Cancelada", "#9E9E9E"),
    DRAFT("Rascunho", "#757575")
}

// ==================== TASK PRIORITY ====================

enum class TaskPriority(val displayName: String, val color: String, val level: Int) {
    LOW("Baixa", "#4CAF50", 1),
    MEDIUM("Média", "#FFC107", 2),
    HIGH("Alta", "#FF9800", 3),
    URGENT("Urgente", "#F44336", 4)
}

// ==================== TASK ATTACHMENT ====================

data class TaskAttachment(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String = "",
    val name: String = "",
    val description: String = "",
    val url: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val uploadedBy: String = "",
    val uploadedByName: String = ""
) {
    fun getFormattedSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }

    fun getFileExtension(): String {
        return fileName.substringAfterLast('.', "")
    }
}

// ==================== TASK SUBMISSION ====================

data class TaskSubmission(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentRa: String = "",

    // ===== SUBMISSÃO =====
    val submittedAt: Long? = null,
    val content: String = "",                     // Texto da resposta
    val attachments: List<SubmissionAttachment> = emptyList(),
    val attemptNumber: Int = 1,                   // Número da tentativa

    // ===== AVALIAÇÃO =====
    val grade: Double? = null,                    // Nota recebida
    val maxGrade: Double = 10.0,                  // Nota máxima
    val feedback: String = "",                    // Feedback do professor
    val gradedAt: Long? = null,
    val gradedBy: String = "",
    val gradedByName: String = "",

    // ===== STATUS =====
    val status: SubmissionStatus = SubmissionStatus.NOT_SUBMITTED,
    val isLate: Boolean = false,                  // Entregue atrasado?
    val latePenaltyApplied: Double = 0.0,         // Penalidade aplicada

    // ===== METADATA =====
    val comments: List<SubmissionComment> = emptyList(),
    val revisions: List<SubmissionRevision> = emptyList(),
    val lastModifiedAt: Long = System.currentTimeMillis()
) {
    /**
     * Verifica se foi submetida
     */
    fun isSubmitted(): Boolean {
        return status != SubmissionStatus.NOT_SUBMITTED
    }

    /**
     * Verifica se foi corrigida
     */
    fun isGraded(): Boolean {
        return grade != null && status == SubmissionStatus.GRADED
    }

    /**
     * Calcula porcentagem da nota
     */
    fun getGradePercentage(): Double? {
        return grade?.let { (it / maxGrade) * 100.0 }
    }

    /**
     * Verifica se foi aprovado
     */
    fun isPassing(passingGrade: Double): Boolean {
        return grade?.let { it >= passingGrade } ?: false
    }
}

enum class SubmissionStatus(val displayName: String) {
    NOT_SUBMITTED("Não Enviada"),
    SUBMITTED("Enviada"),
    GRADED("Corrigida"),
    RETURNED("Devolvida"),
    RESUBMIT_REQUESTED("Reenviada Solicitada")
}

// ==================== SUBMISSION ATTACHMENT ====================

data class SubmissionAttachment(
    val id: String = UUID.randomUUID().toString(),
    val submissionId: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val uploadedAt: Long = System.currentTimeMillis()
) {
    fun getFormattedSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }
    }
}

// ==================== SUBMISSION COMMENT ====================

data class SubmissionComment(
    val id: String = UUID.randomUUID().toString(),
    val submissionId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = false                // Visível apenas para professor?
)

// ==================== SUBMISSION REVISION ====================

data class SubmissionRevision(
    val id: String = UUID.randomUUID().toString(),
    val submissionId: String = "",
    val attemptNumber: Int = 1,
    val submittedAt: Long = System.currentTimeMillis(),
    val content: String = "",
    val attachments: List<String> = emptyList()
)

// ==================== TASK RUBRIC ====================

/**
 * Rubrica de avaliação com critérios
 */
data class TaskRubric(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val criteria: List<RubricCriterion> = emptyList(),
    val totalPoints: Double = 10.0
) {
    fun isValid(): Boolean {
        return criteria.isNotEmpty() &&
               criteria.sumOf { it.maxPoints } == totalPoints
    }
}

data class RubricCriterion(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val maxPoints: Double = 0.0,
    val levels: List<RubricLevel> = emptyList()
)

data class RubricLevel(
    val name: String = "",                        // Ex: "Excelente", "Bom", "Regular"
    val description: String = "",
    val points: Double = 0.0
)

// ==================== TASK REMINDER ====================

data class TaskReminder(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String = "",
    val remindAt: Long = 0L,                      // Quando lembrar
    val message: String = "",
    val sent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== TASK STATS ====================

data class TaskStats(
    val taskId: String = "",

    // Submissões
    val totalStudents: Int = 0,
    val totalSubmissions: Int = 0,
    val lateSubmissions: Int = 0,
    val pendingSubmissions: Int = 0,
    val gradedSubmissions: Int = 0,

    // Notas
    val averageGrade: Double = 0.0,
    val medianGrade: Double = 0.0,
    val highestGrade: Double = 0.0,
    val lowestGrade: Double = 0.0,
    val standardDeviation: Double = 0.0,

    // Taxas
    val submissionRate: Double = 0.0,             // % de alunos que enviaram
    val completionRate: Double = 0.0,             // % de alunos que completaram
    val passingRate: Double = 0.0,                // % de alunos aprovados

    // Tempo
    val averageSubmissionTime: Long = 0L,         // Tempo médio até submissão
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Calcula distribuição de notas
     */
    fun getGradeDistribution(): Map<String, Int> {
        // TODO: Implementar quando tiver acesso às submissões
        return emptyMap()
    }

    /**
     * Identifica alunos em risco
     */
    fun getStudentsAtRisk(): List<String> {
        // Alunos que não enviaram ou tiraram nota baixa
        // TODO: Implementar quando tiver acesso aos dados
        return emptyList()
    }
}

// ==================== TASK FILTER ====================

data class TaskFilter(
    val type: TaskType? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val classId: String? = null,
    val subjectId: String? = null,
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val isOverdue: Boolean? = null,
    val isPublished: Boolean? = null,
    val tags: List<String> = emptyList()
)

// ==================== TASK SORT ====================

enum class TaskSortOption {
    TITLE_ASC,
    TITLE_DESC,
    DUE_DATE_ASC,
    DUE_DATE_DESC,
    CREATED_ASC,
    CREATED_DESC,
    PRIORITY_DESC,
    PRIORITY_ASC,
    STATUS,
    COMPLETION_RATE_DESC
}
