package com.example.takstud.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.takstud.R
import com.example.takstud.data.repository.NotificationRepository
import com.example.takstud.model.Notification
import com.example.takstud.model.NotificationPriority
import com.example.takstud.model.NotificationType
import com.example.takstud.util.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TakStudMessagingService - Serviço de mensagens Firebase Cloud Messaging
 *
 * Funcionalidades:
 * - Recebe notificações push do Firebase
 * - Salva notificações no banco de dados local
 * - Exibe notificações no sistema
 * - Gerencia token FCM do dispositivo
 */
@AndroidEntryPoint
class TakStudMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val CHANNEL_ID_DEFAULT = "takstud_notifications"
        private const val CHANNEL_ID_URGENT = "takstud_urgent"
        private const val CHANNEL_ID_GRADES = "takstud_grades"
        private const val CHANNEL_ID_EVENTS = "takstud_events"
        private const val CHANNEL_ID_TASKS = "takstud_tasks"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Chamado quando uma nova mensagem é recebida
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Extrai dados da mensagem
        val data = remoteMessage.data
        val notificationData = remoteMessage.notification

        // Cria objeto de notificação
        val notification = Notification(
            id = data["id"] ?: System.currentTimeMillis().toString(),
            title = notificationData?.title ?: data["title"] ?: "",
            message = notificationData?.body ?: data["message"] ?: "",
            type = parseNotificationType(data["type"]),
            priority = parseNotificationPriority(data["priority"]),
            targetUserId = data["targetUserId"] ?: "",
            targetRole = data["targetRole"] ?: "",
            targetClass = data["targetClass"] ?: "",
            senderId = data["senderId"] ?: "",
            senderName = data["senderName"] ?: "",
            relatedEntityId = data["relatedEntityId"] ?: "",
            relatedEntityType = data["relatedEntityType"] ?: "",
            actionUrl = data["actionUrl"] ?: "",
            imageUrl = notificationData?.imageUrl?.toString() ?: data["imageUrl"] ?: "",
            createdAt = data["createdAt"]?.toLongOrNull() ?: System.currentTimeMillis(),
            expiresAt = data["expiresAt"]?.toLongOrNull() ?: 0L
        )

        // Salva no banco de dados
        serviceScope.launch {
            notificationRepository.saveNotification(notification)
        }

        // Exibe notificação do sistema
        showSystemNotification(notification)
    }

    /**
     * Chamado quando o token FCM é atualizado
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Salva o token nas preferências
        getSharedPreferences("takstud_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()

        // Envia token para o Firestore
        sendTokenToServer(token)
    }

    /**
     * Exibe notificação do sistema Android
     */
    private fun showSystemNotification(notification: Notification) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Seleciona o canal apropriado
        val channelId = when {
            notification.priority == NotificationPriority.URGENT -> CHANNEL_ID_URGENT
            notification.type == NotificationType.GRADE -> CHANNEL_ID_GRADES
            notification.type == NotificationType.EVENT -> CHANNEL_ID_EVENTS
            notification.type == NotificationType.TASK -> CHANNEL_ID_TASKS
            else -> CHANNEL_ID_DEFAULT
        }

        // Cria intent para abrir o app
        val intent = Intent(this, Class.forName("com.example.takstud.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", notification.id)
            putExtra("action_url", notification.actionUrl)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Constrói a notificação
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(getPriorityLevel(notification.priority))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Adiciona estilo expandido se a mensagem for longa
        if (notification.message.length > 40) {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notification.message)
            )
        }

        // Adiciona imagem se disponível
        if (notification.imageUrl.isNotEmpty()) {
            // TODO: Carregar imagem da URL e adicionar
        }

        // Adiciona cor baseada na prioridade
        builder.color = when (notification.priority) {
            NotificationPriority.LOW -> 0xFF4CAF50.toInt()
            NotificationPriority.NORMAL -> 0xFF2196F3.toInt()
            NotificationPriority.HIGH -> 0xFFFF9800.toInt()
            NotificationPriority.URGENT -> 0xFFF44336.toInt()
        }

        // Exibe a notificação
        notificationManager.notify(notification.id.hashCode(), builder.build())
    }

    /**
     * Cria canais de notificação (Android 8+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Canal padrão
            val defaultChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                "Notificações Gerais",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações gerais do TakStud"
            }

            // Canal urgente
            val urgentChannel = NotificationChannel(
                CHANNEL_ID_URGENT,
                "Notificações Urgentes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações urgentes e importantes"
                enableVibration(true)
                enableLights(true)
            }

            // Canal de notas
            val gradesChannel = NotificationChannel(
                CHANNEL_ID_GRADES,
                "Notas e Avaliações",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações sobre notas e avaliações"
            }

            // Canal de eventos
            val eventsChannel = NotificationChannel(
                CHANNEL_ID_EVENTS,
                "Eventos e Calendário",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações sobre eventos e agenda"
            }

            // Canal de tarefas
            val tasksChannel = NotificationChannel(
                CHANNEL_ID_TASKS,
                "Tarefas e Atividades",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações sobre tarefas e atividades"
            }

            notificationManager.createNotificationChannels(
                listOf(
                    defaultChannel,
                    urgentChannel,
                    gradesChannel,
                    eventsChannel,
                    tasksChannel
                )
            )
        }
    }

    /**
     * Converte string em NotificationType
     */
    private fun parseNotificationType(type: String?): NotificationType {
        return try {
            NotificationType.valueOf(type?.uppercase() ?: "GENERAL")
        } catch (e: Exception) {
            NotificationType.GENERAL
        }
    }

    /**
     * Converte string em NotificationPriority
     */
    private fun parseNotificationPriority(priority: String?): NotificationPriority {
        return try {
            NotificationPriority.valueOf(priority?.uppercase() ?: "NORMAL")
        } catch (e: Exception) {
            NotificationPriority.NORMAL
        }
    }

    /**
     * Converte NotificationPriority em nível de prioridade do sistema
     */
    private fun getPriorityLevel(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.URGENT -> NotificationCompat.PRIORITY_MAX
        }
    }

    /**
     * Envia token para o servidor
     * Salva o token FCM no Firestore para que o backend possa enviar notificações push
     */
    private fun sendTokenToServer(token: String) {
        serviceScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()

                // Tenta pegar o userId da sessão atual
                val session = SessionManager.currentSession.value
                val userId = session?.userId

                // Se não houver sessão, salva o token com um ID temporário baseado no dispositivo
                val deviceId = userId ?: getSharedPreferences("takstud_prefs", Context.MODE_PRIVATE)
                    .getString("device_id", null)
                    ?: run {
                        // Gera um ID único para o dispositivo
                        val newDeviceId = "device_${System.currentTimeMillis()}"
                        getSharedPreferences("takstud_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("device_id", newDeviceId)
                            .apply()
                        newDeviceId
                    }

                // Salva o token no Firestore
                val tokenData = hashMapOf(
                    "token" to token,
                    "userId" to (userId ?: ""),
                    "deviceId" to deviceId,
                    "updatedAt" to System.currentTimeMillis(),
                    "platform" to "android"
                )

                firestore.collection("fcm_tokens")
                    .document(deviceId)
                    .set(tokenData)
                    .addOnSuccessListener {
                        Log.d("TakStudMessaging", "Token FCM salvo com sucesso para $deviceId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("TakStudMessaging", "Erro ao salvar token FCM", e)
                    }

            } catch (e: Exception) {
                Log.e("TakStudMessaging", "Erro ao enviar token para servidor", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // O SupervisorJob será cancelado automaticamente
    }
}
