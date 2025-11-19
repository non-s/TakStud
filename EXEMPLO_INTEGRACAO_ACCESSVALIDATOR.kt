package com.example.takstud

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import com.example.takstud.model.Role
import com.example.takstud.security.AccessValidator
import com.example.takstud.ui.RequireRole
import com.example.takstud.ui.parent.ParentScreen

/**
 * EXEMPLO DE INTEGRAÇÃO: Como usar AccessValidator em MainActivity
 *
 * Este arquivo mostra como implementar validação de acesso parent-student
 * de forma segura.
 *
 * COPIE ESTE CÓDIGO para MainActivity.kt na rota PARENT_ROUTE
 */

// ============== VERSÃO ANTES (SEM VALIDAÇÃO) ==============

@Deprecated("Sem validação de acesso - segurança fraca")
fun insecureParentRoute(
    navController: NavController,
    viewModel: TakStudViewModel
) {
    // ❌ PROBLEMA: Qualquer parent consegue acessar qualquer student!
    // Se parent sabe o studentId, consegue acessar dados de outro parent

    /*
    composable("${TakStudDestinations.PARENT_ROUTE}/{studentId}") { backStackEntry ->
        RequireRole(role = Role.PARENT, fallbackRoute = { navigationActions.navigateToHome() }) {
            val studentId = backStackEntry.arguments?.getString("studentId")
            val student = viewModel.students.collectAsState().value.find { it.id == studentId }

            if (student != null) {
                // ❌ NÃO VALIDA SE PARENT PODE ACESSAR ESTE STUDENT!
                ParentScreen(
                    student = student,
                    // ... parâmetros
                )
            }
        }
    }
    */
}

// ============== VERSÃO DEPOIS (COM VALIDAÇÃO) ==============

/**
 * Rota segura com validação de relacionamento parent-student.
 *
 * Integração passo a passo:
 * 1. Obter sessionManager
 * 2. Obter session ativa (parent ID)
 * 3. Extrair studentId dos parâmetros
 * 4. Obter student do banco
 * 5. VALIDAR se parent pode acessar
 * 6. Se não pode, redirecionar para home
 * 7. Se pode, mostrar tela
 */
fun secureParentRoute(
    navController: NavController,
    viewModel: TakStudViewModel,
    sessionManager: SecureSessionManager,
    repository: TakStudRepository
) {
    /*
    composable("${TakStudDestinations.PARENT_ROUTE}/{studentId}") { backStackEntry ->
        RequireRole(role = Role.PARENT, fallbackRoute = { navigationActions.navigateToHome() }) {

            // PASSO 1: Obter sessão ativa
            val currentSession = sessionManager.getActiveSession()
            val currentParentId = currentSession?.userId ?: run {
                navigationActions.navigateToHome()
                return@composable
            }

            // PASSO 2: Extrair studentId dos parâmetros
            val studentId = backStackEntry.arguments?.getString("studentId") ?: run {
                Log.w("MainActivity", "StudentId não fornecido")
                navigationActions.navigateToHome()
                return@composable
            }

            // PASSO 3: Obter student
            val student = viewModel.students.collectAsState().value.find { it.id == studentId }

            // PASSO 4: VALIDAÇÃO CRÍTICA DE SEGURANÇA
            if (student == null) {
                Log.w("MainActivity", "Student não encontrado: $studentId")
                navigationActions.navigateToHome()
                return@composable
            }

            // PASSO 5: Verificar relacionamento parent-student
            // Opção A: Usando campo "parent" do student (simples)
            val isParent = student.parent == currentParentId

            // Opção B: Usando AccessValidator (mais robusto)
            val relationship = AccessValidator.ParentStudentRelationship(
                parentId = currentParentId,
                studentIds = listOf(studentId)
            )
            val isAuthorized = AccessValidator.canParentAccessStudent(
                currentParentId = currentParentId,
                studentId = studentId,
                student = student,
                relationship = relationship
            )

            // PASSO 6: Se não autorizado, redirecionar
            if (!isAuthorized) {
                Log.e("MainActivity",
                    "SEGURANÇA: Parent $currentParentId tentando acessar student $studentId")

                // Registrar tentativa de acesso não autorizado (auditoria)
                viewModel.viewModelScope.launch {
                    repository.logAccessAudit(
                        userId = currentParentId,
                        userRole = Role.PARENT.name,
                        resourceType = "STUDENT",
                        resourceId = studentId,
                        granted = false
                    )
                }

                // Redirecionar para home
                navigationActions.navigateToHome()
                return@composable
            }

            // PASSO 7: Acesso autorizado - mostrar tela
            ParentScreen(
                student = student,
                tasks = viewModel.getTasksForStudent(student).collectAsState().value,
                notices = viewModel.getNoticesForStudent(student).collectAsState().value,
                schedules = viewModel.getSchedulesForStudent(student).collectAsState().value,
                grades = viewModel.getGradesForStudent(student).collectAsState().value,
                attendance = viewModel.getAttendanceForStudent(student).collectAsState().value,
                onLogout = { navigationActions.navigateToHome() },
                onScheduleClick = { schedule -> navController.navigate(
                    "${TakStudDestinations.SCHEDULE_DETAILS_ROUTE}/${schedule.id}/${student.id}"
                ) }
            )
        }
    }
    */
}

// ============== VERSÃO COM MÚLTIPLOS STUDENTS (Para listar filhos) ==============

/**
 * Rota que lista todos os students (filhos) de um parent.
 *
 * Implementa filtro de segurança para mostrar apenas filhos autorizados.
 */
fun secureStudentListRoute(
    viewModel: TakStudViewModel,
    sessionManager: SecureSessionManager,
    repository: TakStudRepository
) {
    /*
    composable(TakStudDestinations.STUDENT_LIST_ROUTE) { backStackEntry ->
        RequireRole(role = Role.PARENT, fallbackRoute = { navigationActions.navigateToHome() }) {

            // Obter parent ID da sessão
            val currentSession = sessionManager.getActiveSession()
            val currentParentId = currentSession?.userId ?: run {
                navigationActions.navigateToHome()
                return@composable
            }

            // Obter students apenas deste parent
            val studentsList = viewModel.students.collectAsState().value
                .filter { it.parent == currentParentId }

            // Mostrar lista de filhos
            StudentListForParent(
                students = studentsList,
                onStudentClick = { student ->
                    navController.navigate(
                        "${TakStudDestinations.PARENT_ROUTE}/${student.id}"
                    )
                }
            )
        }
    }
    */
}

// ============== EXEMPLO: TEACHER ACESSANDO APENAS SUAS TURMAS ==============

/**
 * Similar para Teacher - pode acessar apenas suas turmas.
 */
fun secureTeacherClassRoute(
    viewModel: TakStudViewModel,
    sessionManager: SecureSessionManager
) {
    /*
    composable("${TakStudDestinations.MANAGE_STUDENTS_ROUTE}") { backStackEntry ->
        RequireRole(role = Role.TEACHER, fallbackRoute = { navigationActions.navigateToHome() }) {

            // Obter teacher ID
            val currentSession = sessionManager.getActiveSession()
            val currentTeacherId = currentSession?.userId ?: run {
                navigationActions.navigateToHome()
                return@composable
            }

            // Obter turmas que este teacher leciona
            // (Buscar no banco de dados ou meta do usuário)
            val teacherClasses = viewModel.classesByPeriod.collectAsState().value
                .flatMap { (_, classes) -> classes }
                // Filtrar apenas turmas do teacher (você precisa de uma fonte de verdade)
                // Ex: teacher tem campo "classAssignments": ["6A", "7B"]

            // Obter students das turmas do teacher
            val accessibleStudents = viewModel.students.collectAsState().value
                .filter { it.studentClass in teacherClasses }

            // Mostrar apenas students autorizados
            ManageStudentsScreen(
                students = accessibleStudents,
                // ... outros parâmetros
            )
        }
    }
    */
}

// ============== COMPARAÇÃO: BEFORE vs AFTER ==============

/**
 * ANTES (Inseguro):
 * ❌ Parent consegue acessar qualquer student
 * ❌ Sem auditoria de tentativas de acesso
 * ❌ Sem log de atividades suspeitas
 * ❌ Sem segregação de dados
 *
 * DEPOIS (Seguro):
 * ✅ Parent só acessa seus filhos
 * ✅ Teacher só gerencia suas turmas
 * ✅ Auditoria de todas tentativas
 * ✅ Log de atividades suspeitas
 * ✅ Segregação de dados implementada
 * ✅ Conformidade com LGPD/GDPR
 */

// ============== TESTES RECOMENDADOS ==============

/*
TESTE 1: Parent Acessando Seu Student
- Login como parent A
- Acessar student próprio (filho)
- ✓ Esperado: Acesso permitido

TESTE 2: Parent Acessando Student de Outro Parent
- Login como parent A
- Tentar acessar student de parent B (direto pela URL)
- ✗ Esperado: Acesso negado, redirecionar

TESTE 3: Teacher Acessando Apenas Suas Turmas
- Login como teacher X
- Tentar ver alunos de turma que NÃO leciona
- ✗ Esperado: Acesso negado

TESTE 4: Admin Acessando Tudo
- Login como admin
- Acessar qualquer student/turma
- ✓ Esperado: Acesso permitido

TESTE 5: Auditoria
- Fazer 3 tentativas de acesso não autorizado
- Verificar logs em Firestore
- ✓ Esperado: Todas registradas em access_audit_logs
*/
