package com.example.takstud.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.takstud.model.Role
import com.google.gson.Gson

/**
 * SessionStorage - Gerencia persistência segura de sessão
 * Usa EncryptedSharedPreferences para armazenar dados de forma segura
 */
class SessionStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "session_storage",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_STUDENT_RA = "student_ra"
        private const val KEY_STUDENT_ID = "student_id"
        private const val KEY_STUDENT_NAME = "student_name"
        private const val KEY_STUDENT_CLASS = "student_class"
        private const val KEY_LAST_LOGIN = "last_login"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    }

    /**
     * Salvar sessão de professor
     */
    fun saveTeacherSession(
        userId: String,
        email: String,
        name: String,
        rememberMe: Boolean = false
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_ROLE, Role.TEACHER.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Salvar sessão de responsável
     */
    fun saveParentSession(
        userId: String,
        studentRa: String,
        studentId: String,
        studentName: String,
        studentClass: String,
        parentName: String,
        rememberMe: Boolean = false
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, Role.PARENT.name)
            putString(KEY_STUDENT_RA, studentRa)
            putString(KEY_STUDENT_ID, studentId)
            putString(KEY_STUDENT_NAME, studentName)
            putString(KEY_STUDENT_CLASS, studentClass)
            putString(KEY_USER_NAME, parentName)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Obter sessão salva
     */
    fun getSessionRole(): Role? {
        val roleString = sharedPreferences.getString(KEY_USER_ROLE, null)
        return roleString?.let { Role.valueOf(it) }
    }

    /**
     * Obter ID do usuário
     */
    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)

    /**
     * Obter ID do aluno (se parent)
     */
    fun getStudentId(): String? = sharedPreferences.getString(KEY_STUDENT_ID, null)

    /**
     * Obter RA do aluno (se parent)
     */
    fun getStudentRA(): String? = sharedPreferences.getString(KEY_STUDENT_RA, null)

    /**
     * Verificar se há sessão válida
     */
    fun isLoggedIn(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return false

        // Verificar timeout (30 minutos = 1.800.000 ms)
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0)
        val timeout = 30 * 60 * 1000L // 30 minutos
        val isExpired = System.currentTimeMillis() - loginTime > timeout

        if (isExpired) {
            logout()
            return false
        }

        return true
    }

    /**
     * Verificar se deve fazer auto-login
     */
    fun shouldAutoLogin(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false) && isLoggedIn()
    }

    /**
     * Obter tempo desde o último login
     */
    fun getTimeSinceLogin(): Long {
        val loginTime = sharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0)
        return System.currentTimeMillis() - loginTime
    }

    /**
     * Fazer logout (limpar todos os dados)
     */
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Obter dados da sessão (para debug)
     */
    fun getSessionData(): Map<String, Any?> {
        return mapOf(
            "userId" to getUserId(),
            "role" to getSessionRole(),
            "studentId" to getStudentId(),
            "studentRA" to getStudentRA(),
            "isLoggedIn" to isLoggedIn(),
            "shouldAutoLogin" to shouldAutoLogin()
        )
    }

    /**
     * Limpar cache periodicamente
     */
    fun clearExpiredSession() {
        if (!isLoggedIn()) {
            logout()
        }
    }
}
