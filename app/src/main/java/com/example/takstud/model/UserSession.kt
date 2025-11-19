package com.example.takstud.model

/**
 * Represents the currently logged-in user session
 *
 * @param userId Unique identifier for the user
 * @param role User's role (PARENT or TEACHER)
 * @param displayName User's display name (name or RA)
 * @param parentStudentId For PARENT role: the ID of their student. Null for TEACHER
 * @param loginTime Timestamp when user logged in
 */
data class UserSession(
    val userId: String,
    val role: Role,
    val displayName: String,
    val parentStudentId: String? = null,
    val loginTime: Long = System.currentTimeMillis()
) {
    /**
     * Check if user has a specific permission
     */
    fun hasPermission(permission: Permission): Boolean {
        return Permission.getPermissionsForRole(role).contains(permission)
    }

    /**
     * Check if user is a parent
     */
    fun isParent(): Boolean = role == Role.PARENT

    /**
     * Check if user is a teacher
     */
    fun isTeacher(): Boolean = role == Role.TEACHER

    /**
     * Get session duration in milliseconds
     */
    fun getSessionDuration(): Long = System.currentTimeMillis() - loginTime
}