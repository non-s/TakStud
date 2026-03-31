package com.example.takstud.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * LocalizationManager - Gerencia preferência de idioma da aplicação
 * Suporta múltiplos idiomas com fallback para português
 */
class LocalizationManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "localization_preferences"
        private val LANGUAGE_KEY = stringPreferencesKey("language")

        // Idiomas suportados
        const val PT_BR = "pt_BR" // Português Brasil
        const val EN_US = "en_US" // English
        const val ES_ES = "es_ES" // Español

        val SUPPORTED_LANGUAGES = mapOf(
            PT_BR to "Português",
            EN_US to "English",
            ES_ES to "Español"
        )
    }

    /**
     * Obter idioma atual como Flow
     */
    fun getCurrentLanguageFlow(): Flow<String> {
        return context.localizationDataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: getSystemLanguage()
        }
    }

    /**
     * Setar idioma
     */
    suspend fun setLanguage(language: String) {
        if (!SUPPORTED_LANGUAGES.containsKey(language)) {
            throw IllegalArgumentException("Idioma não suportado: $language")
        }
        context.localizationDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    /**
     * Obter idioma do sistema
     */
    private fun getSystemLanguage(): String {
        val systemLanguage = java.util.Locale.getDefault().language
        val systemCountry = java.util.Locale.getDefault().country

        return when {
            systemLanguage == "pt" && systemCountry == "BR" -> PT_BR
            systemLanguage == "en" -> EN_US
            systemLanguage == "es" -> ES_ES
            else -> PT_BR // Fallback para português
        }
    }

    /**
     * Obter nome do idioma em exibição
     */
    fun getLanguageName(language: String): String {
        return SUPPORTED_LANGUAGES[language] ?: "Desconhecido"
    }

    /**
     * Resetar para idioma padrão do sistema
     */
    suspend fun resetToSystemLanguage() {
        context.localizationDataStore.edit { preferences ->
            preferences.remove(LANGUAGE_KEY)
        }
    }
}

// Extensão para criar DataStore
private val Context.localizationDataStore: androidx.datastore.core.DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
    name = "localization_preferences"
)

/**
 * Strings traduzidas por idioma
 */
object LocalizedStrings {

    /**
     * Obter string traduzida baseada no idioma
     */
    fun getString(key: String, language: String): String {
        return when (language) {
            LocalizationManager.PT_BR -> getPortugueseString(key)
            LocalizationManager.EN_US -> getEnglishString(key)
            LocalizationManager.ES_ES -> getSpanishString(key)
            else -> getPortugueseString(key) // Fallback
        }
    }

    private fun getPortugueseString(key: String): String = when (key) {
        // Login
        "login.student_ra" -> "RA do Aluno"
        "login.access_code" -> "Código de Acesso"
        "login.teacher_login" -> "Login de Professor"
        "login.parent_login" -> "Login de Responsável"
        "login.error" -> "Erro ao fazer login"
        "login.success" -> "Login realizado com sucesso"

        // Home
        "home.title" -> "Bem-vindo ao TakStud"
        "home.professor" -> "SOU PROFESSOR"
        "home.student" -> "SOU ALUNO/RESPONSÁVEL"

        // Tasks
        "tasks.title" -> "Tarefas e Testes"
        "tasks.add" -> "Adicionar Tarefa"
        "tasks.edit" -> "Editar Tarefa"
        "tasks.delete" -> "Deletar Tarefa"
        "tasks.empty" -> "Nenhuma tarefa registrada"

        // Notices
        "notices.title" -> "Avisos"
        "notices.add" -> "Adicionar Aviso"
        "notices.empty" -> "Nenhum aviso registrado"

        // Attendance
        "attendance.title" -> "Presença"
        "attendance.present" -> "Presente"
        "attendance.absent" -> "Ausente"

        // Grades
        "grades.title" -> "Notas"
        "grades.average" -> "Média"

        // Settings
        "settings.title" -> "Configurações"
        "settings.dark_mode" -> "Modo Escuro"
        "settings.language" -> "Idioma"
        "settings.notifications" -> "Notificações"

        else -> key // Fallback: retorna a chave se não encontrada
    }

    private fun getEnglishString(key: String): String = when (key) {
        // Login
        "login.student_ra" -> "Student RA"
        "login.access_code" -> "Access Code"
        "login.teacher_login" -> "Teacher Login"
        "login.parent_login" -> "Parent Login"
        "login.error" -> "Login error"
        "login.success" -> "Login successful"

        // Home
        "home.title" -> "Welcome to TakStud"
        "home.professor" -> "I AM A TEACHER"
        "home.student" -> "I AM A STUDENT/PARENT"

        // Tasks
        "tasks.title" -> "Tasks and Tests"
        "tasks.add" -> "Add Task"
        "tasks.edit" -> "Edit Task"
        "tasks.delete" -> "Delete Task"
        "tasks.empty" -> "No tasks registered"

        // Notices
        "notices.title" -> "Notices"
        "notices.add" -> "Add Notice"
        "notices.empty" -> "No notices registered"

        // Attendance
        "attendance.title" -> "Attendance"
        "attendance.present" -> "Present"
        "attendance.absent" -> "Absent"

        // Grades
        "grades.title" -> "Grades"
        "grades.average" -> "Average"

        // Settings
        "settings.title" -> "Settings"
        "settings.dark_mode" -> "Dark Mode"
        "settings.language" -> "Language"
        "settings.notifications" -> "Notifications"

        else -> key
    }

    private fun getSpanishString(key: String): String = when (key) {
        // Login
        "login.student_ra" -> "RA del Estudiante"
        "login.access_code" -> "Código de Acceso"
        "login.teacher_login" -> "Inicio de Sesión del Profesor"
        "login.parent_login" -> "Inicio de Sesión del Padre"
        "login.error" -> "Error al iniciar sesión"
        "login.success" -> "Inicio de sesión exitoso"

        // Home
        "home.title" -> "Bienvenido a TakStud"
        "home.professor" -> "SOY PROFESOR"
        "home.student" -> "SOY ESTUDIANTE/PADRE"

        // Tasks
        "tasks.title" -> "Tareas y Pruebas"
        "tasks.add" -> "Agregar Tarea"
        "tasks.edit" -> "Editar Tarea"
        "tasks.delete" -> "Eliminar Tarea"
        "tasks.empty" -> "Ninguna tarea registrada"

        // Notices
        "notices.title" -> "Avisos"
        "notices.add" -> "Agregar Aviso"
        "notices.empty" -> "Ningún aviso registrado"

        // Attendance
        "attendance.title" -> "Asistencia"
        "attendance.present" -> "Presente"
        "attendance.absent" -> "Ausente"

        // Grades
        "grades.title" -> "Calificaciones"
        "grades.average" -> "Promedio"

        // Settings
        "settings.title" -> "Configuración"
        "settings.dark_mode" -> "Modo Oscuro"
        "settings.language" -> "Idioma"
        "settings.notifications" -> "Notificaciones"

        else -> key
    }
}
