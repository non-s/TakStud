package com.example.takstud.data.local.entity

import androidx.room.*
import com.example.takstud.model.task.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 🗄️ Room Entities para Tarefas
 */

// ==================== TASK ENTITY ====================

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["classId"]),
        Index(value = ["subjectId"]),
        Index(value = ["teacherId"]),
        Index(value = ["dueDate"]),
        Index(value = ["status"]),
        Index(value = ["type"]),
        Index(value = ["isPublished"]),
        Index(value = ["isActive"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,

    // Basic Info
    val title: String,
    val description: String,
    val type: String,                             // TaskType enum name
    val status: String,                           // TaskStatus enum name
    val priority: String,                         // TaskPriority enum name

    // Dates
    val createdAt: Long,
    val updatedAt: Long,
    val dueDate: Long?,
    val availableFrom: Long?,
    val closeDate: Long?,

    // Class & Subject
    val classId: String,
    val className: String,
    val subjectId: String,
    val subjectName: String,
    val teacherId: String,
    val teacherName: String,

    // Grading
    val maxPoints: Double,
    val weight: Double,
    val passingGrade: Double,
    val allowLateSubmission: Boolean,
    val latePenalty: Double,

    // Settings
    val allowMultipleAttempts: Boolean,
    val maxAttempts: Int,
    val showGradeAfterSubmission: Boolean,
    val requireFile: Boolean,
    val allowedFileTypesJson: String,            // List<String> as JSON
    val maxFileSize: Long,

    // Content
    val instructions: String,
    val attachmentsJson: String,                  // List<TaskAttachment> as JSON
    val rubricJson: String?,                      // TaskRubric as JSON
    val tagsJson: String,                         // List<String> as JSON

    // Statistics
    val totalSubmissions: Int,
    val gradedSubmissions: Int,
    val pendingGrading: Int,
    val averageGrade: Double,
    val highestGrade: Double,
    val lowestGrade: Double,
    val completionRate: Double,

    // Metadata
    val createdBy: String,
    val lastModifiedBy: String,
    val isActive: Boolean,
    val isPublished: Boolean,
    val isDraft: Boolean
) {
    fun toDomain(): TaskExtended {
        val gson = Gson()

        return TaskExtended(
            id = id,
            title = title,
            description = description,
            type = TaskType.valueOf(type),
            status = TaskStatus.valueOf(status),
            priority = TaskPriority.valueOf(priority),
            createdAt = createdAt,
            updatedAt = updatedAt,
            dueDate = dueDate,
            availableFrom = availableFrom,
            closeDate = closeDate,
            classId = classId,
            className = className,
            subjectId = subjectId,
            subjectName = subjectName,
            teacherId = teacherId,
            teacherName = teacherName,
            maxPoints = maxPoints,
            weight = weight,
            passingGrade = passingGrade,
            allowLateSubmission = allowLateSubmission,
            latePenalty = latePenalty,
            allowMultipleAttempts = allowMultipleAttempts,
            maxAttempts = maxAttempts,
            showGradeAfterSubmission = showGradeAfterSubmission,
            requireFile = requireFile,
            allowedFileTypes = gson.fromJson(
                allowedFileTypesJson,
                object : TypeToken<List<String>>() {}.type
            ) ?: emptyList(),
            maxFileSize = maxFileSize,
            instructions = instructions,
            attachments = gson.fromJson(
                attachmentsJson,
                object : TypeToken<List<TaskAttachment>>() {}.type
            ) ?: emptyList(),
            rubric = rubricJson?.let { gson.fromJson(it, TaskRubric::class.java) },
            tags = gson.fromJson(
                tagsJson,
                object : TypeToken<List<String>>() {}.type
            ) ?: emptyList(),
            totalSubmissions = totalSubmissions,
            gradedSubmissions = gradedSubmissions,
            pendingGrading = pendingGrading,
            averageGrade = averageGrade,
            highestGrade = highestGrade,
            lowestGrade = lowestGrade,
            completionRate = completionRate,
            createdBy = createdBy,
            lastModifiedBy = lastModifiedBy,
            isActive = isActive,
            isPublished = isPublished,
            isDraft = isDraft
        )
    }

    companion object {
        fun fromDomain(task: TaskExtended): TaskEntity {
            val gson = Gson()

            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                type = task.type.name,
                status = task.status.name,
                priority = task.priority.name,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
                dueDate = task.dueDate,
                availableFrom = task.availableFrom,
                closeDate = task.closeDate,
                classId = task.classId,
                className = task.className,
                subjectId = task.subjectId,
                subjectName = task.subjectName,
                teacherId = task.teacherId,
                teacherName = task.teacherName,
                maxPoints = task.maxPoints,
                weight = task.weight,
                passingGrade = task.passingGrade,
                allowLateSubmission = task.allowLateSubmission,
                latePenalty = task.latePenalty,
                allowMultipleAttempts = task.allowMultipleAttempts,
                maxAttempts = task.maxAttempts,
                showGradeAfterSubmission = task.showGradeAfterSubmission,
                requireFile = task.requireFile,
                allowedFileTypesJson = gson.toJson(task.allowedFileTypes),
                maxFileSize = task.maxFileSize,
                instructions = task.instructions,
                attachmentsJson = gson.toJson(task.attachments),
                rubricJson = task.rubric?.let { gson.toJson(it) },
                tagsJson = gson.toJson(task.tags),
                totalSubmissions = task.totalSubmissions,
                gradedSubmissions = task.gradedSubmissions,
                pendingGrading = task.pendingGrading,
                averageGrade = task.averageGrade,
                highestGrade = task.highestGrade,
                lowestGrade = task.lowestGrade,
                completionRate = task.completionRate,
                createdBy = task.createdBy,
                lastModifiedBy = task.lastModifiedBy,
                isActive = task.isActive,
                isPublished = task.isPublished,
                isDraft = task.isDraft
            )
        }
    }
}

// ==================== TASK SUBMISSION ENTITY ====================

@Entity(
    tableName = "task_submissions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["studentId"]),
        Index(value = ["status"]),
        Index(value = ["submittedAt"]),
        Index(value = ["gradedAt"])
    ]
)
data class TaskSubmissionEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val studentId: String,
    val studentName: String,
    val studentRa: String,

    // Submission
    val submittedAt: Long?,
    val content: String,
    val attachmentsJson: String,                  // List<SubmissionAttachment> as JSON
    val attemptNumber: Int,

    // Grading
    val grade: Double?,
    val maxGrade: Double,
    val feedback: String,
    val gradedAt: Long?,
    val gradedBy: String,
    val gradedByName: String,

    // Status
    val status: String,                           // SubmissionStatus enum name
    val isLate: Boolean,
    val latePenaltyApplied: Double,

    // Metadata
    val commentsJson: String,                     // List<SubmissionComment> as JSON
    val revisionsJson: String,                    // List<SubmissionRevision> as JSON
    val lastModifiedAt: Long
) {
    fun toDomain(): TaskSubmission {
        val gson = Gson()

        return TaskSubmission(
            id = id,
            taskId = taskId,
            studentId = studentId,
            studentName = studentName,
            studentRa = studentRa,
            submittedAt = submittedAt,
            content = content,
            attachments = gson.fromJson(
                attachmentsJson,
                object : TypeToken<List<SubmissionAttachment>>() {}.type
            ) ?: emptyList(),
            attemptNumber = attemptNumber,
            grade = grade,
            maxGrade = maxGrade,
            feedback = feedback,
            gradedAt = gradedAt,
            gradedBy = gradedBy,
            gradedByName = gradedByName,
            status = SubmissionStatus.valueOf(status),
            isLate = isLate,
            latePenaltyApplied = latePenaltyApplied,
            comments = gson.fromJson(
                commentsJson,
                object : TypeToken<List<SubmissionComment>>() {}.type
            ) ?: emptyList(),
            revisions = gson.fromJson(
                revisionsJson,
                object : TypeToken<List<SubmissionRevision>>() {}.type
            ) ?: emptyList(),
            lastModifiedAt = lastModifiedAt
        )
    }

    companion object {
        fun fromDomain(submission: TaskSubmission): TaskSubmissionEntity {
            val gson = Gson()

            return TaskSubmissionEntity(
                id = submission.id,
                taskId = submission.taskId,
                studentId = submission.studentId,
                studentName = submission.studentName,
                studentRa = submission.studentRa,
                submittedAt = submission.submittedAt,
                content = submission.content,
                attachmentsJson = gson.toJson(submission.attachments),
                attemptNumber = submission.attemptNumber,
                grade = submission.grade,
                maxGrade = submission.maxGrade,
                feedback = submission.feedback,
                gradedAt = submission.gradedAt,
                gradedBy = submission.gradedBy,
                gradedByName = submission.gradedByName,
                status = submission.status.name,
                isLate = submission.isLate,
                latePenaltyApplied = submission.latePenaltyApplied,
                commentsJson = gson.toJson(submission.comments),
                revisionsJson = gson.toJson(submission.revisions),
                lastModifiedAt = submission.lastModifiedAt
            )
        }
    }
}

// ==================== TASK STATS ENTITY ====================

@Entity(
    tableName = "task_stats",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"], unique = true)]
)
data class TaskStatsEntity(
    @PrimaryKey
    val taskId: String,

    // Submissions
    val totalStudents: Int,
    val totalSubmissions: Int,
    val lateSubmissions: Int,
    val pendingSubmissions: Int,
    val gradedSubmissions: Int,

    // Grades
    val averageGrade: Double,
    val medianGrade: Double,
    val highestGrade: Double,
    val lowestGrade: Double,
    val standardDeviation: Double,

    // Rates
    val submissionRate: Double,
    val completionRate: Double,
    val passingRate: Double,

    // Time
    val averageSubmissionTime: Long,
    val lastUpdated: Long
) {
    fun toDomain(): TaskStats {
        return TaskStats(
            taskId = taskId,
            totalStudents = totalStudents,
            totalSubmissions = totalSubmissions,
            lateSubmissions = lateSubmissions,
            pendingSubmissions = pendingSubmissions,
            gradedSubmissions = gradedSubmissions,
            averageGrade = averageGrade,
            medianGrade = medianGrade,
            highestGrade = highestGrade,
            lowestGrade = lowestGrade,
            standardDeviation = standardDeviation,
            submissionRate = submissionRate,
            completionRate = completionRate,
            passingRate = passingRate,
            averageSubmissionTime = averageSubmissionTime,
            lastUpdated = lastUpdated
        )
    }

    companion object {
        fun fromDomain(stats: TaskStats): TaskStatsEntity {
            return TaskStatsEntity(
                taskId = stats.taskId,
                totalStudents = stats.totalStudents,
                totalSubmissions = stats.totalSubmissions,
                lateSubmissions = stats.lateSubmissions,
                pendingSubmissions = stats.pendingSubmissions,
                gradedSubmissions = stats.gradedSubmissions,
                averageGrade = stats.averageGrade,
                medianGrade = stats.medianGrade,
                highestGrade = stats.highestGrade,
                lowestGrade = stats.lowestGrade,
                standardDeviation = stats.standardDeviation,
                submissionRate = stats.submissionRate,
                completionRate = stats.completionRate,
                passingRate = stats.passingRate,
                averageSubmissionTime = stats.averageSubmissionTime,
                lastUpdated = stats.lastUpdated
            )
        }
    }
}
