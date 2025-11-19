package com.example.takstud.util

import com.example.takstud.model.Permission
import com.example.takstud.model.Role
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse as ktAssertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue as ktAssertTrue

/**
 * Unit tests for SessionManager
 * Tests session creation, role checks, and permission validation
 */
class SessionManagerTest {

    @Before
    fun setup() {
        // Clear any existing session
        SessionManager.clearSession()
    }

    @After
    fun tearDown() {
        // Clean up after each test
        SessionManager.clearSession()
    }

    @Test
    fun testSessionNotAuthenticatedInitially() {
        // Assert
        ktAssertFalse(SessionManager.isAuthenticated())
        assertNull(SessionManager.getCurrentRole())
    }

    @Test
    fun testCreateParentSession() {
        // Act
        SessionManager.createParentSession(
            studentId = "student_001",
            studentRa = "001",
            studentName = "João Silva"
        )

        // Assert
        ktAssertTrue(SessionManager.isAuthenticated())
        ktAssertTrue(SessionManager.isParent())
        ktAssertFalse(SessionManager.isTeacher())
        assertEquals(Role.PARENT, SessionManager.getCurrentRole())
        assertEquals("João Silva", SessionManager.getCurrentUserName())
        assertEquals("student_001", SessionManager.getParentStudentId())
    }

    @Test
    fun testCreateTeacherSession() {
        // Act
        SessionManager.createTeacherSession()

        // Assert
        ktAssertTrue(SessionManager.isAuthenticated())
        ktAssertTrue(SessionManager.isTeacher())
        ktAssertFalse(SessionManager.isParent())
        assertEquals(Role.TEACHER, SessionManager.getCurrentRole())
        assertEquals("Professor", SessionManager.getCurrentUserName())
        assertNull(SessionManager.getParentStudentId())
    }

    @Test
    fun testParentHasCorrectPermissions() {
        // Arrange
        SessionManager.createParentSession(
            studentId = "student_001",
            studentRa = "001",
            studentName = "João Silva"
        )

        // Assert - Parent should have VIEW permissions
        ktAssertTrue(SessionManager.hasPermission(Permission.VIEW_TASKS))
        ktAssertTrue(SessionManager.hasPermission(Permission.VIEW_GRADES))
        ktAssertTrue(SessionManager.hasPermission(Permission.VIEW_NOTICES))

        // Assert - Parent should NOT have MANAGE permissions
        ktAssertFalse(SessionManager.hasPermission(Permission.MANAGE_STUDENTS))
        ktAssertFalse(SessionManager.hasPermission(Permission.CREATE_TASK))
        ktAssertFalse(SessionManager.hasPermission(Permission.TAKE_ATTENDANCE))
    }

    @Test
    fun testTeacherHasCorrectPermissions() {
        // Arrange
        SessionManager.createTeacherSession()

        // Assert - Teacher should have MANAGE permissions
        ktAssertTrue(SessionManager.hasPermission(Permission.MANAGE_STUDENTS))
        ktAssertTrue(SessionManager.hasPermission(Permission.CREATE_TASK))
        ktAssertTrue(SessionManager.hasPermission(Permission.TAKE_ATTENDANCE))
        ktAssertTrue(SessionManager.hasPermission(Permission.MANAGE_GRADES))

        // Assert - Teacher should NOT have parent-specific permissions
        ktAssertFalse(SessionManager.hasPermission(Permission.VIEW_OWN_STUDENT))
    }

    @Test
    fun testLogout() {
        // Arrange
        SessionManager.createParentSession(
            studentId = "student_001",
            studentRa = "001",
            studentName = "João Silva"
        )
        ktAssertTrue(SessionManager.isAuthenticated())

        // Act
        SessionManager.logout()

        // Assert
        ktAssertFalse(SessionManager.isAuthenticated())
        assertNull(SessionManager.getCurrentRole())
        assertNull(SessionManager.getCurrentUserName())
    }

    @Test
    fun testClearSession() {
        // Arrange
        SessionManager.createTeacherSession()
        ktAssertTrue(SessionManager.isAuthenticated())

        // Act
        SessionManager.clearSession()

        // Assert
        ktAssertFalse(SessionManager.isAuthenticated())
        assertNull(SessionManager.getCurrentRole())
    }

    @Test
    fun testSessionSwitchingRoles() {
        // Arrange & Act - Create parent session
        SessionManager.createParentSession(
            studentId = "student_001",
            studentRa = "001",
            studentName = "João Silva"
        )
        ktAssertTrue(SessionManager.isParent())

        // Act - Create teacher session (switching roles)
        SessionManager.createTeacherSession()

        // Assert - Should now be teacher
        ktAssertTrue(SessionManager.isTeacher())
        ktAssertFalse(SessionManager.isParent())
        assertEquals(Role.TEACHER, SessionManager.getCurrentRole())
    }

    @Test
    fun testPermissionsAfterLogout() {
        // Arrange
        SessionManager.createParentSession(
            studentId = "student_001",
            studentRa = "001",
            studentName = "João Silva"
        )
        ktAssertTrue(SessionManager.hasPermission(Permission.VIEW_TASKS))

        // Act
        SessionManager.logout()

        // Assert - No permissions after logout
        ktAssertFalse(SessionManager.hasPermission(Permission.VIEW_TASKS))
        ktAssertFalse(SessionManager.hasPermission(Permission.MANAGE_STUDENTS))
    }
}