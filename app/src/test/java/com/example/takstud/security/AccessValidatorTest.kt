package com.example.takstud.security

import com.example.takstud.model.Role
import com.example.takstud.model.Student
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testes de segurança para AccessValidator
 *
 * Valida que o controle de acesso está funcionando corretamente:
 * - Parents não conseguem acessar filhos de outros
 * - Teachers não conseguem acessar turmas que não lecionam
 * - Admins conseguem acessar tudo
 * - Audit logs são registrados corretamente
 */
class AccessValidatorTest {

    private lateinit var validator: AccessValidator
    private val parentId1 = "parent_001"
    private val parentId2 = "parent_002"
    private val studentId1 = "student_001"
    private val studentId2 = "student_002"
    private val studentId3 = "student_003"
    private val teacherId1 = "teacher_001"
    private val teacherId2 = "teacher_002"
    private val adminId = "admin_001"

    @Before
    fun setUp() {
        validator = AccessValidator
        // Limpar logs antes de cada teste
        validator.clearOldAuditLogs(0)
    }

    // ==================== PARENT ACCESS CONTROL TESTS ====================

    @Test
    fun `parent can access own student`() {
        // Arrange
        val student = Student(id = studentId1, ra = "001", name = "João", studentClass = "6A")
        val relationship = AccessValidator.ParentStudentRelationship(
            parentId = parentId1,
            studentIds = listOf(studentId1, studentId2)
        )

        // Act
        val result = validator.canParentAccessStudent(
            currentParentId = parentId1,
            studentId = studentId1,
            student = student,
            relationship = relationship
        )

        // Assert
        assertTrue(result, "Parent deveria conseguir acessar seu próprio filho")
    }

    @Test
    fun `parent cannot access another parent's student`() {
        // Arrange
        val student = Student(id = studentId1, ra = "001", name = "João", studentClass = "6A")
        val relationship = AccessValidator.ParentStudentRelationship(
            parentId = parentId1,
            studentIds = listOf(studentId1, studentId2)
        )

        // Act - Parent 2 tentando acessar estudante de Parent 1
        val result = validator.canParentAccessStudent(
            currentParentId = parentId2,
            studentId = studentId1,
            student = student,
            relationship = relationship
        )

        // Assert
        assertFalse(result, "Parent NÃO deveria conseguir acessar filho de outro parent")
    }

    @Test
    fun `parent cannot access student if relationship is null`() {
        // Arrange
        val student = Student(id = studentId1, ra = "001", name = "João", studentClass = "6A")

        // Act
        val result = validator.canParentAccessStudent(
            currentParentId = parentId1,
            studentId = studentId1,
            student = student,
            relationship = null
        )

        // Assert
        assertFalse(result, "Parent NÃO deveria acessar sem relacionamento definido")
    }

    @Test
    fun `parent cannot access non-existent student`() {
        // Arrange
        val relationship = AccessValidator.ParentStudentRelationship(
            parentId = parentId1,
            studentIds = listOf(studentId1, studentId2)
        )

        // Act
        val result = validator.canParentAccessStudent(
            currentParentId = parentId1,
            studentId = studentId3,
            student = null,  // Student não existe
            relationship = relationship
        )

        // Assert
        assertFalse(result, "Parent NÃO deveria acessar estudante inexistente")
    }

    @Test
    fun `get accessible students for parent returns correct list`() {
        // Arrange
        val allStudents = listOf(
            Student(id = studentId1, ra = "001", name = "João", studentClass = "6A"),
            Student(id = studentId2, ra = "002", name = "Maria", studentClass = "6A"),
            Student(id = studentId3, ra = "003", name = "Pedro", studentClass = "6B")
        )
        val relationships = listOf(
            AccessValidator.ParentStudentRelationship(
                parentId = parentId1,
                studentIds = listOf(studentId1, studentId2)
            ),
            AccessValidator.ParentStudentRelationship(
                parentId = parentId2,
                studentIds = listOf(studentId3)
            )
        )

        // Act
        val accessibleStudents = validator.getAccessibleStudentsForParent(
            currentParentId = parentId1,
            allStudents = allStudents,
            relationships = relationships
        )

        // Assert
        assertTrue(accessibleStudents.size == 2, "Parent 1 deveria ter acesso a 2 estudantes")
        assertTrue(accessibleStudents.any { it.id == studentId1 }, "Deveria conter estudante 1")
        assertTrue(accessibleStudents.any { it.id == studentId2 }, "Deveria conter estudante 2")
        assertFalse(accessibleStudents.any { it.id == studentId3 }, "NÃO deveria conter estudante 3")
    }

    // ==================== TEACHER ACCESS CONTROL TESTS ====================

    @Test
    fun `teacher can access own class`() {
        // Arrange
        val relationship = AccessValidator.TeacherClassRelationship(
            teacherId = teacherId1,
            classNames = listOf("6A", "6B", "7A")
        )

        // Act
        val result = validator.canTeacherAccessClass(
            currentTeacherId = teacherId1,
            className = "6A",
            relationship = relationship
        )

        // Assert
        assertTrue(result, "Teacher deveria conseguir acessar sua turma")
    }

    @Test
    fun `teacher cannot access class they don't teach`() {
        // Arrange
        val relationship = AccessValidator.TeacherClassRelationship(
            teacherId = teacherId1,
            classNames = listOf("6A", "6B")
        )

        // Act
        val result = validator.canTeacherAccessClass(
            currentTeacherId = teacherId1,
            className = "7A",  // Turma que não leciona
            relationship = relationship
        )

        // Assert
        assertFalse(result, "Teacher NÃO deveria acessar turma que não leciona")
    }

    @Test
    fun `teacher cannot access another teacher's class`() {
        // Arrange
        val relationship = AccessValidator.TeacherClassRelationship(
            teacherId = teacherId1,
            classNames = listOf("6A", "6B")
        )

        // Act - Teacher 2 tentando acessar turma de Teacher 1
        val result = validator.canTeacherAccessClass(
            currentTeacherId = teacherId2,
            className = "6A",
            relationship = relationship
        )

        // Assert
        assertFalse(result, "Teacher NÃO deveria acessar turma de outro professor")
    }

    @Test
    fun `get accessible students for teacher filters by class`() {
        // Arrange
        val allStudents = listOf(
            Student(id = studentId1, ra = "001", name = "João", studentClass = "6A"),
            Student(id = studentId2, ra = "002", name = "Maria", studentClass = "6A"),
            Student(id = studentId3, ra = "003", name = "Pedro", studentClass = "7A")
        )
        val teacherClasses = listOf("6A", "6B")

        // Act
        val accessibleStudents = validator.getAccessibleStudentsForTeacher(
            currentTeacherId = teacherId1,
            allStudents = allStudents,
            teacherClasses = teacherClasses
        )

        // Assert
        assertTrue(accessibleStudents.size == 2, "Teacher deveria ter acesso a 2 estudantes")
        assertTrue(accessibleStudents.all { it.studentClass in teacherClasses },
            "Todos os estudantes devem ser da turma do professor"
        )
    }

    // ==================== ADMIN ACCESS CONTROL TESTS ====================

    @Test
    fun `admin can access anything`() {
        // Act
        val result = validator.canAdminAccessAnything(adminId)

        // Assert
        assertTrue(result, "Admin deveria conseguir acessar tudo")
    }

    // ==================== ROLE-BASED ACCESS TESTS ====================

    @Test
    fun `validate access for admin grants full access`() {
        // Act
        val result = validator.validateAccess(
            currentUserId = adminId,
            userRole = "ADMIN",
            resourceId = "any_resource",
            resourceType = "ANY_TYPE"
        )

        // Assert
        assertTrue(result, "Admin deveria ter acesso a qualquer recurso")
    }

    @Test
    fun `validate access for parent restricts to student only`() {
        // Arrange
        val metadata = mapOf(
            "parentStudentRelationship" to AccessValidator.ParentStudentRelationship(
                parentId = parentId1,
                studentIds = listOf(studentId1)
            ),
            "student" to Student(id = studentId1, ra = "001", name = "João", studentClass = "6A")
        )

        // Act
        val result = validator.validateAccess(
            currentUserId = parentId1,
            userRole = "PARENT",
            resourceId = studentId1,
            resourceType = "STUDENT",
            metadata = metadata
        )

        // Assert
        assertTrue(result, "Parent deveria acessar seu student")
    }

    @Test
    fun `validate access for parent denies non-student resources`() {
        // Act
        val result = validator.validateAccess(
            currentUserId = parentId1,
            userRole = "PARENT",
            resourceId = "any_class",
            resourceType = "CLASS"
        )

        // Assert
        assertFalse(result, "Parent NÃO deveria acessar CLASS")
    }

    @Test
    fun `validate access for teacher restricts to own classes`() {
        // Arrange
        val metadata = mapOf(
            "teacherClassRelationship" to AccessValidator.TeacherClassRelationship(
                teacherId = teacherId1,
                classNames = listOf("6A")
            )
        )

        // Act
        val result = validator.validateAccess(
            currentUserId = teacherId1,
            userRole = "TEACHER",
            resourceId = "6A",
            resourceType = "CLASS",
            metadata = metadata
        )

        // Assert
        assertTrue(result, "Teacher deveria acessar sua turma")
    }

    // ==================== AUDIT LOGGING TESTS ====================

    @Test
    fun `audit log captures access grant`() {
        // Act
        validator.logAccessAttempt(
            userId = parentId1,
            userRole = "PARENT",
            resourceType = "STUDENT",
            resourceId = studentId1,
            granted = true
        )

        // Assert
        val logs = validator.getAuditLogs(userId = parentId1)
        assertTrue(logs.size == 1, "Deveria ter 1 log de auditoria")
        assertTrue(logs[0].granted, "Log deveria indicar acesso concedido")
    }

    @Test
    fun `audit log captures access denial`() {
        // Act
        validator.logAccessAttempt(
            userId = parentId2,
            userRole = "PARENT",
            resourceType = "STUDENT",
            resourceId = studentId1,
            granted = false
        )

        // Assert
        val logs = validator.getAuditLogs(userId = parentId2)
        assertTrue(logs.size == 1, "Deveria ter 1 log de auditoria")
        assertFalse(logs[0].granted, "Log deveria indicar acesso negado")
    }

    @Test
    fun `filter audit logs by grant status`() {
        // Arrange
        validator.logAccessAttempt(parentId1, "PARENT", "STUDENT", studentId1, true)
        validator.logAccessAttempt(parentId1, "PARENT", "STUDENT", studentId3, false)
        validator.logAccessAttempt(parentId1, "PARENT", "STUDENT", studentId2, true)

        // Act
        val grantedLogs = validator.getAuditLogs(grantedOnly = true)
        val deniedLogs = validator.getAuditLogs(deniedOnly = true)

        // Assert
        assertTrue(grantedLogs.size == 2, "Deveria ter 2 acessos concedidos")
        assertTrue(deniedLogs.size == 1, "Deveria ter 1 acesso negado")
    }

    @Test
    fun `clear old audit logs removes old entries`() {
        // Arrange
        validator.logAccessAttempt(parentId1, "PARENT", "STUDENT", studentId1, true)

        // Act
        val logsBeforeClear = validator.getAuditLogs()
        validator.clearOldAuditLogs(keepLastNHours = 0)  // Limpar tudo
        val logsAfterClear = validator.getAuditLogs()

        // Assert
        assertTrue(logsBeforeClear.isNotEmpty(), "Deveria ter logs antes de limpar")
        assertTrue(logsAfterClear.isEmpty(), "Deveria estar vazio após limpar")
    }

    @Test
    fun `audit log contains correct metadata`() {
        // Act
        validator.logAccessAttempt(
            userId = teacherId1,
            userRole = "TEACHER",
            resourceType = "CLASS",
            resourceId = "6A",
            granted = true
        )

        // Assert
        val logs = validator.getAuditLogs(userId = teacherId1)
        val log = logs[0]

        assertTrue(log.userId == teacherId1, "User ID deve corresponder")
        assertTrue(log.userRole == "TEACHER", "Role deve corresponder")
        assertTrue(log.resourceType == "CLASS", "Resource type deve corresponder")
        assertTrue(log.resourceId == "6A", "Resource ID deve corresponder")
        assertTrue(log.granted, "Grant status deve corresponder")
        assertTrue(log.timestamp > 0, "Timestamp deve ser válido")
    }
}
