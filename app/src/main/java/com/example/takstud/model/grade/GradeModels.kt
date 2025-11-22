package com.example.takstud.model.grade

import java.util.UUID

/**
 * 📊 SISTEMA COMPLETO DE AVALIAÇÕES E NOTAS
 *
 * Suporta:
 * - Múltiplos tipos de avaliação (provas, trabalhos, participação, etc)
 * - Diferentes sistemas de pontuação (0-10, 0-100, conceitos A-F)
 * - Média ponderada customizável
 * - Recuperação e segunda chamada
 * - Pareceres descritivos
 * - Rubricas de avaliação
 */

// ==================== ENUMS ====================

/**
 * Tipo de avaliação
 */
enum class AssessmentType {
    EXAM,              // Prova
    TEST,              // Teste
    ASSIGNMENT,        // Trabalho
    PROJECT,           // Projeto
    PRESENTATION,      // Apresentação
    PARTICIPATION,     // Participação
    HOMEWORK,          // Dever de casa
    LAB_WORK,          // Trabalho de laboratório
    PORTFOLIO,         // Portfólio
    DEBATE,            // Debate
    SEMINAR,           // Seminário
    PRACTICAL_EXAM,    // Prova prática
    ORAL_EXAM,         // Prova oral
    GROUP_WORK,        // Trabalho em grupo
    SELF_ASSESSMENT,   // Autoavaliação
    PEER_ASSESSMENT,   // Avaliação por pares
    RECOVERY,          // Recuperação
    MAKEUP_EXAM,       // Segunda chamada
    FINAL_EXAM,        // Exame final
    OTHER              // Outro
}

/**
 * Sistema de pontuação
 */
enum class GradingScale {
    NUMERIC_0_10,      // 0.0 a 10.0
    NUMERIC_0_100,     // 0 a 100
    LETTER_A_F,        // A, B, C, D, F
    LETTER_PLUS_MINUS, // A+, A, A-, B+, B, etc
    CONCEPTS,          // Excelente, Bom, Regular, Insuficiente
    PASS_FAIL,         // Aprovado/Reprovado
    SATISFACTORY,      // Satisfatório/Insatisfatório
    CUSTOM             // Personalizado
}

/**
 * Conceitos (para GradingScale.CONCEPTS)
 */
enum class Concept {
    EXCELLENT,         // Excelente
    VERY_GOOD,         // Muito Bom
    GOOD,              // Bom
    REGULAR,           // Regular
    INSUFFICIENT,      // Insuficiente
    NOT_EVALUATED      // Não avaliado
}

/**
 * Status da avaliação
 */
enum class AssessmentStatus {
    DRAFT,             // Rascunho
    SCHEDULED,         // Agendada
    IN_PROGRESS,       // Em andamento
    COMPLETED,         // Concluída
    GRADING,           // Em correção
    GRADED,            // Corrigida
    PUBLISHED,         // Publicada
    CANCELLED          // Cancelada
}

// ==================== DOMAIN MODELS ====================

/**
 * Avaliação (prova, trabalho, etc)
 */
data class Assessment(
    val id: String = UUID.randomUUID().toString(),

    // Informações básicas
    val title: String,
    val description: String,
    val type: AssessmentType,
    val status: AssessmentStatus,

    // Turma e disciplina
    val classId: String,
    val className: String,
    val subjectId: String,
    val subjectName: String,
    val teacherId: String,
    val teacherName: String,

    // Datas
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val scheduledDate: Long?,           // Data agendada
    val availableFrom: Long?,           // Disponível a partir de
    val dueDate: Long?,                 // Prazo de entrega
    val publishGradesDate: Long?,       // Data de publicação das notas

    // Configuração de pontuação
    val gradingScale: GradingScale,
    val maxScore: Double,               // Pontuação máxima
    val passingScore: Double,           // Nota mínima para aprovação
    val weight: Double,                 // Peso na média (0.0 a 1.0)

    // Configurações
    val allowLateSubmission: Boolean = false,
    val latePenaltyPercentage: Double = 0.0,
    val showGradesImmediately: Boolean = false,
    val allowRecovery: Boolean = true,
    val isRecovery: Boolean = false,    // É uma recuperação?
    val originalAssessmentId: String? = null, // ID da avaliação original (se for recuperação)

    // Rubrica
    val rubricId: String? = null,
    val hasRubric: Boolean = false,

    // Estatísticas
    val totalStudents: Int = 0,
    val submittedCount: Int = 0,
    val gradedCount: Int = 0,
    val averageScore: Double = 0.0,
    val highestScore: Double = 0.0,
    val lowestScore: Double = 0.0,

    // Anexos e materiais
    val attachments: List<String> = emptyList(),
    val instructions: String = "",

    // Metadados
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true
)

/**
 * Nota individual de um aluno em uma avaliação
 */
data class Grade(
    val id: String = UUID.randomUUID().toString(),

    // Relacionamentos
    val assessmentId: String,
    val studentId: String,
    val studentName: String,
    val studentRegistrationNumber: String,

    // Pontuação
    val score: Double?,                 // Nota numérica
    val concept: Concept?,              // Conceito (se aplicável)
    val letterGrade: String?,           // Nota em letra (A, B, C, etc)
    val percentage: Double?,            // Porcentagem

    // Detalhes
    val feedback: String = "",          // Feedback do professor
    val rubricScores: Map<String, Double> = emptyMap(), // Pontuação por critério da rubrica
    val isAbsent: Boolean = false,      // Aluno faltou
    val isExcused: Boolean = false,     // Falta justificada
    val isLateSubmission: Boolean = false,
    val penaltyApplied: Double = 0.0,

    // Submissão (para trabalhos)
    val submittedAt: Long? = null,
    val submissionFiles: List<String> = emptyList(),
    val submissionText: String = "",

    // Correção
    val gradedAt: Long? = null,
    val gradedBy: String? = null,       // ID do professor que corrigiu
    val publishedAt: Long? = null,

    // Recuperação
    val hasRecovery: Boolean = false,
    val recoveryGradeId: String? = null,
    val finalScore: Double? = null,     // Nota final após recuperação

    // Metadados
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isPublished: Boolean = false
)

/**
 * Rubrica de avaliação
 */
data class Rubric(
    val id: String = UUID.randomUUID().toString(),

    // Informações básicas
    val title: String,
    val description: String,

    // Critérios
    val criteria: List<RubricCriterion>,

    // Configuração
    val totalPoints: Double,
    val isTemplate: Boolean = false,

    // Metadados
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String,
    val isActive: Boolean = true
)

/**
 * Critério de uma rubrica
 */
data class RubricCriterion(
    val id: String = UUID.randomUUID().toString(),

    // Informações
    val title: String,
    val description: String,
    val maxPoints: Double,
    val weight: Double = 1.0,

    // Níveis de desempenho
    val performanceLevels: List<PerformanceLevel>
)

/**
 * Nível de desempenho em um critério
 */
data class PerformanceLevel(
    val id: String = UUID.randomUUID().toString(),
    val title: String,              // Ex: "Excelente", "Bom", "Regular"
    val description: String,
    val points: Double,
    val order: Int
)

/**
 * Boletim de um aluno (consolidação de todas as notas)
 */
data class ReportCard(
    val id: String = UUID.randomUUID().toString(),

    // Aluno
    val studentId: String,
    val studentName: String,
    val studentRegistrationNumber: String,

    // Período
    val academicYear: Int,
    val semester: Int,
    val quarter: Int? = null,

    // Turma
    val classId: String,
    val className: String,

    // Notas por disciplina
    val subjectGrades: List<SubjectGrade>,

    // Médias
    val overallAverage: Double,
    val gpa: Double,                    // GPA (Grade Point Average)

    // Status
    val isPassed: Boolean,
    val hasRecovery: Boolean,
    val isPublished: Boolean,

    // Pareceres
    val teacherComments: Map<String, String> = emptyMap(), // subjectId -> comment
    val coordinatorComment: String = "",
    val principalComment: String = "",

    // Metadados
    val generatedAt: Long = System.currentTimeMillis(),
    val publishedAt: Long? = null
)

/**
 * Nota de uma disciplina no boletim
 */
data class SubjectGrade(
    val subjectId: String,
    val subjectName: String,

    // Notas
    val assessmentGrades: List<AssessmentGradeSummary>,

    // Médias
    val average: Double,
    val weightedAverage: Double,

    // Status
    val isPassed: Boolean,
    val needsRecovery: Boolean,
    val recoveryScore: Double? = null,
    val finalScore: Double,

    // Frequência
    val attendanceRate: Double,
    val totalClasses: Int,
    val attendedClasses: Int,
    val absences: Int,

    // Comentário do professor
    val teacherComment: String = ""
)

/**
 * Resumo de uma avaliação no boletim
 */
data class AssessmentGradeSummary(
    val assessmentId: String,
    val assessmentTitle: String,
    val assessmentType: AssessmentType,
    val score: Double,
    val maxScore: Double,
    val weight: Double,
    val date: Long
)

/**
 * Estatísticas de desempenho de uma turma
 */
data class ClassPerformanceStats(
    val classId: String,
    val className: String,
    val subjectId: String,
    val subjectName: String,
    val assessmentId: String,
    val assessmentTitle: String,

    // Estatísticas
    val totalStudents: Int,
    val gradedStudents: Int,
    val averageScore: Double,
    val medianScore: Double,
    val highestScore: Double,
    val lowestScore: Double,
    val standardDeviation: Double,

    // Distribuição
    val scoreDistribution: Map<String, Int>, // faixa -> quantidade
    val passRate: Double,
    val failRate: Double,

    // Comparações
    val comparedToClassAverage: Double,
    val comparedToSchoolAverage: Double
)
