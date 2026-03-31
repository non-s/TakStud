package com.example.takstud.data.local.entity

import androidx.room.*
import com.example.takstud.model.student.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 🗄️ Room Entities para Alunos
 * Entidades otimizadas para banco de dados local
 */

// ==================== MAIN ENTITY ====================

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["cpf"], unique = true),
        Index(value = ["registrationNumber"], unique = true),
        Index(value = ["fullName"]),
        Index(value = ["className"]),
        Index(value = ["status"]),
        Index(value = ["isActive"])
    ]
)
data class StudentEntity(
    @PrimaryKey
    val id: String,

    // ===== PERSONAL INFO (flattened for queries) =====
    val fullName: String,
    val preferredName: String,
    val birthDate: Long?,
    val birthPlace: String,
    val cpf: String,
    val rg: String,
    val rgIssuer: String,
    val rgIssueDate: Long?,
    val birthCertificate: String,
    val gender: String, // Gender enum name
    val nationality: String,
    val photoUrl: String,
    val bloodType: String?, // BloodType enum name

    // ===== CONTACT INFO (flattened) =====
    val phone: String,
    val phoneSecondary: String,
    val email: String,

    // Address as JSON (complex nested object)
    val addressJson: String?,

    // ===== ACADEMIC INFO (flattened for queries) =====
    val registrationNumber: String,
    val enrollmentDate: Long?,
    val className: String,
    val grade: String,
    val period: String,
    val status: String, // StudentStatus enum name
    val previousSchool: String,
    val previousGrade: String,
    val transferDate: Long?,
    val graduationDate: Long?,
    val isScholarship: Boolean,
    val scholarshipPercentage: Int,
    val gpa: Double,
    val attendanceRate: Double,

    // ===== COMPLEX NESTED DATA (as JSON) =====
    val guardiansJson: String, // List<Guardian> as JSON
    val healthInfoJson: String?, // HealthInfo as JSON (optional)
    val documentsJson: String, // List<StudentDocument> as JSON
    val observationsJson: String, // List<Observation> as JSON

    // ===== SIMPLE LIST =====
    val tagsJson: String, // List<String> as JSON

    // ===== METADATA =====
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val isActive: Boolean
) {
    /**
     * Converte entidade Room para modelo de domínio
     */
    fun toDomain(): StudentExtended {
        val gson = Gson()

        return StudentExtended(
            id = id,
            personalInfo = PersonalInfo(
                fullName = fullName,
                preferredName = preferredName,
                birthDate = birthDate,
                birthPlace = birthPlace,
                cpf = cpf,
                rg = rg,
                rgIssuer = rgIssuer,
                rgIssueDate = rgIssueDate,
                birthCertificate = birthCertificate,
                gender = if (gender.isNotBlank()) Gender.valueOf(gender) else Gender.NOT_SPECIFIED,
                nationality = nationality,
                photoUrl = photoUrl,
                bloodType = bloodType?.let { BloodType.valueOf(it) }
            ),
            contactInfo = ContactInfo(
                phone = phone,
                phoneSecondary = phoneSecondary,
                email = email,
                address = addressJson?.let { gson.fromJson(it, Address::class.java) }
            ),
            guardians = gson.fromJson(
                guardiansJson,
                object : TypeToken<List<Guardian>>() {}.type
            ) ?: emptyList(),
            academicInfo = AcademicInfo(
                registrationNumber = registrationNumber,
                enrollmentDate = enrollmentDate,
                className = className,
                grade = grade,
                period = period,
                status = if (status.isNotBlank()) StudentStatus.valueOf(status) else StudentStatus.ACTIVE,
                previousSchool = previousSchool,
                previousGrade = previousGrade,
                transferDate = transferDate,
                graduationDate = graduationDate,
                isScholarship = isScholarship,
                scholarshipPercentage = scholarshipPercentage,
                gpa = gpa,
                attendanceRate = attendanceRate
            ),
            healthInfo = healthInfoJson?.let {
                gson.fromJson(it, HealthInfo::class.java)
            },
            documents = gson.fromJson(
                documentsJson,
                object : TypeToken<List<StudentDocument>>() {}.type
            ) ?: emptyList(),
            observations = gson.fromJson(
                observationsJson,
                object : TypeToken<List<Observation>>() {}.type
            ) ?: emptyList(),
            tags = gson.fromJson(
                tagsJson,
                object : TypeToken<List<String>>() {}.type
            ) ?: emptyList(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            isActive = isActive
        )
    }

    companion object {
        /**
         * Converte modelo de domínio para entidade Room
         */
        fun fromDomain(student: StudentExtended): StudentEntity {
            val gson = Gson()

            return StudentEntity(
                id = student.id,

                // Personal Info
                fullName = student.personalInfo.fullName,
                preferredName = student.personalInfo.preferredName,
                birthDate = student.personalInfo.birthDate,
                birthPlace = student.personalInfo.birthPlace,
                cpf = student.personalInfo.cpf,
                rg = student.personalInfo.rg,
                rgIssuer = student.personalInfo.rgIssuer,
                rgIssueDate = student.personalInfo.rgIssueDate,
                birthCertificate = student.personalInfo.birthCertificate,
                gender = student.personalInfo.gender.name,
                nationality = student.personalInfo.nationality,
                photoUrl = student.personalInfo.photoUrl,
                bloodType = student.personalInfo.bloodType?.name,

                // Contact Info
                phone = student.contactInfo.phone,
                phoneSecondary = student.contactInfo.phoneSecondary,
                email = student.contactInfo.email,
                addressJson = student.contactInfo.address?.let { gson.toJson(it) },

                // Academic Info
                registrationNumber = student.academicInfo.registrationNumber,
                enrollmentDate = student.academicInfo.enrollmentDate,
                className = student.academicInfo.className,
                grade = student.academicInfo.grade,
                period = student.academicInfo.period,
                status = student.academicInfo.status.name,
                previousSchool = student.academicInfo.previousSchool,
                previousGrade = student.academicInfo.previousGrade,
                transferDate = student.academicInfo.transferDate,
                graduationDate = student.academicInfo.graduationDate,
                isScholarship = student.academicInfo.isScholarship,
                scholarshipPercentage = student.academicInfo.scholarshipPercentage,
                gpa = student.academicInfo.gpa,
                attendanceRate = student.academicInfo.attendanceRate,

                // Complex nested data
                guardiansJson = gson.toJson(student.guardians),
                healthInfoJson = student.healthInfo?.let { gson.toJson(it) },
                documentsJson = gson.toJson(student.documents),
                observationsJson = gson.toJson(student.observations),
                tagsJson = gson.toJson(student.tags),

                // Metadata
                createdAt = student.createdAt,
                updatedAt = student.updatedAt,
                createdBy = student.createdBy,
                isActive = student.isActive
            )
        }
    }
}

// ==================== SECONDARY ENTITIES ====================

/**
 * Entidade separada para Timeline de eventos do aluno
 * (Mantida separada para não sobrecarregar a entidade principal)
 */
@Entity(
    tableName = "student_timeline_events",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["timestamp"]),
        Index(value = ["type"])
    ]
)
data class StudentTimelineEventEntity(
    @PrimaryKey
    val id: String,
    val studentId: String,
    val type: String, // TimelineEventType enum name
    val title: String,
    val description: String,
    val timestamp: Long,
    val createdBy: String,
    val metadataJson: String // Map<String, String> as JSON
) {
    fun toDomain(): StudentTimelineEvent {
        val gson = Gson()
        return StudentTimelineEvent(
            id = id,
            type = if (type.isNotBlank()) TimelineEventType.valueOf(type) else TimelineEventType.OTHER,
            title = title,
            description = description,
            timestamp = timestamp,
            createdBy = createdBy,
            metadata = gson.fromJson(
                metadataJson,
                object : TypeToken<Map<String, String>>() {}.type
            ) ?: emptyMap()
        )
    }

    companion object {
        fun fromDomain(event: StudentTimelineEvent, studentId: String): StudentTimelineEventEntity {
            val gson = Gson()
            return StudentTimelineEventEntity(
                id = event.id,
                studentId = studentId,
                type = event.type.name,
                title = event.title,
                description = event.description,
                timestamp = event.timestamp,
                createdBy = event.createdBy,
                metadataJson = gson.toJson(event.metadata)
            )
        }
    }
}

/**
 * Entidade para estatísticas do aluno
 * Atualizada periodicamente para análises rápidas
 */
@Entity(
    tableName = "student_stats",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"], unique = true)]
)
data class StudentStatsEntity(
    @PrimaryKey
    val studentId: String,

    // Classes
    val totalClasses: Int,
    val attendedClasses: Int,
    val absentClasses: Int,
    val attendanceRate: Double,

    // Tasks
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val taskCompletionRate: Double,

    // Grades
    val averageGrade: Double,
    val highestGrade: Double,
    val lowestGrade: Double,

    // Disciplines
    val totalDisciplines: Int,
    val approvedDisciplines: Int,
    val failedDisciplines: Int,

    // Metadata
    val lastUpdated: Long
) {
    fun toDomain(): StudentStats {
        return StudentStats(
            totalClasses = totalClasses,
            attendedClasses = attendedClasses,
            absentClasses = absentClasses,
            attendanceRate = attendanceRate,
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            pendingTasks = pendingTasks,
            taskCompletionRate = taskCompletionRate,
            averageGrade = averageGrade,
            highestGrade = highestGrade,
            lowestGrade = lowestGrade,
            totalDisciplines = totalDisciplines,
            approvedDisciplines = approvedDisciplines,
            failedDisciplines = failedDisciplines
        )
    }

    companion object {
        fun fromDomain(stats: StudentStats, studentId: String): StudentStatsEntity {
            return StudentStatsEntity(
                studentId = studentId,
                totalClasses = stats.totalClasses,
                attendedClasses = stats.attendedClasses,
                absentClasses = stats.absentClasses,
                attendanceRate = stats.attendanceRate,
                totalTasks = stats.totalTasks,
                completedTasks = stats.completedTasks,
                pendingTasks = stats.pendingTasks,
                taskCompletionRate = stats.taskCompletionRate,
                averageGrade = stats.averageGrade,
                highestGrade = stats.highestGrade,
                lowestGrade = stats.lowestGrade,
                totalDisciplines = stats.totalDisciplines,
                approvedDisciplines = stats.approvedDisciplines,
                failedDisciplines = stats.failedDisciplines,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}

// ==================== TYPE CONVERTERS ====================

/**
 * Type converters para Room
 * (Não necessário aqui pois estamos usando JSON strings,
 *  mas mantido para possíveis extensões futuras)
 */
class StudentTypeConverters {
    private val gson = Gson()

    // Lists
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    // Map
    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String> {
        if (value == null) return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun toStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
}
