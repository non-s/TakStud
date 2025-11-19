package com.example.takstud.security

import android.util.Log
import com.example.takstud.model.Role
import com.example.takstud.model.Student

/**
 * Validador de acesso baseado em relacionamentos e roles.
 *
 * Garante que:
 * - Parents só conseguem acessar seus próprios filhos
 * - Teachers só conseguem acessar alunos de suas turmas
 * - Admins conseguem acessar tudo
 *
 * Implementa o princípio de controle de acesso de dados (Data Access Control).
 */
object AccessValidator {

    private const val TAG = "AccessValidator"

    /**
     * Validação de acesso - Mapeamento de parent para student.
     *
     * Padrão esperado:
     * - Parent pode ter 1+ estudantes filhos
     * - Student pertence a 1 parent
     */
    data class ParentStudentRelationship(
        val parentId: String,
        val studentIds: List<String> // UUIDs dos filhos
    )

    /**
     * Validação de acesso - Mapeamento de teacher para class.
     *
     * Padrão esperado:
     * - Teacher pode lecionar 1+ turmas
     * - Turma tem 1 teacher responsável (ou múltiplos)
     */
    data class TeacherClassRelationship(
        val teacherId: String,
        val classNames: List<String> // Ex: ["6A", "7B", "8A"]
    )

    // ============== PARENT ACCESS CONTROL ==============

    /**
     * Verifica se um parent pode acessar um student específico.
     *
     * CRÍTICO PARA SEGURANÇA: Impede que parents acessem filhos de outros parents.
     *
     * @param currentSession Sessão do usuário logado (parent)
     * @param studentId ID do student que está tentando acessar
     * @param studentStudent Objeto do student (para verificação extra)
     * @param relationship Mapeamento parent-student (do banco de dados)
     * @return true se parent pode acessar, false caso contrário
     */
    fun canParentAccessStudent(
        currentParentId: String,
        studentId: String,
        student: Student?,
        relationship: ParentStudentRelationship? = null
    ): Boolean {
        // Validação 1: Student deve existir
        if (student == null) {
            Log.w(TAG, "Student não encontrado: $studentId")
            return false
        }

        // Validação 2: Verificar relacionamento parent-student
        val canAccess = relationship?.parentId == currentParentId &&
                       studentId in relationship.studentIds

        if (!canAccess) {
            Log.w(
                TAG,
                "ACESSO NEGADO: Parent $currentParentId tentando acessar student $studentId"
            )
        } else {
            Log.i(TAG, "✓ Parent $currentParentId autorizado acessar student $studentId")
        }

        return canAccess
    }

    /**
     * Obter todos os students que um parent pode acessar.
     *
     * Útil para filtrar listas.
     *
     * @param currentParentId ID do parent
     * @param allStudents Todos os students do sistema
     * @param relationships Mapeamento de relacionamentos
     * @return Lista filtrada apenas de students que o parent pode acessar
     */
    fun getAccessibleStudentsForParent(
        currentParentId: String,
        allStudents: List<Student>,
        relationships: List<ParentStudentRelationship>
    ): List<Student> {
        val relationship = relationships.find { it.parentId == currentParentId }
            ?: return emptyList()

        return allStudents.filter { it.id in relationship.studentIds }
    }

    // ============== TEACHER ACCESS CONTROL ==============

    /**
     * Verifica se um teacher pode acessar/gerenciar uma turma específica.
     *
     * IMPORTANTE: Teacher deve estar associado à turma.
     *
     * @param currentTeacherId ID do teacher logado
     * @param className Nome da turma (ex: "6A", "7B")
     * @param relationship Mapeamento teacher-class
     * @return true se teacher pode acessar, false caso contrário
     */
    fun canTeacherAccessClass(
        currentTeacherId: String,
        className: String,
        relationship: TeacherClassRelationship? = null
    ): Boolean {
        val canAccess = relationship?.teacherId == currentTeacherId &&
                       className in relationship.classNames

        if (!canAccess) {
            Log.w(
                TAG,
                "ACESSO NEGADO: Teacher $currentTeacherId tentando acessar turma $className"
            )
        } else {
            Log.i(TAG, "✓ Teacher $currentTeacherId autorizado acessar turma $className")
        }

        return canAccess
    }

    /**
     * Filtrar students para mostrar apenas aqueles da turma do teacher.
     *
     * @param currentTeacherId ID do teacher
     * @param allStudents Todos os students
     * @param teacherClasses Turmas que o teacher leciona
     * @return Students apenas das turmas do teacher
     */
    fun getAccessibleStudentsForTeacher(
        currentTeacherId: String,
        allStudents: List<Student>,
        teacherClasses: List<String>
    ): List<Student> {
        return allStudents.filter { it.studentClass in teacherClasses }
    }

    // ============== ADMIN ACCESS CONTROL ==============

    /**
     * Admin consegue acessar tudo.
     *
     * @return sempre true
     */
    fun canAdminAccessAnything(currentAdminId: String): Boolean {
        Log.i(TAG, "✓ Admin $currentAdminId tem acesso total")
        return true
    }

    // ============== ROLE-BASED ACCESS ==============

    /**
     * Verificação centralizada de acesso por role.
     *
     * @param currentUserId ID do usuário logado
     * @param userRole Role do usuário (PARENT, TEACHER, ADMIN)
     * @param resourceId ID do recurso que está tentando acessar
     * @param resourceType Tipo do recurso (STUDENT, CLASS, TASK, etc)
     * @param metadata Dados adicionais para validação (relationships, assignments, etc)
     * @return true se acesso é permitido
     */
    fun validateAccess(
        currentUserId: String,
        userRole: String,
        resourceId: String,
        resourceType: String,
        metadata: Map<String, Any> = emptyMap()
    ): Boolean {
        return when (userRole.uppercase()) {
            "ADMIN" -> {
                Log.i(TAG, "✓ Admin $currentUserId acesso permitido para $resourceType:$resourceId")
                true
            }

            "PARENT" -> {
                // Parent pode acessar seu próprio student
                when (resourceType.uppercase()) {
                    "STUDENT" -> {
                        val relationship = metadata["parentStudentRelationship"] as? ParentStudentRelationship
                        canParentAccessStudent(currentUserId, resourceId, metadata["student"] as? Student, relationship)
                    }
                    else -> {
                        Log.w(TAG, "Parent tentando acessar $resourceType (não permitido)")
                        false
                    }
                }
            }

            "TEACHER" -> {
                // Teacher pode acessar sua turma
                when (resourceType.uppercase()) {
                    "CLASS" -> {
                        val relationship = metadata["teacherClassRelationship"] as? TeacherClassRelationship
                        canTeacherAccessClass(currentUserId, resourceId, relationship)
                    }
                    "STUDENT" -> {
                        // Teacher pode acessar student se estiver na sua turma
                        val student = metadata["student"] as? Student
                        val teacherClasses = metadata["teacherClasses"] as? List<String> ?: emptyList()
                        student != null && student.studentClass in teacherClasses
                    }
                    else -> {
                        Log.w(TAG, "Teacher tentando acessar $resourceType")
                        false
                    }
                }
            }

            else -> {
                Log.w(TAG, "Role desconhecida: $userRole")
                false
            }
        }
    }

    // ============== AUDIT LOGGING ==============

    /**
     * Registra tentativa de acesso (bem-sucedida ou falha).
     *
     * Útil para auditoria e detecção de ataques.
     */
    data class AccessAuditLog(
        val userId: String,
        val userRole: String,
        val resourceType: String,
        val resourceId: String,
        val granted: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val auditLogs = mutableListOf<AccessAuditLog>()

    /**
     * Adiciona entrada ao log de auditoria.
     */
    fun logAccessAttempt(
        userId: String,
        userRole: String,
        resourceType: String,
        resourceId: String,
        granted: Boolean
    ) {
        val log = AccessAuditLog(
            userId = userId,
            userRole = userRole,
            resourceType = resourceType,
            resourceId = resourceId,
            granted = granted
        )
        auditLogs.add(log)

        // Log também no Logcat para debug
        val status = if (granted) "✓ GRANTED" else "✗ DENIED"
        Log.i(TAG, "$status: $userRole($userId) -> $resourceType($resourceId)")
    }

    /**
     * Retorna histórico de acessos.
     *
     * Útil para investigar atividades suspeitas.
     */
    fun getAuditLogs(
        userId: String? = null,
        grantedOnly: Boolean = false,
        deniedOnly: Boolean = false
    ): List<AccessAuditLog> {
        var filtered = auditLogs.asSequence()

        if (userId != null) {
            filtered = filtered.filter { it.userId == userId }
        }

        if (grantedOnly) {
            filtered = filtered.filter { it.granted }
        }

        if (deniedOnly) {
            filtered = filtered.filter { !it.granted }
        }

        return filtered.toList()
    }

    /**
     * Limpar logs (chamar periodicamente em produção).
     */
    fun clearOldAuditLogs(keepLastNHours: Int = 24) {
        val cutoffTime = System.currentTimeMillis() - (keepLastNHours * 60 * 60 * 1000)
        auditLogs.removeIf { it.timestamp < cutoffTime }
    }
}
