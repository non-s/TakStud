package com.example.takstud.data.local.entity

import androidx.room.*
import com.example.takstud.model.grade.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 📊 ROOM ENTITIES PARA SISTEMA DE AVALIAÇÕES
 *
 * Otimizado para:
 * - Consultas rápidas
 * - Relacionamentos eficientes
 * - Sincronização offline-first
 */

// ==================== ASSESSMENT ENTITY ====================

@Entity(
    tableName = "assessments",
    indices = [
        Index(value = ["classId"]),
        Index(value = ["subjectId"]),
        Index(value = ["teacherId"]),
        Index(value = ["type"]),
        Index(value = ["status"]),
        Index(value = ["scheduledDate"]),
        Index(value = ["isActive"])
    ]
)
@TypeConverters(GradeTypeConverters::class)
data class AssessmentEntity(
    @PrimaryKey
    val id: String,

    // Informações básicas
    val title: String,
    val description: String,
    val type: String,                   // AssessmentType.name
    val status: String,                 // AssessmentStatus.name

    // Turma e disciplina
    val classId: String,
    val className: String,
    val subjectId: String,
    val subjectName: String,
    val teacherId: String,
    val teacherName: String,

    // Datas
    val createdAt: Long,
    val updatedAt: Long,
    val scheduledDate: Long?,
    val availableFrom: Long?,
    val dueDate: Long?,
    val publishGradesDate: Long?,

    // Configuração de pontuação
    val gradingScale: String,           // GradingScale.name
    val maxScore: Double,
    val passingScore: Double,
    val weight: Double,

    // Configurações
    val allowLateSubmission: Boolean,
    val latePenaltyPercentage: Double,
    val showGradesImmediately: Boolean,
    val allowRecovery: Boolean,
    val isRecovery: Boolean,
    val originalAssessmentId: String?,

    // Rubrica
    val rubricId: String?,
    val hasRubric: Boolean,

    // Estatísticas
    val totalStudents: Int,
    val submittedCount: Int,
    val gradedCount: Int,
    val averageScore: Double,
    val highestScore: Double,
    val lowestScore: Double,

    // Anexos e materiais (JSON)
    val attachmentsJson: String,        // List<String>
    val instructions: String,

    // Metadados (JSON)
    val tagsJson: String,               // List<String>
    val isActive: Boolean,

    // Sincronização
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toAssessment(): Assessment {
        val gson = Gson()
        val stringListType = object : TypeToken<List<String>>() {}.type

        return Assessment(
            id = id,
            title = title,
            description = description,
            type = AssessmentType.valueOf(type),
            status = AssessmentStatus.valueOf(status),
            classId = classId,
            className = className,
            subjectId = subjectId,
            subjectName = subjectName,
            teacherId = teacherId,
            teacherName = teacherName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            scheduledDate = scheduledDate,
            availableFrom = availableFrom,
            dueDate = dueDate,
            publishGradesDate = publishGradesDate,
            gradingScale = GradingScale.valueOf(gradingScale),
            maxScore = maxScore,
            passingScore = passingScore,
            weight = weight,
            allowLateSubmission = allowLateSubmission,
            latePenaltyPercentage = latePenaltyPercentage,
            showGradesImmediately = showGradesImmediately,
            allowRecovery = allowRecovery,
            isRecovery = isRecovery,
            originalAssessmentId = originalAssessmentId,
            rubricId = rubricId,
            hasRubric = hasRubric,
            totalStudents = totalStudents,
            submittedCount = submittedCount,
            gradedCount = gradedCount,
            averageScore = averageScore,
            highestScore = highestScore,
            lowestScore = lowestScore,
            attachments = gson.fromJson(attachmentsJson, stringListType) ?: emptyList(),
            instructions = instructions,
            tags = gson.fromJson(tagsJson, stringListType) ?: emptyList(),
            isActive = isActive
        )
    }

    companion object {
        fun fromAssessment(assessment: Assessment): AssessmentEntity {
            val gson = Gson()
            return AssessmentEntity(
                id = assessment.id,
                title = assessment.title,
                description = assessment.description,
                type = assessment.type.name,
                status = assessment.status.name,
                classId = assessment.classId,
                className = assessment.className,
                subjectId = assessment.subjectId,
                subjectName = assessment.subjectName,
                teacherId = assessment.teacherId,
                teacherName = assessment.teacherName,
                createdAt = assessment.createdAt,
                updatedAt = assessment.updatedAt,
                scheduledDate = assessment.scheduledDate,
                availableFrom = assessment.availableFrom,
                dueDate = assessment.dueDate,
                publishGradesDate = assessment.publishGradesDate,
                gradingScale = assessment.gradingScale.name,
                maxScore = assessment.maxScore,
                passingScore = assessment.passingScore,
                weight = assessment.weight,
                allowLateSubmission = assessment.allowLateSubmission,
                latePenaltyPercentage = assessment.latePenaltyPercentage,
                showGradesImmediately = assessment.showGradesImmediately,
                allowRecovery = assessment.allowRecovery,
                isRecovery = assessment.isRecovery,
                originalAssessmentId = assessment.originalAssessmentId,
                rubricId = assessment.rubricId,
                hasRubric = assessment.hasRubric,
                totalStudents = assessment.totalStudents,
                submittedCount = assessment.submittedCount,
                gradedCount = assessment.gradedCount,
                averageScore = assessment.averageScore,
                highestScore = assessment.highestScore,
                lowestScore = assessment.lowestScore,
                attachmentsJson = gson.toJson(assessment.attachments),
                instructions = assessment.instructions,
                tagsJson = gson.toJson(assessment.tags),
                isActive = assessment.isActive
            )
        }
    }
}

// ==================== GRADE ENTITY ====================

@Entity(
    tableName = "student_grades",
    indices = [
        Index(value = ["assessmentId"]),
        Index(value = ["studentId"]),
        Index(value = ["isPublished"]),
        Index(value = ["gradedAt"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = AssessmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assessmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(GradeTypeConverters::class)
data class DetailedGradeEntity(
    @PrimaryKey
    val id: String,

    // Relacionamentos
    val assessmentId: String,
    val studentId: String,
    val studentName: String,
    val studentRegistrationNumber: String,

    // Pontuação
    val score: Double?,
    val concept: String?,               // Concept.name
    val letterGrade: String?,
    val percentage: Double?,

    // Detalhes
    val feedback: String,
    val rubricScoresJson: String,       // Map<String, Double>
    val isAbsent: Boolean,
    val isExcused: Boolean,
    val isLateSubmission: Boolean,
    val penaltyApplied: Double,

    // Submissão
    val submittedAt: Long?,
    val submissionFilesJson: String,    // List<String>
    val submissionText: String,

    // Correção
    val gradedAt: Long?,
    val gradedBy: String?,
    val publishedAt: Long?,

    // Recuperação
    val hasRecovery: Boolean,
    val recoveryGradeId: String?,
    val finalScore: Double?,

    // Metadados
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String,
    val isPublished: Boolean,

    // Sincronização
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toGrade(): Grade {
        val gson = Gson()
        val stringListType = object : TypeToken<List<String>>() {}.type
        val mapType = object : TypeToken<Map<String, Double>>() {}.type

        return Grade(
            id = id,
            assessmentId = assessmentId,
            studentId = studentId,
            studentName = studentName,
            studentRegistrationNumber = studentRegistrationNumber,
            score = score,
            concept = concept?.let { Concept.valueOf(it) },
            letterGrade = letterGrade,
            percentage = percentage,
            feedback = feedback,
            rubricScores = gson.fromJson(rubricScoresJson, mapType) ?: emptyMap(),
            isAbsent = isAbsent,
            isExcused = isExcused,
            isLateSubmission = isLateSubmission,
            penaltyApplied = penaltyApplied,
            submittedAt = submittedAt,
            submissionFiles = gson.fromJson(submissionFilesJson, stringListType) ?: emptyList(),
            submissionText = submissionText,
            gradedAt = gradedAt,
            gradedBy = gradedBy,
            publishedAt = publishedAt,
            hasRecovery = hasRecovery,
            recoveryGradeId = recoveryGradeId,
            finalScore = finalScore,
            createdAt = createdAt,
            updatedAt = updatedAt,
            notes = notes,
            isPublished = isPublished
        )
    }

    companion object {
        fun fromGrade(grade: Grade): GradeEntity {
            val gson = Gson()
            return GradeEntity(
                id = grade.id,
                assessmentId = grade.assessmentId,
                studentId = grade.studentId,
                studentName = grade.studentName,
                studentRegistrationNumber = grade.studentRegistrationNumber,
                score = grade.score,
                concept = grade.concept?.name,
                letterGrade = grade.letterGrade,
                percentage = grade.percentage,
                feedback = grade.feedback,
                rubricScoresJson = gson.toJson(grade.rubricScores),
                isAbsent = grade.isAbsent,
                isExcused = grade.isExcused,
                isLateSubmission = grade.isLateSubmission,
                penaltyApplied = grade.penaltyApplied,
                submittedAt = grade.submittedAt,
                submissionFilesJson = gson.toJson(grade.submissionFiles),
                submissionText = grade.submissionText,
                gradedAt = grade.gradedAt,
                gradedBy = grade.gradedBy,
                publishedAt = grade.publishedAt,
                hasRecovery = grade.hasRecovery,
                recoveryGradeId = grade.recoveryGradeId,
                finalScore = grade.finalScore,
                createdAt = grade.createdAt,
                updatedAt = grade.updatedAt,
                notes = grade.notes,
                isPublished = grade.isPublished
            )
        }
    }
}

// ==================== TYPE CONVERTERS ====================

class GradeTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromMapStringDouble(value: Map<String, Double>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMapStringDouble(value: String): Map<String, Double> {
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }
}
