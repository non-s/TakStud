package com.example.takstud.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * AnalyticsManager - Gerencia eventos e análise de uso da aplicação
 * Rastreia comportamento do usuário e métricas de engajamento
 */
object AnalyticsManager {

    private lateinit var analytics: FirebaseAnalytics

    /**
     * Inicializar analytics
     */
    fun initialize(context: Context) {
        analytics = Firebase.analytics
        analytics.setAnalyticsCollectionEnabled(true)
    }

    // ===== Eventos de Login =====

    fun logTeacherLogin(success: Boolean) {
        val bundle = android.os.Bundle().apply {
            putBoolean("success", success)
        }
        analytics.logEvent("teacher_login", bundle)
    }

    fun logParentLogin(success: Boolean) {
        val bundle = android.os.Bundle().apply {
            putBoolean("success", success)
        }
        analytics.logEvent("parent_login", bundle)
    }

    // ===== Eventos de Tarefas =====

    fun logTaskCreated() {
        analytics.logEvent("task_created", android.os.Bundle())
    }

    fun logTaskViewed(taskId: String) {
        val bundle = android.os.Bundle().apply {
            putString("task_id", taskId)
        }
        analytics.logEvent("task_viewed", bundle)
    }

    fun logTaskDeleted() {
        analytics.logEvent("task_deleted", android.os.Bundle())
    }

    fun logGradeAssigned(taskId: String, gradeCount: Int) {
        val bundle = android.os.Bundle().apply {
            putString("task_id", taskId)
            putInt("grade_count", gradeCount)
        }
        analytics.logEvent("grades_assigned", bundle)
    }

    // ===== Eventos de Avisos =====

    fun logNoticeCreated() {
        analytics.logEvent("notice_created", android.os.Bundle())
    }

    fun logNoticeViewed(noticeId: String) {
        val bundle = android.os.Bundle().apply {
            putString("notice_id", noticeId)
        }
        analytics.logEvent("notice_viewed", bundle)
    }

    fun logNoticeDeleted() {
        analytics.logEvent("notice_deleted", android.os.Bundle())
    }

    // ===== Eventos de Presença =====

    fun logAttendanceRecorded(studentClass: String, studentCount: Int) {
        val bundle = android.os.Bundle().apply {
            putString("class", studentClass)
            putInt("students_recorded", studentCount)
        }
        analytics.logEvent("attendance_recorded", bundle)
    }

    fun logAttendanceViewed(studentClass: String) {
        val bundle = android.os.Bundle().apply {
            putString("class", studentClass)
        }
        analytics.logEvent("attendance_viewed", bundle)
    }

    // ===== Eventos de Turma =====

    fun logScheduleCreated(studentClass: String) {
        val bundle = android.os.Bundle().apply {
            putString("class", studentClass)
        }
        analytics.logEvent("schedule_created", bundle)
    }

    fun logStudentAdded(studentClass: String) {
        val bundle = android.os.Bundle().apply {
            putString("class", studentClass)
        }
        analytics.logEvent("student_added", bundle)
    }

    fun logStudentRemoved(studentClass: String) {
        val bundle = android.os.Bundle().apply {
            putString("class", studentClass)
        }
        analytics.logEvent("student_removed", bundle)
    }

    // ===== Eventos de Preferências =====

    fun logDarkModeToggled(enabled: Boolean) {
        val bundle = android.os.Bundle().apply {
            putBoolean("dark_mode_enabled", enabled)
        }
        analytics.logEvent("dark_mode_toggled", bundle)
    }

    fun logLanguageChanged(language: String) {
        val bundle = android.os.Bundle().apply {
            putString("language", language)
        }
        analytics.logEvent("language_changed", bundle)
    }

    fun logNotificationsToggled(enabled: Boolean) {
        val bundle = android.os.Bundle().apply {
            putBoolean("notifications_enabled", enabled)
        }
        analytics.logEvent("notifications_toggled", bundle)
    }

    // ===== Eventos de Erro =====

    fun logError(errorType: String, errorMessage: String) {
        val bundle = android.os.Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
        }
        analytics.logEvent("app_error", bundle)
    }

    fun logLoginError(errorType: String) {
        val bundle = android.os.Bundle().apply {
            putString("error_type", errorType)
        }
        analytics.logEvent("login_error", bundle)
    }

    // ===== Eventos de Engajamento =====

    fun logScreenView(screenName: String) {
        val bundle = android.os.Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logSessionStart() {
        analytics.logEvent("user_session_started", android.os.Bundle())
    }

    fun logSessionEnd() {
        analytics.logEvent("user_session_ended", android.os.Bundle())
    }

    // ===== Propriedades do Usuário =====

    fun setUserRole(role: String) {
        analytics.setUserProperty("user_role", role)
    }

    fun setUserClass(studentClass: String) {
        analytics.setUserProperty("student_class", studentClass)
    }

    fun setAppVersion(version: String) {
        analytics.setUserProperty("app_version", version)
    }

    /**
     * Definir ID de usuário anônimo (sem PII)
     */
    fun setAnonymousUserId(userId: String) {
        analytics.setUserId(userId)
    }

    /**
     * Desabilitar coleta de analytics (para respeitar preferências do usuário)
     */
    fun disableAnalytics() {
        analytics.setAnalyticsCollectionEnabled(false)
    }

    /**
     * Habilitar coleta de analytics
     */
    fun enableAnalytics() {
        analytics.setAnalyticsCollectionEnabled(true)
    }
}
