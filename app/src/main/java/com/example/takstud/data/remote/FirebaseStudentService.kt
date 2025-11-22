package com.example.takstud.data.remote

import com.example.takstud.model.student.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔥 Firebase Service para Alunos
 * - Sincronização em tempo real
 * - Backup na nuvem
 * - Compartilhamento entre dispositivos
 */
@Singleton
class FirebaseStudentService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val STUDENTS_COLLECTION = "students"
        private const val TIMELINE_COLLECTION = "student_timeline_events"
        private const val STATS_COLLECTION = "student_stats"
        private const val DOCUMENTS_SUBCOLLECTION = "documents"
        private const val OBSERVATIONS_SUBCOLLECTION = "observations"
    }

    private val gson = Gson()

    // ==================== STUDENTS ====================

    /**
     * Obter todos os alunos ativos (real-time)
     */
    fun getActiveStudents(schoolId: String): Flow<List<StudentExtended>> = callbackFlow {
        val listener = firestore.collection(STUDENTS_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("isActive", true)
            .orderBy("fullName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val students = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudentFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(students)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obter todos os alunos (ativos + inativos)
     */
    fun getAllStudents(schoolId: String): Flow<List<StudentExtended>> = callbackFlow {
        val listener = firestore.collection(STUDENTS_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .orderBy("fullName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val students = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudentFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(students)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obter aluno por ID (real-time)
     */
    fun getStudentById(studentId: String): Flow<StudentExtended?> = callbackFlow {
        val listener = firestore.collection(STUDENTS_COLLECTION)
            .document(studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val student = snapshot?.toObject(StudentFirestore::class.java)?.toDomain()
                trySend(student)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obter alunos por turma
     */
    fun getStudentsByClass(schoolId: String, className: String): Flow<List<StudentExtended>> = callbackFlow {
        val listener = firestore.collection(STUDENTS_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("className", className)
            .whereEqualTo("isActive", true)
            .orderBy("fullName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val students = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudentFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(students)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Buscar alunos
     */
    suspend fun searchStudents(schoolId: String, query: String): List<StudentExtended> {
        // Firestore não suporta busca por texto livre nativamente
        // Alternativa 1: Buscar todos e filtrar localmente
        // Alternativa 2: Usar Algolia ou ElasticSearch
        // Por simplicidade, usando Alternativa 1 aqui

        val allStudents = firestore.collection(STUDENTS_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(StudentFirestore::class.java)?.toDomain() }

        // Filtrar localmente
        return allStudents.filter { student ->
            student.personalInfo.fullName.contains(query, ignoreCase = true) ||
            student.personalInfo.cpf.contains(query) ||
            student.academicInfo.registrationNumber.contains(query) ||
            student.contactInfo.email.contains(query, ignoreCase = true) ||
            student.contactInfo.phone.contains(query)
        }
    }

    /**
     * Salvar aluno
     */
    suspend fun saveStudent(schoolId: String, student: StudentExtended) {
        val firestoreStudent = StudentFirestore.fromDomain(student, schoolId)

        firestore.collection(STUDENTS_COLLECTION)
            .document(student.id)
            .set(firestoreStudent)
            .await()
    }

    /**
     * Salvar múltiplos alunos (batch)
     */
    suspend fun saveStudents(schoolId: String, students: List<StudentExtended>) {
        val batch = firestore.batch()

        students.forEach { student ->
            val ref = firestore.collection(STUDENTS_COLLECTION).document(student.id)
            val firestoreStudent = StudentFirestore.fromDomain(student, schoolId)
            batch.set(ref, firestoreStudent)
        }

        batch.commit().await()
    }

    /**
     * Deletar aluno
     */
    suspend fun deleteStudent(studentId: String) {
        val batch = firestore.batch()

        // Deletar aluno
        val studentRef = firestore.collection(STUDENTS_COLLECTION).document(studentId)
        batch.delete(studentRef)

        // Deletar timeline
        val timelineEvents = firestore.collection(TIMELINE_COLLECTION)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()

        timelineEvents.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        // Deletar stats
        val statsRef = firestore.collection(STATS_COLLECTION).document(studentId)
        batch.delete(statsRef)

        batch.commit().await()
    }

    /**
     * Desativar aluno (soft delete)
     */
    suspend fun deactivateStudent(studentId: String) {
        firestore.collection(STUDENTS_COLLECTION)
            .document(studentId)
            .update(
                mapOf(
                    "isActive" to false,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    // ==================== TIMELINE ====================

    /**
     * Obter timeline de um aluno
     */
    fun getStudentTimeline(studentId: String): Flow<List<StudentTimelineEvent>> = callbackFlow {
        val listener = firestore.collection(TIMELINE_COLLECTION)
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TimelineEventFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Adicionar evento na timeline
     */
    suspend fun addTimelineEvent(studentId: String, event: StudentTimelineEvent) {
        val firestoreEvent = TimelineEventFirestore.fromDomain(event, studentId)

        firestore.collection(TIMELINE_COLLECTION)
            .document(event.id)
            .set(firestoreEvent)
            .await()
    }

    /**
     * Deletar evento da timeline
     */
    suspend fun deleteTimelineEvent(eventId: String) {
        firestore.collection(TIMELINE_COLLECTION)
            .document(eventId)
            .delete()
            .await()
    }

    // ==================== STATISTICS ====================

    /**
     * Obter estatísticas de um aluno
     */
    fun getStudentStats(studentId: String): Flow<StudentStats?> = callbackFlow {
        val listener = firestore.collection(STATS_COLLECTION)
            .document(studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val stats = snapshot?.toObject(StudentStatsFirestore::class.java)?.toDomain()
                trySend(stats)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Atualizar estatísticas
     */
    suspend fun updateStudentStats(studentId: String, stats: StudentStats) {
        val firestoreStats = StudentStatsFirestore.fromDomain(stats, studentId)

        firestore.collection(STATS_COLLECTION)
            .document(studentId)
            .set(firestoreStats)
            .await()
    }

    /**
     * Obter top performers
     */
    suspend fun getTopPerformers(schoolId: String, limit: Int = 10): List<StudentStats> {
        val statsSnapshot = firestore.collection(STATS_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .orderBy("averageGrade", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return statsSnapshot.documents.mapNotNull {
            it.toObject(StudentStatsFirestore::class.java)?.toDomain()
        }
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Atualizar turma de múltiplos alunos
     */
    suspend fun updateClassForMultiple(studentIds: List<String>, newClassName: String) {
        val batch = firestore.batch()

        studentIds.forEach { studentId ->
            val ref = firestore.collection(STUDENTS_COLLECTION).document(studentId)
            batch.update(ref, mapOf(
                "className" to newClassName,
                "updatedAt" to System.currentTimeMillis()
            ))
        }

        batch.commit().await()
    }

    /**
     * Atualizar status de múltiplos alunos
     */
    suspend fun updateStatusForMultiple(studentIds: List<String>, newStatus: String) {
        val batch = firestore.batch()

        studentIds.forEach { studentId ->
            val ref = firestore.collection(STUDENTS_COLLECTION).document(studentId)
            batch.update(ref, mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            ))
        }

        batch.commit().await()
    }

    // ==================== EXPORT / BACKUP ====================

    /**
     * Exportar todos os dados de um aluno
     */
    suspend fun exportStudentData(studentId: String): StudentFullExport {
        val student = firestore.collection(STUDENTS_COLLECTION)
            .document(studentId)
            .get()
            .await()
            .toObject(StudentFirestore::class.java)?.toDomain()

        val timeline = firestore.collection(TIMELINE_COLLECTION)
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(TimelineEventFirestore::class.java)?.toDomain() }

        val stats = firestore.collection(STATS_COLLECTION)
            .document(studentId)
            .get()
            .await()
            .toObject(StudentStatsFirestore::class.java)?.toDomain()

        return StudentFullExport(
            student = student,
            timeline = timeline,
            stats = stats
        )
    }
}

// ==================== FIRESTORE MODELS ====================

/**
 * Modelo Firestore para Student
 * (Versão simplificada - dados complexos como JSON)
 */
data class StudentFirestore(
    val id: String = "",
    val schoolId: String = "",

    // Personal Info (flattened)
    val fullName: String = "",
    val preferredName: String = "",
    val birthDate: Long? = null,
    val birthPlace: String = "",
    val cpf: String = "",
    val rg: String = "",
    val rgIssuer: String = "",
    val rgIssueDate: Long? = null,
    val birthCertificate: String = "",
    val gender: String = "",
    val nationality: String = "",
    val photoUrl: String = "",
    val bloodType: String? = null,

    // Contact Info (flattened)
    val phone: String = "",
    val phoneSecondary: String = "",
    val email: String = "",
    val addressJson: String? = null,

    // Academic Info (flattened)
    val registrationNumber: String = "",
    val enrollmentDate: Long? = null,
    val className: String = "",
    val grade: String = "",
    val period: String = "",
    val status: String = "",
    val previousSchool: String = "",
    val previousGrade: String = "",
    val transferDate: Long? = null,
    val graduationDate: Long? = null,
    val isScholarship: Boolean = false,
    val scholarshipPercentage: Int = 0,
    val gpa: Double = 0.0,
    val attendanceRate: Double = 0.0,

    // Complex nested data (as JSON)
    val guardiansJson: String = "",
    val healthInfoJson: String? = null,
    val documentsJson: String = "",
    val observationsJson: String = "",
    val tagsJson: String = "",

    // Metadata
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val createdBy: String = "",
    val isActive: Boolean = true
) {
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
        fun fromDomain(student: StudentExtended, schoolId: String): StudentFirestore {
            val gson = Gson()

            return StudentFirestore(
                id = student.id,
                schoolId = schoolId,
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
                phone = student.contactInfo.phone,
                phoneSecondary = student.contactInfo.phoneSecondary,
                email = student.contactInfo.email,
                addressJson = student.contactInfo.address?.let { gson.toJson(it) },
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
                guardiansJson = gson.toJson(student.guardians),
                healthInfoJson = student.healthInfo?.let { gson.toJson(it) },
                documentsJson = gson.toJson(student.documents),
                observationsJson = gson.toJson(student.observations),
                tagsJson = gson.toJson(student.tags),
                createdAt = student.createdAt,
                updatedAt = student.updatedAt,
                createdBy = student.createdBy,
                isActive = student.isActive
            )
        }
    }
}

/**
 * Modelo Firestore para Timeline Event
 */
data class TimelineEventFirestore(
    val id: String = "",
    val studentId: String = "",
    val type: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val createdBy: String = "",
    val metadataJson: String = ""
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
        fun fromDomain(event: StudentTimelineEvent, studentId: String): TimelineEventFirestore {
            val gson = Gson()
            return TimelineEventFirestore(
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
 * Modelo Firestore para Student Stats
 */
data class StudentStatsFirestore(
    val studentId: String = "",
    val schoolId: String = "",
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
    val failedDisciplines: Int = 0,
    val lastUpdated: Long = 0L
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
        fun fromDomain(stats: StudentStats, studentId: String, schoolId: String = ""): StudentStatsFirestore {
            return StudentStatsFirestore(
                studentId = studentId,
                schoolId = schoolId,
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

/**
 * Exportação completa dos dados de um aluno
 */
data class StudentFullExport(
    val student: StudentExtended?,
    val timeline: List<StudentTimelineEvent>,
    val stats: StudentStats?
)
