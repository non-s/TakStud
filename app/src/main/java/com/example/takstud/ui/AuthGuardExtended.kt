package com.example.takstud.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


import com.example.takstud.model.Role
import com.example.takstud.model.Student
import com.example.takstud.security.AccessValidator
import com.example.takstud.util.SessionManager

/**
 * Guard Composable para validar parent-student relationship.
 *
 * Verificação em duas camadas:
 * 1. Role PARENT verificado pelo RequireRole
 * 2. Parent ID corresponde ao student específico
 *
 * Exemplo:
 * ```kotlin
 * ParentAccessGuard(
 *     studentId = studentId,
 *     sessionManager = sessionManager,
 *     repository = repository,
 *     fallbackRoute = { navigationActions.navigateToHome() }
 * ) {
 *     ParentScreen(student = student, ...)
 * }
 * ```
 *
 * @param studentId ID do estudante sendo acessado
 * @param sessionManager Gerenciador de sessão do usuário
 * @param repository Repository para buscar dados
 * @param fallbackRoute Callback se acesso for negado
 * @param onAccessDenied Callback customizado para acesso negado
 * @param content Composable a renderizar se acesso permitido
 */
@Composable
fun ParentAccessGuard(
    studentId: String,
    sessionManager: SessionManager,
    fallbackRoute: () -> Unit,
    onAccessDenied: ((String) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val session = sessionManager.getSession()

    // 1. Verificação básica: user deve ser PARENT
    if (session.role != Role.PARENT) {
        Log.w("ParentAccessGuard", "ACESSO NEGADO: User role ${session.role} não é PARENT")
        LaunchedEffect(Unit) {
            fallbackRoute()
        }
        return
    }

    // 2. Verificação crítica: Parent deve ser responsável do student
    LaunchedEffect(studentId, session.userId) {
        try {
            // Verificar se o student ID da sessão corresponde ao studentId solicitado
            val isParent = session.parentStudentId == studentId

            if (!isParent) {
                Log.w(
                    "ParentAccessGuard",
                    "ACESSO NEGADO: Parent ${session.userId} não é responsável de $studentId"
                )

                // Log auditoria
                AccessValidator.logAccessAttempt(
                    userId = session.userId,
                    userRole = session.role.name,
                    resourceType = "STUDENT",
                    resourceId = studentId,
                    granted = false
                )

                onAccessDenied?.invoke("Acesso negado a este estudante")
                fallbackRoute()
            } else {
                Log.i(
                    "ParentAccessGuard",
                    "✓ ACESSO PERMITIDO: Parent ${session.userId} is responsável de $studentId"
                )

                // Log auditoria (sucesso)
                AccessValidator.logAccessAttempt(
                    userId = session.userId,
                    userRole = session.role.name,
                    resourceType = "STUDENT",
                    resourceId = studentId,
                    granted = true
                )
            }
        } catch (e: Exception) {
            Log.e("ParentAccessGuard", "Erro ao validar parent-student relationship", e)
            fallbackRoute()
        }
    }

    // Se chegou aqui, acesso foi concedido
    content()
}

/**
 * Guard Composable para validar teacher-class relationship.
 *
 * Verificação:
 * 1. Role TEACHER verificado pelo RequireRole
 * 2. Teacher leciona a turma específica
 *
 * Exemplo:
 * ```kotlin
 * TeacherAccessGuard(
 *     className = "6A",
 *     sessionManager = sessionManager,
 *     teacherClasses = listOf("6A", "6B"),
 *     fallbackRoute = { navigationActions.navigateToHome() }
 * ) {
 *     ManageGradesScreen(...)
 * }
 * ```
 *
 * @param className Nome da turma sendo acessada
 * @param sessionManager Gerenciador de sessão
 * @param teacherClasses Lista de turmas que o teacher leciona
 * @param fallbackRoute Callback se acesso negado
 * @param onAccessDenied Callback customizado
 * @param content Composable a renderizar
 */
@Composable
fun TeacherAccessGuard(
    className: String,
    sessionManager: SessionManager,
    teacherClasses: List<String>,
    fallbackRoute: () -> Unit,
    onAccessDenied: ((String) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val session = sessionManager.getSession()

    // 1. Verificação básica: user deve ser TEACHER
    if (session.role != Role.TEACHER) {
        Log.w("TeacherAccessGuard", "ACESSO NEGADO: User role ${session.role} não é TEACHER")
        LaunchedEffect(Unit) {
            fallbackRoute()
        }
        return
    }

    // 2. Verificação: Teacher deve lecionar esta turma
    val canAccess = className in teacherClasses

    if (!canAccess) {
        Log.w(
            "TeacherAccessGuard",
            "ACESSO NEGADO: Teacher ${session.userId} não leciona turma $className"
        )

        // Log auditoria
        AccessValidator.logAccessAttempt(
            userId = session.userId,
            userRole = session.role.name,
            resourceType = "CLASS",
            resourceId = className,
            granted = false
        )

        LaunchedEffect(Unit) {
            onAccessDenied?.invoke("Você não leciona esta turma")
            fallbackRoute()
        }
        return
    }

    Log.i(
        "TeacherAccessGuard",
        "✓ ACESSO PERMITIDO: Teacher ${session.userId} leciona turma $className"
    )

    // Log auditoria (sucesso)
    AccessValidator.logAccessAttempt(
        userId = session.userId,
        userRole = session.role.name,
        resourceType = "CLASS",
        resourceId = className,
        granted = true
    )

    // Acesso concedido
    content()
}

/**
 * Guard Composable para validar acesso a student por teacher.
 *
 * Verifica se teacher leciona a turma do student.
 *
 * @param student Estudante sendo acessado
 * @param teacherClasses Turmas que o teacher leciona
 * @param sessionManager Gerenciador de sessão
 * @param fallbackRoute Callback se acesso negado
 * @param content Composable a renderizar se permitido
 */
@Composable
fun TeacherStudentAccessGuard(
    student: Student,
    teacherClasses: List<String>,
    sessionManager: SessionManager,
    fallbackRoute: () -> Unit,
    content: @Composable () -> Unit
) {
    val session = sessionManager.getSession()

    // 1. Verificação básica: user deve ser TEACHER
    if (session.role != Role.TEACHER) {
        Log.w("TeacherStudentAccessGuard", "ACESSO NEGADO: User role ${session.role} não é TEACHER")
        LaunchedEffect(Unit) {
            fallbackRoute()
        }
        return
    }

    // 2. Verificação: Student deve estar em uma das turmas do teacher
    val canAccess = student.studentClass in teacherClasses

    if (!canAccess) {
        Log.w(
            "TeacherStudentAccessGuard",
            "ACESSO NEGADO: Teacher ${session.userId} não leciona turma ${student.studentClass}"
        )

        AccessValidator.logAccessAttempt(
            userId = session.userId,
            userRole = session.role.name,
            resourceType = "STUDENT",
            resourceId = student.id,
            granted = false
        )

        LaunchedEffect(Unit) {
            fallbackRoute()
        }
        return
    }

    Log.i(
        "TeacherStudentAccessGuard",
        "✓ ACESSO PERMITIDO: Teacher acessando student ${student.id}"
    )

    AccessValidator.logAccessAttempt(
        userId = session.userId,
        userRole = session.role.name,
        resourceType = "STUDENT",
        resourceId = student.id,
        granted = true
    )

    content()
}

/**
 * Guard Composable para validar acesso a uma task/grade por teacher.
 *
 * Verifica se teacher leciona a turma associada à task.
 *
 * @param taskClassName Turma associada à task
 * @param teacherClasses Turmas que o teacher leciona
 * @param sessionManager Gerenciador de sessão
 * @param fallbackRoute Callback se acesso negado
 * @param content Composable a renderizar se permitido
 */
@Composable
fun TeacherTaskAccessGuard(
    taskClassName: String,
    teacherClasses: List<String>,
    sessionManager: SessionManager,
    fallbackRoute: () -> Unit,
    content: @Composable () -> Unit
) {
    val session = sessionManager.getSession()

    // 1. Verificação básica
    if (session.role != Role.TEACHER) {
        LaunchedEffect(Unit) { fallbackRoute() }
        return
    }

    // 2. Verificação: Turma da task deve estar nas turmas do teacher
    val canAccess = taskClassName in teacherClasses

    if (!canAccess) {
        Log.w(
            "TeacherTaskAccessGuard",
            "ACESSO NEGADO: Teacher ${session.userId} não leciona turma $taskClassName"
        )
        LaunchedEffect(Unit) { fallbackRoute() }
        return
    }

    Log.i("TeacherTaskAccessGuard", "✓ ACESSO PERMITIDO: Teacher acessando task da turma $taskClassName")

    content()
}

/**
 * Extensão para validação simples de access logs.
 *
 * Imprime relatório dos últimos acessos no Logcat.
 */
fun printAccessAuditReport(
    userId: String? = null,
    grantedOnly: Boolean = false,
    deniedOnly: Boolean = false
) {
    val logs = AccessValidator.getAuditLogs(userId, grantedOnly, deniedOnly)

    Log.i("AccessAudit", "=== AUDIT REPORT ===")
    Log.i("AccessAudit", "Total entries: ${logs.size}")

    logs.forEach { log ->
        val status = if (log.granted) "✓ GRANTED" else "✗ DENIED"
        Log.i(
            "AccessAudit",
            "$status | ${log.userRole}(${log.userId}) -> ${log.resourceType}(${log.resourceId})"
        )
    }

    Log.i("AccessAudit", "===================")
}

/**
 * Limpa logs antigos de auditoria.
 *
 * @param keepLastNHours Quantas horas de logs manter
 */
fun cleanupOldAuditLogs(keepLastNHours: Int = 24) {
    AccessValidator.clearOldAuditLogs(keepLastNHours)
    Log.i("AccessAudit", "Cleared audit logs older than $keepLastNHours hours")
}
