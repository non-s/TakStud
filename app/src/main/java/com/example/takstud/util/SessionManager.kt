package com.example.takstud.util

import androidx.compose.runtime.mutableStateOf
import com.example.takstud.model.Permission
import com.example.takstud.model.Role
import com.example.takstud.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global session manager for tracking the current logged-in user
 * Implements singleton pattern to ensure only one instance exists
 */
object SessionManager {
    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean = _currentSession.value != null

    /**
     * Get current session or throw if not authenticated
     */
    fun getSession(): UserSession {
        return _currentSession.value ?: throw IllegalStateException("No active session")
    }

    /**
     * Create a new session for a parent user
     */
    fun createParentSession(studentId: String, studentRa: String, studentName: String) {
        val session = UserSession(
            userId = studentId,
            role = Role.PARENT,
            displayName = studentName,
            parentStudentId = studentId
        )
        _currentSession.value = session
    }

    /**
     * Create a new session for a teacher user
     */
    fun createTeacherSession(teacherId: String = "teacher_001") {
        val session = UserSession(
            userId = teacherId,
            role = Role.TEACHER,
            displayName = "Professor",
            parentStudentId = null
        )
        _currentSession.value = session
    }

    /**
     * Check if current user has a specific permission
     */
    fun hasPermission(permission: Permission): Boolean {
        return _currentSession.value?.hasPermission(permission) ?: false
    }

    /**
     * Check if current user is a parent
     */
    fun isParent(): Boolean = _currentSession.value?.isParent() ?: false

    /**
     * Check if current user is a teacher
     */
    fun isTeacher(): Boolean = _currentSession.value?.isTeacher() ?: false

    /**
     * Get current user's role
     */
    fun getCurrentRole(): Role? = _currentSession.value?.role

    /**
     * Get current user's display name
     */
    fun getCurrentUserName(): String? = _currentSession.value?.displayName

    /**
     * Get parent's student ID (only for parent users)
     */
    fun getParentStudentId(): String? = _currentSession.value?.parentStudentId

    /**
     * Logout current user and clear session
     */
    fun logout() {
        _currentSession.value = null
    }

    /**
     * Clear all session data (used for app reset)
     */
    fun clearSession() {
        _currentSession.value = null
    }
}