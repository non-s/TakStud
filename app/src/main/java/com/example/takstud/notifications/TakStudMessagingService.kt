package com.example.takstud.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.takstud.MainActivity
import com.example.takstud.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * TakStudMessagingService - Serviço para receber notificações push via Firebase Cloud Messaging
 * Lida com notificações de tarefas, avisos, presença e mensagens
 */
class TakStudMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Notificações com dados (dados de tela cheia)
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }

        // Notificações com notificação (padrão)
        remoteMessage.notification?.let {
            handleNotification(it, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        // Novo token de dispositivo - enviar para servidor
        sendTokenToServer(token)
    }

    /**
     * Processar mensagens de dados
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        val title = data["title"] ?: "TakStud"
        val message = data["message"] ?: ""
        val targetScreen = data["target_screen"]

        when (type) {
            "task" -> showTaskNotification(title, message, data)
            "notice" -> showNoticeNotification(title, message, data)
            "attendance" -> showAttendanceNotification(title, message, data)
            "grade" -> showGradeNotification(title, message, data)
            else -> showDefaultNotification(title, message)
        }
    }

    /**
     * Processar notificação padrão
     */
    private fun handleNotification(notification: RemoteMessage.Notification, data: Map<String, String>) {
        val title = notification.title ?: "TakStud"
        val message = notification.body ?: ""

        showDefaultNotification(title, message)
    }

    /**
     * Mostrar notificação de tarefa
     */
    private fun showTaskNotification(title: String, message: String, data: Map<String, String>) {
        val notificationId = 1001
        val channel = createNotificationChannel(
            "tasks_channel",
            "Tarefas e Testes",
            NotificationManager.IMPORTANCE_HIGH
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "tasks")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        showNotification(
            notificationId,
            channel,
            title,
            message,
            intent,
            R.drawable.ic_launcher_foreground
        )
    }

    /**
     * Mostrar notificação de aviso
     */
    private fun showNoticeNotification(title: String, message: String, data: Map<String, String>) {
        val notificationId = 1002
        val channel = createNotificationChannel(
            "notices_channel",
            "Avisos",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "notices")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        showNotification(
            notificationId,
            channel,
            title,
            message,
            intent,
            R.drawable.ic_launcher_foreground
        )
    }

    /**
     * Mostrar notificação de presença
     */
    private fun showAttendanceNotification(title: String, message: String, data: Map<String, String>) {
        val notificationId = 1003
        val channel = createNotificationChannel(
            "attendance_channel",
            "Presença",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "attendance")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        showNotification(
            notificationId,
            channel,
            title,
            message,
            intent,
            R.drawable.ic_launcher_foreground
        )
    }

    /**
     * Mostrar notificação de nota
     */
    private fun showGradeNotification(title: String, message: String, data: Map<String, String>) {
        val notificationId = 1004
        val channel = createNotificationChannel(
            "grades_channel",
            "Notas",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "grades")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        showNotification(
            notificationId,
            channel,
            title,
            message,
            intent,
            R.drawable.ic_launcher_foreground
        )
    }

    /**
     * Mostrar notificação padrão
     */
    private fun showDefaultNotification(title: String, message: String) {
        val notificationId = 1000
        val channel = createNotificationChannel(
            "default_channel",
            "Notificações",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        showNotification(
            notificationId,
            channel,
            title,
            message,
            intent,
            R.drawable.ic_launcher_foreground
        )
    }

    /**
     * Criar notification channel (Android 8+)
     */
    private fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = "Notificações de $channelName"

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    /**
     * Mostrar notificação
     */
    private fun showNotification(
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        intent: Intent,
        smallIcon: Int
    ) {
        val pendingIntentFlags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(smallIcon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Enviar token de dispositivo para servidor
     * Implementar conforme necessário para o backend
     */
    private fun sendTokenToServer(token: String) {
        // TODO: Enviar token para Firebase Firestore ou servidor backend
        // Salvar em SharedPreferences ou DataStore para referência futura
    }
}
