package com.example.takstud.data.repository

import com.example.takstud.model.chat.ChatChannel
import com.example.takstud.model.chat.ChatMessage
import com.example.takstud.util.firestoreQueryFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    /**
     * Obtém canais onde o usuário participa.
     */
    fun getChannelsForUser(userId: String): Flow<List<ChatChannel>> {
        return firestoreQueryFlow(
            db.collection("channels")
                .whereArrayContains("participantIds", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING),
            ChatChannel::class.java,
            "TakStud"
        )
    }

    /**
     * Obtém mensagens de um canal.
     */
    fun getMessagesForChannel(channelId: String): Flow<List<ChatMessage>> {
        return firestoreQueryFlow(
            db.collection("channels").document(channelId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING),
            ChatMessage::class.java,
            "TakStud"
        )
    }

    /**
     * Envia uma mensagem.
     */
    suspend fun sendMessage(channelId: String, message: ChatMessage) {
        val channelRef = db.collection("channels").document(channelId)
        val messageRef = channelRef.collection("messages").document(message.id)

        db.runTransaction { transaction ->
            // Salvar mensagem
            transaction.set(messageRef, message)

            // Atualizar canal com última mensagem e timestamp
            transaction.update(channelRef, mapOf(
                "lastMessage" to message,
                "updatedAt" to message.timestamp
                // TODO: Incrementar unread counts para outros participantes
            ))
        }.await()
    }

    /**
     * Cria um novo canal.
     */
    suspend fun createChannel(channel: ChatChannel): String {
        val ref = if (channel.id.isBlank()) db.collection("channels").document() else db.collection("channels").document(channel.id)
        val channelToSave = channel.copy(id = ref.id)
        ref.set(channelToSave).await()
        return ref.id
    }
}
