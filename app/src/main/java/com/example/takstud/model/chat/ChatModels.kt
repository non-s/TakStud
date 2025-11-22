package com.example.takstud.model.chat

import java.util.UUID

/**
 * 💬 Canal de Chat
 * Representa uma conversa entre usuários ou um grupo.
 */
data class ChatChannel(
    val id: String = UUID.randomUUID().toString(),
    val type: ChannelType = ChannelType.DIRECT,
    val name: String = "", // Nome do grupo ou vazio para direto
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(), // Cache de nomes: userId -> name
    val lastMessage: ChatMessage? = null,
    val unreadCounts: Map<String, Int> = emptyMap(), // userId -> count
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {
    fun getDisplayName(currentUserId: String): String {
        return if (type == ChannelType.GROUP) {
            name
        } else {
            // Para chat direto, mostrar o nome do outro participante
            participantNames.filterKeys { it != currentUserId }.values.firstOrNull() ?: "Chat"
        }
    }
}

enum class ChannelType {
    DIRECT, // 1-on-1
    GROUP,  // Grupo (ex: Turma, Pais e Mestres)
    BROADCAST // Canal de avisos (apenas admin posta)
}

/**
 * 📨 Mensagem de Chat
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val channelId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val attachments: List<String> = emptyList(), // URLs
    val timestamp: Long = System.currentTimeMillis(),
    val readBy: List<String> = emptyList(), // Lista de IDs que leram
    val isDeleted: Boolean = false
)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    AUDIO,
    SYSTEM // Mensagens do sistema (ex: "Fulano entrou no grupo")
}
