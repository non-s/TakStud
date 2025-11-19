package com.example.takstud.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.takstud.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Date

/**
 * FCMNotificationService - Serviço para receber e processar notificações FCM.
 *
 * FUNCIONALIDADES:
 * - Recebimento de mensagens push via FCM
 * - Processamento de diferentes tipos de notificação
 * - Exibição em foreground e background
 * - Geração de canais de notificação por tipo
 * - Log e analytics de notificações
 *
 * TIPOS DE NOTIFICAÇÃO:
 * - grade_released: Nova nota lançada
 * - absence_alert: Alerta de frequência baixa
 * - announcement: Aviso importante
 * - event: Evento da escola
 * - parent_message: Mensagem para responsável
 *
 * EXEMPLO DE USO:
 * O serviço é registrado no AndroidManifest.xml e recebe mensagens automaticamente
 * via FirebaseCloud Messaging.
 */
class FCMNotificationService : FirebaseMessagingService() {

    /**
     * Chamado quando uma mensagem é recebida enquanto o app está em foreground.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Dados da notificação
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Notificação"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val notificationType = remoteMessage.data["type"] ?: "general"
        val targetId = remoteMessage.data["targetId"] ?: ""

        // Processar baseado no tipo
        when (notificationType) {
            "grade_released" -> handleGradeReleased(title, body, targetId)
            "absence_alert" -> handleAbsenceAlert(title, body, targetId)
            "announcement" -> handleAnnouncement(title, body)
            "event" -> handleEvent(title, body, targetId)
            "parent_message" -> handleParentMessage(title, body, targetId)
            else -> showNotification(title, body, "general")
        }

        // Log da notificação
        logNotificationReceived(notificationType, title)
    }

    /**
     * Chamado quando o token é atualizado ou criado.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Atualizar token no servidor/banco de dados
        sendTokenToServer(token)
    }

    /**
     * Processa notificação de nota lançada.
     */
    private fun handleGradeReleased(title: String, body: String, targetId: String) {
        val message = "$title: $body"
        showNotification(
            title = title,
            message = message,
            channelId = "grades",
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    /**
     * Processa alerta de frequência baixa.
     */
    private fun handleAbsenceAlert(title: String, body: String, targetId: String) {
        val message = "⚠️ $title: $body"
        showNotification(
            title = title,
            message = message,
            channelId = "attendance",
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    /**
     * Processa aviso/comunicado da escola.
     */
    private fun handleAnnouncement(title: String, body: String) {
        val message = "📢 $title: $body"
        showNotification(
            title = title,
            message = message,
            channelId = "announcements",
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    /**
     * Processa evento da escola.
     */
    private fun handleEvent(title: String, body: String, targetId: String) {
        val message = "📅 $title: $body"
        showNotification(
            title = title,
            message = message,
            channelId = "events",
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    /**
     * Processa mensagem de responsável.
     */
    private fun handleParentMessage(title: String, body: String, targetId: String) {
        val message = "$title: $body"
        showNotification(
            title = title,
            message = message,
            channelId = "messages",
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    /**
     * Exibe notificação no notification manager.
     */
    private fun showNotification(
        title: String,
        message: String,
        channelId: String = "general",
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        // Criar canal se não existir (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    /**
     * Cria canal de notificação com as configurações apropriadas.
     */
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val (name, description, importance) = when (channelId) {
            "grades" -> Triple("Notas", "Notificações de notas lançadas", NotificationManager.IMPORTANCE_HIGH)
            "attendance" -> Triple("Frequência", "Alertas de frequência baixa", NotificationManager.IMPORTANCE_HIGH)
            "announcements" -> Triple("Avisos", "Comunicados da escola", NotificationManager.IMPORTANCE_DEFAULT)
            "events" -> Triple("Eventos", "Eventos da escola", NotificationManager.IMPORTANCE_DEFAULT)
            "messages" -> Triple("Mensagens", "Mensagens de responsáveis", NotificationManager.IMPORTANCE_HIGH)
            else -> Triple("Geral", "Notificações gerais", NotificationManager.IMPORTANCE_DEFAULT)
        }

        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Envia token do dispositivo para servidor.
     */
    private fun sendTokenToServer(token: String) {
        // Salvar em SharedPreferences ou enviar para servidor
        val prefs = getSharedPreferences("fcm", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // TODO: Enviar para API do servidor
        // repository.updateDeviceToken(token)
    }

    /**
     * Log de notificação recebida para analytics.
     */
    private fun logNotificationReceived(type: String, title: String) {
        val timestamp = Date().time
        // TODO: Implementar logging/analytics
        android.util.Log.d(
            "FCMNotification",
            "Received: type=$type, title=$title, time=$timestamp"
        )
    }
}

/**
 * Gerenciador de notificações FCM - Métodos auxiliares para enviar notificações.
 */
object FCMNotificationManager {

    /**
     * Subscreve user a um tópico (para notificações em massa).
     */
    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }

    /**
     * Desinscreve user de um tópico.
     */
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
    }

    /**
     * Tópicos disponíveis para subscrição.
     */
    object Topics {
        const val GRADES = "grades"              // Notificações de notas
        const val ATTENDANCE = "attendance"      // Alertas de frequência
        const val ANNOUNCEMENTS = "announcements" // Comunicados
        const val EVENTS = "events"              // Eventos
        const val ALL_PARENTS = "all_parents"    // Para todos pais
    }

    /**
     * Obtém token FCM do dispositivo.
     */
    fun getDeviceToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result)
            } else {
                callback(null)
            }
        }
    }

    /**
     * Configura listener para token updates.
     */
    fun setupTokenListener(context: Context) {
        getDeviceToken { token ->
            if (token != null) {
                val prefs = context.getSharedPreferences("fcm", Context.MODE_PRIVATE)
                prefs.edit().putString("fcm_token", token).apply()
            }
        }
    }
}

/**
 * Notificação push que pode ser enviada via API.
 */
data class PushNotification(
    val title: String,
    val body: String,
    val type: String,  // grade_released, absence_alert, announcement, event, parent_message
    val targetId: String = "",  // studentId, classId, etc.
    val recipientRole: String = "parent",  // parent, teacher, admin
    val sendToTopic: Boolean = false,  // Se true, envia para tópico em vez de indivíduo
    val topicName: String = ""
) {
    /**
     * Converte para mapa para envio via API.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "notification" to mapOf(
            "title" to title,
            "body" to body
        ),
        "data" to mapOf(
            "type" to type,
            "targetId" to targetId,
            "recipientRole" to recipientRole,
            "timestamp" to System.currentTimeMillis()
        ),
        "android" to mapOf(
            "priority" to "high",
            "notification" to mapOf(
                "click_action" to "FLUTTER_NOTIFICATION_CLICK"
            )
        ),
        "apns" to mapOf(
            "payload" to mapOf(
                "aps" to mapOf(
                    "alert" to mapOf(
                        "title" to title,
                        "body" to body
                    ),
                    "badge" to 1,
                    "sound" to "default"
                )
            )
        )
    )
}
