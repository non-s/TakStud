package com.example.takstud.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de sessão com armazenamento seguro e encriptado.
 *
 * Responsabilidades:
 * - Armazenamento criptografado de dados de sessão usando EncryptedSharedPreferences
 * - Validação automática de expiração (timeout 12 horas)
 * - Serialização/desserialização segura com Gson
 * - Auditoria de acesso via Android Log
 * - Limpeza automática de sessões expiradas
 *
 * Segurança:
 * - EncryptedSharedPreferences com AES256_GCM para dados sensíveis
 * - MasterKey com AES256_GCM gerada pelo Android SecurityProvider
 * - Chaves criptografadas com AES256_SIV
 * - Valores criptografados com AES256_GCM
 * - Sessão expires em 12 horas (timeout automático)
 *
 * Arquitetura:
 * ```
 * App (Auth Screen)
 *     ↓
 * SecureSessionManager
 *     ↓
 * EncryptedSharedPreferences (device-encrypted)
 *     ↓
 * Device Storage (encrypted at rest)
 * ```
 *
 * Fluxo típico:
 * ```kotlin
 * val sessionManager = SecureSessionManager(context)
 *
 * // Após login bem-sucedido
 * val session = UserSession(
 *     userId = user.id,
 *     role = "TEACHER",
 *     name = user.name
 * )
 * sessionManager.saveSession(session)
 *
 * // Em screens subsequentes
 * if (sessionManager.isSessionActive()) {
 *     val session = sessionManager.getActiveSession()
 *     val remainingMinutes = sessionManager.getSessionRemainingTime()
 *     showWelcome("${session.name}, expira em ${remainingMinutes}m")
 * } else {
 *     navigateToLogin()
 * }
 *
 * // Ao fazer logout
 * sessionManager.clearSession()
 * ```
 *
 * @see UserSession
 * @see EncryptedSharedPreferences
 */
class SecureSessionManager(private val context: Context) {

    companion object {
        private const val TAG = "SecureSessionManager"
        /** Nome do arquivo EncryptedSharedPreferences */
        private const val PREFS_NAME = "takstud_secure_session"
        /** Chave para armazenar JSON da sessão */
        private const val SESSION_KEY = "active_session"
        /** Tempo de expiração da sessão (12 horas) */
        private const val EXPIRY_TIME_HOURS = 12L
    }

    private val encryptedPrefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao criar EncryptedSharedPreferences", e)
            throw RuntimeException("Não foi possível inicializar armazenamento seguro", e)
        }
    }

    /**
     * Salva uma sessão de usuário de forma criptografada.
     *
     * Fluxo:
     * 1. Serializa UserSession para JSON usando Gson
     * 2. Armazena JSON em EncryptedSharedPreferences (criptografado)
     * 3. Armazena timestamp de criação (para cálculo de expiração)
     * 4. Log de auditoria em nível INFO
     *
     * Segurança:
     * - Dados são criptografados antes de armazenar
     * - Session expires em 12 horas automaticamente
     * - Timestamp é obtido do relógio do sistema
     *
     * @param session Sessão do usuário com userId, role, name
     *
     * @throws Exception se falhar criptografia ou armazenamento
     *
     * Exemplo:
     * ```kotlin
     * val session = UserSession(
     *     userId = "user_123",
     *     role = "TEACHER",
     *     name = "Prof. João Silva"
     * )
     * sessionManager.saveSession(session)
     * ```
     *
     * @see UserSession
     * @see getActiveSession
     */
    fun saveSession(session: UserSession) {
        try {
            val gson = Gson()
            val sessionJson = gson.toJson(session)
            encryptedPrefs.edit().apply {
                putString(SESSION_KEY, sessionJson)
                putLong("session_created_at", System.currentTimeMillis())
                apply()
            }
            Log.i(TAG, "Sessão salva com segurança para usuário: ${session.userId}")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar sessão", e)
            throw e
        }
    }

    /**
     * Recupera a sessão ativa se ainda for válida e não expirada.
     *
     * Fluxo:
     * 1. Recupera JSON da sessão do EncryptedSharedPreferences
     * 2. Se não existe, retorna null
     * 3. Verifica se sessão já expirou (passaram 12 horas)
     * 4. Se expirada, limpa e retorna null
     * 5. Desserializa JSON para UserSession usando Gson
     * 6. Retorna sessão válida
     *
     * Tratamento de erros:
     * - JsonSyntaxException: JSON corrompido ou inválido (retorna null)
     * - IOException: Erro ao descriptografar (retorna null)
     * - Exception: Erro genérico (retorna null)
     *
     * @return UserSession se válida e não expirada, null caso contrário
     *
     * Exemplo:
     * ```kotlin
     * val session = sessionManager.getActiveSession()
     * if (session != null) {
     *     showWelcome("Bem-vindo, ${session.name}!")
     * } else {
     *     showLogin("Sessão expirada, faça login novamente")
     * }
     * ```
     *
     * @see UserSession
     * @see saveSession
     * @see isSessionActive
     */
    fun getActiveSession(): UserSession? {
        return try {
            val sessionJson = encryptedPrefs.getString(SESSION_KEY, null) ?: return null
            val createdAt = encryptedPrefs.getLong("session_created_at", 0L)

            // Verificar expiração (12 horas)
            if (isSessionExpired(createdAt)) {
                Log.w(TAG, "Sessão expirada, removendo")
                clearSession()
                return null
            }

            val gson = Gson()
            val session = gson.fromJson(sessionJson, UserSession::class.java)
            Log.d(TAG, "Sessão recuperada para usuário: ${session.userId}")
            session
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Erro ao fazer parse da sessão", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recuperar sessão", e)
            null
        }
    }

    /**
     * Verifica se existe uma sessão ativa, válida e não expirada.
     *
     * Conveniência para checar antes de recuperar sessão completa.
     *
     * @return true se sessão existe e é válida, false caso contrário
     *
     * Exemplo:
     * ```kotlin
     * if (sessionManager.isSessionActive()) {
     *     navigateToHome()
     * } else {
     *     navigateToLogin()
     * }
     * ```
     *
     * @see getActiveSession
     */
    fun isSessionActive(): Boolean {
        val session = getActiveSession()
        return session != null
    }

    /**
     * Limpa a sessão atual (logout).
     *
     * Remove tanto o JSON da sessão quanto o timestamp de criação
     * do EncryptedSharedPreferences.
     * Erro ao limpar não lança exceção.
     *
     * @see saveSession
     * @see getActiveSession
     */
    fun clearSession() {
        try {
            encryptedPrefs.edit().apply {
                remove(SESSION_KEY)
                remove("session_created_at")
                apply()
            }
            Log.i(TAG, "Sessão limpa com segurança")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao limpar sessão", e)
        }
    }

    /**
     * Retorna tempo restante até expiração da sessão em minutos.
     *
     * Cálculo:
     * 1. Obtém timestamp de criação
     * 2. Calcula tempo de expiração = criado + 12 horas
     * 3. Calcula tempo restante = expiração - agora
     * 4. Converte para minutos
     *
     * Casos especiais:
     * - Se não há sessão (timestamp 0), retorna 0
     * - Se já expirou (tempo < 0), retorna 0
     * - Se tempo > 0, retorna minutos arredondados para baixo
     *
     * @return Minutos restantes até expiração (0 se nenhum ou expirado)
     *
     * Exemplo:
     * ```kotlin
     * val remainingMinutes = sessionManager.getSessionRemainingTime()
     * if (remainingMinutes < 15) {
     *     showWarning("Sessão expira em ${remainingMinutes} minutos")
     * }
     * ```
     *
     * @see getActiveSession
     */
    fun getSessionRemainingTime(): Long {
        val createdAt = encryptedPrefs.getLong("session_created_at", 0L)
        if (createdAt == 0L) return 0

        val expiryTime = createdAt + TimeUnit.HOURS.toMillis(EXPIRY_TIME_HOURS)
        val remainingTime = expiryTime - System.currentTimeMillis()

        return if (remainingTime > 0) TimeUnit.MILLISECONDS.toMinutes(remainingTime) else 0
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Verifica se a sessão já expirou.
     *
     * Uma sessão expira 12 horas após sua criação.
     *
     * @param createdAt Timestamp de criação em milissegundos
     *
     * @return true se expirou (passou 12 horas), false caso contrário
     */
    private fun isSessionExpired(createdAt: Long): Boolean {
        if (createdAt == 0L) return true
        val expiryTime = createdAt + TimeUnit.HOURS.toMillis(EXPIRY_TIME_HOURS)
        return System.currentTimeMillis() > expiryTime
    }
}

/**
 * Modelo de sessão do usuário para serialização segura.
 *
 * Representa dados de autenticação persistidos no EncryptedSharedPreferences.
 * Serializa para JSON com Gson para armazenamento criptografado.
 *
 * Campos:
 * - userId: Identificador único do usuário (ex: "user_12345")
 * - role: Papel/perfil do usuário (TEACHER, PARENT, ADMIN)
 * - name: Nome completo para exibição na UI
 * - createdAt: Timestamp Unix em ms de quando foi criada (para expiração)
 *
 * Armazenamento:
 * - Convertido para JSON: {"userId":"...", "role":"...", "name":"...", "createdAt":...}
 * - Criptografado antes de armazenar no SharedPreferences
 * - Automaticamente expirado após 12 horas
 *
 * Serialização:
 * - Usa Gson para converte para/de JSON
 * - Campos públicos são serializados automaticamente
 * - Data class implementa equals/hashCode/toString
 *
 * @param userId Identificador único do usuário
 * @param role Papel do usuário (TEACHER, PARENT, ADMIN)
 * @param name Nome completo para exibição na UI
 * @param createdAt Timestamp de criação em milissegundos (padrão: agora)
 *
 * Exemplo:
 * ```kotlin
 * val session = UserSession(
 *     userId = "user_123",
 *     role = "TEACHER",
 *     name = "Prof. João da Silva",
 *     createdAt = System.currentTimeMillis()
 * )
 * sessionManager.saveSession(session)
 *
 * // Depois, ao recuperar
 * val savedSession = sessionManager.getActiveSession()
 * if (savedSession != null && savedSession.role == "TEACHER") {
 *     showTeacherPanel()
 * }
 * ```
 *
 * @see SecureSessionManager
 * @see SecureSessionManager.saveSession
 * @see SecureSessionManager.getActiveSession
 */
data class UserSession(
    val userId: String,
    val role: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
