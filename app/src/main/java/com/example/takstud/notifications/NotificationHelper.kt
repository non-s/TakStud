package com.example.takstud.notifications

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * NotificationHelper - Utilitário para gerenciar notificações FCM
 * Permite inscrição em tópicos e envio de notificações de teste
 */
object NotificationHelper {

    /**
     * Tópicos de notificação disponíveis
     */
    object Topics {
        const val TASKS = "tasks"
        const val NOTICES = "notices"
        const val ATTENDANCE = "attendance"
        const val GRADES = "grades"
        const val ALL = "all"
    }

    /**
     * Inscrever em tópico
     *
     * @param topic Nome do tópico
     */
    suspend fun subscribeToTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cancelar inscrição em tópico
     *
     * @param topic Nome do tópico
     */
    suspend fun unsubscribeFromTopic(topic: String) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Obter token FCM do dispositivo
     *
     * @return Token FCM
     */
    suspend fun getDeviceToken(): String {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Inscrever em múltiplos tópicos
     *
     * @param topics Lista de tópicos
     */
    suspend fun subscribeToTopics(topics: List<String>) {
        topics.forEach { topic ->
            subscribeToTopic(topic)
        }
    }

    /**
     * Configuração padrão de tópicos para novo usuário
     * Professor se inscreve em todos os tópicos
     * Aluno/Responsável se inscreve apenas em seu tópico de turma
     *
     * @param userRole Função do usuário (TEACHER, PARENT)
     * @param userClass Turma do usuário (se aplicável)
     */
    suspend fun setupDefaultTopicSubscriptions(userRole: String, userClass: String = "") {
        try {
            // Todos se inscrevem em avisos gerais
            subscribeToTopic(Topics.ALL)

            when (userRole) {
                "TEACHER" -> {
                    // Professores recebem todas as notificações
                    subscribeToTopics(
                        listOf(
                            Topics.TASKS,
                            Topics.NOTICES,
                            Topics.ATTENDANCE,
                            Topics.GRADES
                        )
                    )
                }
                "PARENT" -> {
                    // Responsáveis recebem notificações da sua turma
                    if (userClass.isNotEmpty()) {
                        subscribeToTopic("class_$userClass")
                    }
                    // Recebem notificações de tarefas e avisos
                    subscribeToTopics(
                        listOf(
                            Topics.TASKS,
                            Topics.NOTICES
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Limpar inscrições ao logout
     */
    suspend fun clearNotificationSubscriptions() {
        try {
            val allTopics = listOf(
                Topics.TASKS,
                Topics.NOTICES,
                Topics.ATTENDANCE,
                Topics.GRADES,
                Topics.ALL
            )
            allTopics.forEach { topic ->
                unsubscribeFromTopic(topic)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
