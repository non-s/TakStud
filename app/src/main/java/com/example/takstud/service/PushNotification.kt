package com.example.takstud.service

/**
 * Modelo simplificado para notificações push.
 */
data class PushNotification(
    val title: String = "",
    val body: String = "",
    val type: String = "general",
    val targetId: String = "",
    val recipientRole: String = "",
    val sendToTopic: Boolean = false,
    val topicName: String = ""
)
