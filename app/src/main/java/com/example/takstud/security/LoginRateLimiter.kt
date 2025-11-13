package com.example.takstud.security

import android.content.SharedPreferences
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * Implementa rate limiting para proteção contra ataques de força bruta em login.
 *
 * Limita tentativas de login falhadas a máximo 5 por hora por usuário.
 * Após exceder o limite, o usuário é bloqueado por 1 hora.
 *
 * Armazenamento:
 * - Usa SharedPreferences para rastreamento persistente entre execuções
 * - Chaves: "login_attempts_<identifier>" e "login_timestamp_<identifier>"
 * - Dados sobrevivem ao restart do app
 *
 * Fluxo de uso típico:
 * ```kotlin
 * val limiter = LoginRateLimiter(sharedPreferences)
 *
 * // Verificar se pode tentar
 * if (!limiter.isAllowedToLogin(userInput)) {
 *     val secondsLeft = limiter.getSecondsUntilRetry(userInput)
 *     showError("Bloqueado. Tente em ${secondsLeft}s")
 *     return
 * }
 *
 * // Tentar login
 * try {
 *     loginUser(userInput)
 *     limiter.clearAttempts(userInput)  // Sucesso: limpa contador
 * } catch (e: Exception) {
 *     limiter.recordFailedAttempt(userInput)  // Falha: incrementa tentativa
 *     val remaining = limiter.getRemainingAttempts(userInput)
 *     showError("Falha. Tentativas restantes: $remaining")
 * }
 * ```
 *
 * Segurança:
 * - Protege contra ataques de força bruta (dicionário, brute force)
 * - Case-insensitive: "JOÃO" e "joão" contam como mesmo usuário
 * - Whitespace trimado: "  joão  " e "joão" contam como mesmo usuário
 * - SharedPreferences é local, não sincronizado na nuvem
 *
 * @see isAllowedToLogin
 * @see recordFailedAttempt
 * @see getRemainingAttempts
 * @see getSecondsUntilRetry
 */
class LoginRateLimiter(private val prefs: SharedPreferences) {

    companion object {
        private const val TAG = "LoginRateLimiter"
        /** Máximo de tentativas de login falhadas permitidas */
        private const val MAX_ATTEMPTS = 5
        /** Prefixo para chave de armazenamento de tentativas */
        private const val PREFIX_ATTEMPTS = "login_attempts_"
        /** Prefixo para chave de armazenamento de timestamp */
        private const val PREFIX_TIMESTAMP = "login_timestamp_"
        /** Janela de tempo para rate limiting: 1 hora (em milissegundos) */
        private val TIME_WINDOW_MILLIS = TimeUnit.HOURS.toMillis(1)
    }

    /**
     * Verifica se o usuário pode tentar fazer login.
     *
     * Retorna false se o usuário excedeu 5 tentativas falhadas e
     * menos de 1 hora passou desde a última tentativa.
     *
     * Normalização: identifier é trimado e convertido para lowercase.
     *
     * @param identifier RA, email ou identificador do usuário
     *
     * @return true se login é permitido (tentativas < 5 OU passou 1 hora),
     *         false se bloqueado (tentativas >= 5 E menos de 1 hora)
     *
     * Exemplo:
     * ```kotlin
     * if (limiter.isAllowedToLogin("2024001")) {
     *     attemptLogin(credentials)
     * } else {
     *     val seconds = limiter.getSecondsUntilRetry("2024001")
     *     showBlocked("Bloqueado por $seconds segundos")
     * }
     * ```
     *
     * @see recordFailedAttempt
     * @see getSecondsUntilRetry
     */
    fun isAllowedToLogin(identifier: String): Boolean {
        val cleanIdentifier = identifier.trim().lowercase()
        val attempts = getAttempts(cleanIdentifier)

        if (attempts >= MAX_ATTEMPTS) {
            val lastAttemptTime = getLastAttemptTime(cleanIdentifier)
            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime

            if (timeSinceLastAttempt < TIME_WINDOW_MILLIS) {
                Log.w(TAG, "Tentativas excedidas para: $cleanIdentifier")
                return false
            } else {
                // Reset após 1 hora
                resetAttempts(cleanIdentifier)
                return true
            }
        }
        return true
    }

    /**
     * Registra uma tentativa de login falhada.
     *
     * Incrementa contador de tentativas e atualiza timestamp.
     * Logging é feito em nível WARN com detalhes da tentativa.
     *
     * Normalização: identifier é trimado e convertido para lowercase.
     *
     * @param identifier RA, email ou identificador do usuário
     *
     * Exemplo:
     * ```kotlin
     * try {
     *     loginUser(credentials)
     * } catch (e: LoginException) {
     *     limiter.recordFailedAttempt(userInput)  // Registra falha
     *     val remaining = limiter.getRemainingAttempts(userInput)
     *     if (remaining == 0) {
     *         showError("Bloqueado por 1 hora após 5 falhas")
     *     } else {
     *         showError("Falha. Tentativas restantes: $remaining")
     *     }
     * }
     * ```
     *
     * @see isAllowedToLogin
     * @see getRemainingAttempts
     */
    fun recordFailedAttempt(identifier: String) {
        val cleanIdentifier = identifier.trim().lowercase()
        val currentAttempts = getAttempts(cleanIdentifier)
        val key = PREFIX_ATTEMPTS + cleanIdentifier

        prefs.edit().apply {
            putInt(key, currentAttempts + 1)
            putLong(PREFIX_TIMESTAMP + cleanIdentifier, System.currentTimeMillis())
            apply()
        }

        Log.w(TAG, "Tentativa falhada registrada para: $cleanIdentifier (${currentAttempts + 1}/$MAX_ATTEMPTS)")
    }

    /**
     * Limpa o contador de tentativas após login bem-sucedido.
     *
     * Remove chaves de armazenamento do SharedPreferences.
     * Deve ser chamado após autenticação bem-sucedida.
     *
     * @param identifier RA, email ou identificador do usuário
     *
     * Exemplo:
     * ```kotlin
     * try {
     *     val user = loginUser(credentials)
     *     limiter.clearAttempts(userInput)  // Sucesso: limpa tentativas
     *     navigateToHome()
     * } catch (e: LoginException) {
     *     limiter.recordFailedAttempt(userInput)  // Falha: registra tentativa
     * }
     * ```
     *
     * @see recordFailedAttempt
     */
    fun clearAttempts(identifier: String) {
        val cleanIdentifier = identifier.trim().lowercase()
        prefs.edit().apply {
            remove(PREFIX_ATTEMPTS + cleanIdentifier)
            remove(PREFIX_TIMESTAMP + cleanIdentifier)
            apply()
        }
    }

    /**
     * Retorna quantas tentativas falhadas restam até o bloqueio.
     *
     * Se usuário já foi bloqueado e passou 1 hora, retorna MAX_ATTEMPTS (5).
     * Se usuário não tem tentativas registradas, retorna MAX_ATTEMPTS (5).
     *
     * @param identifier RA, email ou identificador do usuário
     *
     * @return Número de tentativas restantes (0 a 5)
     *         - 5 = nenhuma tentativa registrada
     *         - 0 = bloqueado (5 ou mais tentativas, menos de 1 hora)
     *
     * Exemplo:
     * ```kotlin
     * val remaining = limiter.getRemainingAttempts("2024001")
     * when {
     *     remaining > 0 -> showError("Tentativas restantes: $remaining")
     *     remaining == 0 -> {
     *         val seconds = limiter.getSecondsUntilRetry("2024001")
     *         showBlocked("Bloqueado. Tente em $seconds segundos")
     *     }
     * }
     * ```
     *
     * @see recordFailedAttempt
     * @see getSecondsUntilRetry
     */
    fun getRemainingAttempts(identifier: String): Int {
        val cleanIdentifier = identifier.trim().lowercase()
        val attempts = getAttempts(cleanIdentifier)
        return maxOf(0, MAX_ATTEMPTS - attempts)
    }

    /**
     * Retorna tempo em segundos até poder tentar login novamente.
     *
     * Se usuário não foi bloqueado, retorna 0.
     * Se passou 1 hora desde a última tentativa, retorna 0.
     * Caso contrário, retorna segundos restantes da janela de 1 hora.
     *
     * @param identifier RA, email ou identificador do usuário
     *
     * @return Segundos até poder tentar login novamente (0 se permitido)
     *
     * Exemplo:
     * ```kotlin
     * if (!limiter.isAllowedToLogin(userInput)) {
     *     val seconds = limiter.getSecondsUntilRetry(userInput)
     *     val minutes = seconds / 60
     *     val remainingSeconds = seconds % 60
     *     showBlocked("Bloqueado. Tente em ${minutes}m ${remainingSeconds}s")
     * }
     * ```
     *
     * @see isAllowedToLogin
     * @see getRemainingAttempts
     */
    fun getSecondsUntilRetry(identifier: String): Long {
        val cleanIdentifier = identifier.trim().lowercase()
        val attempts = getAttempts(cleanIdentifier)

        if (attempts < MAX_ATTEMPTS) return 0

        val lastAttemptTime = getLastAttemptTime(cleanIdentifier)
        val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime
        val remainingTime = maxOf(0, TIME_WINDOW_MILLIS - timeSinceLastAttempt)

        return TimeUnit.MILLISECONDS.toSeconds(remainingTime)
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Obtém número de tentativas falhadas registradas.
     *
     * @param identifier Identificador normalizado (lowercase)
     * @return Número de tentativas (0 se não registrado)
     */
    private fun getAttempts(identifier: String): Int {
        return prefs.getInt(PREFIX_ATTEMPTS + identifier, 0)
    }

    /**
     * Obtém timestamp da última tentativa falhada em milissegundos.
     *
     * @param identifier Identificador normalizado (lowercase)
     * @return Timestamp em ms (0 se não registrado)
     */
    private fun getLastAttemptTime(identifier: String): Long {
        return prefs.getLong(PREFIX_TIMESTAMP + identifier, 0L)
    }

    /**
     * Reseta contador de tentativas após passar 1 hora de bloqueio.
     *
     * Remove ambas as chaves de armazenamento (tentativas e timestamp).
     *
     * @param identifier Identificador normalizado (lowercase)
     */
    private fun resetAttempts(identifier: String) {
        prefs.edit().apply {
            remove(PREFIX_ATTEMPTS + identifier)
            remove(PREFIX_TIMESTAMP + identifier)
            apply()
        }
    }
}
