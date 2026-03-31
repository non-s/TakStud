package com.example.takstud.util

import android.content.Context
import com.example.takstud.model.*
import com.example.takstud.service.PushNotification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * NotificationManager - Gerencia envio de notificações para pais.
 *
 * FUNCIONALIDADES:
 * - Notificar quando notas são lançadas
 * - Alerta automático para frequência baixa
 * - Avisos e comunicados
 * - Eventos da escola
 * - Controle de preferências de notificação
 *
 * EVENTOS QUE GERAM NOTIFICAÇÕES:
 * 1. Grade Released - Quando professor lança nota
 * 2. Low Attendance - Quando frequência está baixa
 * 3. Announcement - Avisos da coordenação
 * 4. School Event - Eventos importantes
 *
 * EXEMPLO DE USO:
 * val notificationManager = NotificationManager(context)
 * notificationManager.notifyGradeReleased(
 *     studentId = "student1",
 *     taskTitle = "Prova de Matemática",
 *     grade = "8.5"
 * )
 */
class NotificationManager(private val context: Context) {

    private val db = Firebase.firestore

    /**
     * Notifica pais que uma nota foi lançada.
     */
    suspend fun notifyGradeReleased(
        studentId: String,
        taskTitle: String,
        grade: String,
        taskId: String = ""
    ): Boolean {
        return try {
            val parents = getParentsOfStudent(studentId)
            if (parents.isEmpty()) return false

            val notification = PushNotification(
                title = "Nota Lançada",
                body = "$taskTitle - $grade",
                type = "grade_released",
                targetId = taskId,
                recipientRole = "parent"
            )

            // Enviar para cada responsável
            parents.forEach { parentId ->
                saveNotificationToFirestore(parentId, notification)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Notifica pais sobre frequência baixa do estudante.
     */
    suspend fun notifyLowAttendance(
        studentId: String,
        studentName: String,
        attendancePercentage: Double,
        criticalLevel: Boolean = false
    ): Boolean {
        return try {
            val parents = getParentsOfStudent(studentId)
            if (parents.isEmpty()) return false

            val title = if (criticalLevel) "⚠️ Frequência Crítica" else "Frequência Baixa"
            val body = "$studentName: ${String.format("%.1f", attendancePercentage)}% de presença"

            val notification = PushNotification(
                title = title,
                body = body,
                type = "absence_alert",
                targetId = studentId,
                recipientRole = "parent"
            )

            parents.forEach { parentId ->
                saveNotificationToFirestore(parentId, notification)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Notifica sobre comunicado/aviso da escola.
     */
    suspend fun notifyAnnouncement(
        title: String,
        body: String,
        targetRole: String = "parent",  // parent, teacher, student
        sendToAll: Boolean = true
    ): Boolean {
        return try {
            val notification = PushNotification(
                title = title,
                body = body,
                type = "announcement",
                recipientRole = targetRole,
                sendToTopic = true,
                topicName = "announcements"
            )

            if (sendToAll) {
                // Salvar comunicado para todos
                db.collection("announcements").document().set(
                    mapOf(
                        "title" to title,
                        "body" to body,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "targetRole" to targetRole
                    )
                ).await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Notifica sobre evento da escola.
     */
    suspend fun notifySchoolEvent(
        eventTitle: String,
        eventDescription: String,
        eventDate: String,
        classIds: List<String> = emptyList()
    ): Boolean {
        return try {
            val notification = PushNotification(
                title = "📅 Evento: $eventTitle",
                body = "$eventDescription - $eventDate",
                type = "event"
            )

            // Se específico para classes, notificar pais dessas classes
            if (classIds.isNotEmpty()) {
                classIds.forEach { classId ->
                    notifyParentsByClass(classId, notification)
                }
            } else {
                // Notificar todos
                db.collection("events").document().set(
                    mapOf(
                        "title" to eventTitle,
                        "description" to eventDescription,
                        "date" to eventDate,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Notifica pais de uma turma.
     */
    private suspend fun notifyParentsByClass(
        classId: String,
        notification: PushNotification
    ) {
        try {
            // Buscar todos os estudantes da classe
            val students = db.collection("students")
                .whereEqualTo("classId", classId)
                .get()
                .await()
                .documents

            val parentIds = mutableSetOf<String>()
            for (studentDoc in students) {
                val student = studentDoc.toObject(Student::class.java)
                student?.parent?.let { parentIds.add(it) }
            }

            // Notificar cada responsável
            parentIds.forEach { parentId ->
                saveNotificationToFirestore(parentId, notification)
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Obtém IDs dos responsáveis de um estudante.
     */
    private suspend fun getParentsOfStudent(studentId: String): List<String> {
        return try {
            val parentIds = mutableListOf<String>()

            // Buscar a partir da coleção student
            val studentDoc = db.collection("students")
                .document(studentId)
                .get()
                .await()

            val student = studentDoc.toObject(Student::class.java)
            student?.parent?.let { parentIds.add(it) }

            // Também verificar na coleção parent_student_relationships
            val relationships = db.collection("parent_student_relationships")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
                .documents

            for (rel in relationships) {
                val parentId = rel.getString("parentId")
                if (parentId != null && parentId !in parentIds) {
                    parentIds.add(parentId)
                }
            }

            parentIds
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Salva notificação no Firestore para histórico e app.
     */
    private suspend fun saveNotificationToFirestore(
        userId: String,
        notification: PushNotification
    ) {
        try {
            db.collection("users")
                .document(userId)
                .collection("notifications")
                .document()
                .set(
                    mapOf(
                        "title" to notification.title,
                        "body" to notification.body,
                        "type" to notification.type,
                        "targetId" to notification.targetId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "read" to false
                    )
                )
                .await()
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Marca notificação como lida.
     */
    suspend fun markNotificationAsRead(
        userId: String,
        notificationId: String
    ): Boolean {
        return try {
            db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtém notificações não lidas de um usuário.
     */
    suspend fun getUnreadNotifications(userId: String): List<PushNotification> {
        return try {
            val docs = db.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .orderBy("createdAt")
                .get()
                .await()
                .documents

            docs.mapNotNull { doc ->
                PushNotification(
                    title = doc.getString("title") ?: "",
                    body = doc.getString("body") ?: "",
                    type = doc.getString("type") ?: "general",
                    targetId = doc.getString("targetId") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Preferences de notificação do usuário.
     */
    data class NotificationPreferences(
        val enableGradeNotifications: Boolean = true,
        val enableAttendanceAlerts: Boolean = true,
        val enableAnnouncements: Boolean = true,
        val enableEventNotifications: Boolean = true,
        val enableSound: Boolean = true,
        val enableVibration: Boolean = true,
        val quietHourStart: String = "22:00",  // HH:MM
        val quietHourEnd: String = "08:00"      // HH:MM
    )

    /**
     * Obtém preferências de notificação.
     */
    suspend fun getNotificationPreferences(userId: String): NotificationPreferences {
        return try {
            val doc = db.collection("users")
                .document(userId)
                .get()
                .await()

            val prefs = doc.get("notificationPreferences") as? Map<String, Any> ?: return NotificationPreferences()

            NotificationPreferences(
                enableGradeNotifications = (prefs["enableGradeNotifications"] as? Boolean) ?: true,
                enableAttendanceAlerts = (prefs["enableAttendanceAlerts"] as? Boolean) ?: true,
                enableAnnouncements = (prefs["enableAnnouncements"] as? Boolean) ?: true,
                enableEventNotifications = (prefs["enableEventNotifications"] as? Boolean) ?: true,
                enableSound = (prefs["enableSound"] as? Boolean) ?: true,
                enableVibration = (prefs["enableVibration"] as? Boolean) ?: true,
                quietHourStart = (prefs["quietHourStart"] as? String) ?: "22:00",
                quietHourEnd = (prefs["quietHourEnd"] as? String) ?: "08:00"
            )
        } catch (e: Exception) {
            NotificationPreferences()
        }
    }

    /**
     * Atualiza preferências de notificação.
     */
    suspend fun updateNotificationPreferences(
        userId: String,
        preferences: NotificationPreferences
    ): Boolean {
        return try {
            db.collection("users")
                .document(userId)
                .update("notificationPreferences", mapOf(
                    "enableGradeNotifications" to preferences.enableGradeNotifications,
                    "enableAttendanceAlerts" to preferences.enableAttendanceAlerts,
                    "enableAnnouncements" to preferences.enableAnnouncements,
                    "enableEventNotifications" to preferences.enableEventNotifications,
                    "enableSound" to preferences.enableSound,
                    "enableVibration" to preferences.enableVibration,
                    "quietHourStart" to preferences.quietHourStart,
                    "quietHourEnd" to preferences.quietHourEnd
                ))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
}
