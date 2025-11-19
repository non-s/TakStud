package com.example.takstud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.takstud.model.Role
import com.example.takstud.ui.HomeScreen
import com.example.takstud.ui.RequireRole
import com.example.takstud.ui.login.AdminLoginScreen
import com.example.takstud.ui.login.LoginScreen
import com.example.takstud.ui.login.ParentLoginScreen
import com.example.takstud.ui.login.TeacherLoginScreen
import com.example.takstud.ui.parent.ParentScreen
import com.example.takstud.ui.teacher.*
import com.example.takstud.ui.theme.TakStudTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TakStudTheme {
                TakStudApp()
            }
        }
    }
}

@Composable
fun TakStudApp(viewModel: TakStudViewModel = viewModel()) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) { TakStudNavigationActions(navController) }

    NavHost(navController = navController, startDestination = TakStudDestinations.HOME_ROUTE) {
        // TELA INICIAL
        composable(TakStudDestinations.HOME_ROUTE) {
            HomeScreen(
                onProfessorClick = { navigationActions.navigateToTeacherLogin() },
                onAlunoClick = { navigationActions.navigateToParentLogin() }
            )
        }

        // TELA LOGIN PROFESSOR (NOVO)
        composable(TakStudDestinations.TEACHER_LOGIN_ROUTE) {
            TeacherLoginScreen(
                onLoginSuccess = { navigationActions.navigateToTeacher() },
                onBackClick = { navigationActions.onBack() }
            )
        }

        // TELA LOGIN RESPONSÁVEL (NOVO)
        composable(TakStudDestinations.PARENT_LOGIN_ROUTE) {
            ParentLoginScreen(
                onLoginSuccess = { studentId ->
                    navigationActions.navigateToParent(studentId)
                },
                onBackClick = { navigationActions.onBack() }
            )
        }

        // TELA LOGIN ANTIGO (mantém compatibilidade)
        composable(TakStudDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onTeacherLogin = { navigationActions.navigateToAdminLogin() },
                onParentLoginSuccess = {
                    // TODO: Navigate to parent screen with student ID from login result
                    navigationActions.navigateToLogin()
                }
            )
        }
        composable(TakStudDestinations.ADMIN_LOGIN_ROUTE) {
            AdminLoginScreen(
                onAdminLoginSuccess = { navigationActions.navigateToTeacher() }
            )
        }
        composable(TakStudDestinations.TEACHER_ROUTE) {
            RequireRole(role = Role.TEACHER, fallbackRoute = { navigationActions.navigateToHome() }) {
                TeacherScreen(
                    onManageTasks = { navController.navigate(TakStudDestinations.TASK_LIST_ROUTE) },
                    onManageNotices = { navController.navigate(TakStudDestinations.NOTICE_LIST_ROUTE) },
                    onManageSchedules = { navController.navigate(TakStudDestinations.SCHEDULES_LIST_ROUTE) },
                    onManageStudents = { navController.navigate(TakStudDestinations.MANAGE_STUDENTS_ROUTE) },
                    onManageAttendance = { navController.navigate(TakStudDestinations.ATTENDANCE_ROUTE) },
                    onLogout = { navigationActions.navigateToHome() }
                )
            }
        }
        composable("${TakStudDestinations.PARENT_ROUTE}/{studentId}") { backStackEntry ->
            RequireRole(role = Role.PARENT, fallbackRoute = { navigationActions.navigateToHome() }) {
                val studentId = backStackEntry.arguments?.getString("studentId")
                val student = viewModel.students.collectAsState().value.find { it.id == studentId }
                if (student != null) {
                    ParentScreen(
                        student = student,
                        tasks = viewModel.getTasksForStudent(student).collectAsState().value,
                        notices = viewModel.getNoticesForStudent(student).collectAsState().value,
                        schedules = viewModel.getSchedulesForStudent(student).collectAsState().value,
                        grades = viewModel.getGradesForStudent(student).collectAsState().value,
                        attendance = viewModel.getAttendanceForStudent(student).collectAsState().value,
                        onLogout = { navigationActions.navigateToHome() },
                        onScheduleClick = { schedule -> navController.navigate("${TakStudDestinations.SCHEDULE_DETAILS_ROUTE}/${schedule.id}/${student.id}") }
                    )
                }
            }
        }
        composable(TakStudDestinations.TASK_LIST_ROUTE) {
             TaskListScreen(
                tasks = viewModel.tasks.collectAsState().value,
                onAddTask = { navController.navigate("${TakStudDestinations.ADD_TASK_ROUTE}/null") },
                onTaskClick = { task -> navController.navigate("${TakStudDestinations.ADD_TASK_ROUTE}/${task.id}") },
                onDeleteTask = { task -> viewModel.deleteTask(task) },
                onManageGrades = { task -> navController.navigate("${TakStudDestinations.MANAGE_GRADES_ROUTE}/${task.id}") },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.ADD_TASK_ROUTE}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = if (taskId == "null") null else viewModel.tasks.collectAsState().value.find { it.id == taskId }
            AddTaskScreen(
                taskToEdit = task,
                schedules = viewModel.schedules.collectAsState().value,
                onSave = { t -> viewModel.saveTask(t) { navController.popBackStack() } },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.MANAGE_GRADES_ROUTE}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = viewModel.tasks.collectAsState().value.find { it.id == taskId }
            if (task != null) {
                 ManageGradesScreen(
                    task = task,
                    students = viewModel.getStudentsForClass(task.studentClass).collectAsState().value,
                    grades = viewModel.getGradesForTask(task.id).collectAsState().value,
                    onSaveGrade = { grade -> viewModel.saveGrade(grade) },
                    onBack = { navigationActions.onBack() }
                )
            }
        }
         composable(TakStudDestinations.NOTICE_LIST_ROUTE) {
            NoticeListScreen(
                notices = viewModel.notices.collectAsState().value,
                onAddNotice = { navController.navigate("${TakStudDestinations.ADD_NOTICE_ROUTE}/null") },
                onNoticeClick = { notice -> navController.navigate("${TakStudDestinations.ADD_NOTICE_ROUTE}/${notice.id}") },
                onDeleteNotice = { notice -> viewModel.deleteNotice(notice) },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.ADD_NOTICE_ROUTE}/{noticeId}") { backStackEntry ->
            val noticeId = backStackEntry.arguments?.getString("noticeId")
            val notice = if (noticeId == "null") null else viewModel.notices.collectAsState().value.find { it.id == noticeId }
            AddNoticeScreen(
                noticeToEdit = notice,
                schedules = viewModel.schedules.collectAsState().value,
                onSave = { n -> viewModel.saveNotice(n) { navController.popBackStack() } },
                onBack = { navigationActions.onBack() }
            )
        }
        composable(TakStudDestinations.MANAGE_STUDENTS_ROUTE) {
            ManageStudentsScreen(
                students = viewModel.students.collectAsState().value,
                classesByPeriod = viewModel.classesByPeriod.collectAsState().value,
                onRegisterStudent = { name, ra, className -> viewModel.registerStudent(name, ra, className) },
                onDeleteStudent = { student -> viewModel.deleteStudent(student) },
                onBack = { navigationActions.onBack() }
            )
        }
        
        composable(TakStudDestinations.SCHEDULES_LIST_ROUTE) {
            SchedulesListScreen(
                schedules = viewModel.schedules.collectAsState().value,
                onAddSchedule = { navController.navigate("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/null") },
                onScheduleClick = { schedule -> navController.navigate("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/${schedule.id}") },
                onDeleteSchedule = { schedule -> viewModel.deleteSchedule(schedule) },
                onAddMissingSchedules = { /* TODO: Implement add missing schedules */ },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            val schedule = if (scheduleId == "null") null else viewModel.schedules.collectAsState().value.find { it.id == scheduleId }
            ManageScheduleScreen(
                scheduleToEdit = schedule,
                onSave = { s -> viewModel.saveSchedule(s) { navController.popBackStack() } },
                onBack = { navigationActions.onBack() }
            )
        }
         composable("${TakStudDestinations.SCHEDULE_DETAILS_ROUTE}/{scheduleId}/{studentId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            val studentId = backStackEntry.arguments?.getString("studentId")
            val schedule = viewModel.schedules.collectAsState().value.find { it.id == scheduleId }
            val student = viewModel.students.collectAsState().value.find { it.id == studentId }
            if (schedule != null && student != null) {
                 ScheduleDetailsScreen(
                    schedule = schedule,
                    onBack = { navigationActions.onBack() }
                ) 
            }
        }
        composable(TakStudDestinations.ATTENDANCE_ROUTE) {
            AttendanceScreen(
                classesByPeriod = viewModel.classesByPeriod.collectAsState().value,
                onTakeAttendance = { studentClass, date ->
                    viewModel.setAttendanceData(studentClass, date)
                    navController.navigate(TakStudDestinations.TAKE_ATTENDANCE_ROUTE)
                },
                onBack = { navigationActions.onBack() }
            )
        }
        composable(TakStudDestinations.TAKE_ATTENDANCE_ROUTE) {
            val studentClass = viewModel.selectedClassForAttendance.collectAsState().value
            val date = viewModel.selectedDateForAttendance.collectAsState().value

            if (studentClass.isNotBlank() && date.isNotBlank()) {
                TakeAttendanceScreen(
                    studentClass = studentClass,
                    date = date,
                    students = viewModel.getStudentsForClass(studentClass).collectAsState().value,
                    records = viewModel.getAttendanceForClassByDate(studentClass, date).collectAsState().value,
                    onSaveAttendance = { record -> viewModel.saveAttendanceRecord(record) },
                    onBack = {
                        viewModel.clearAttendanceData()
                        navigationActions.onBack()
                    }
                )
            } else {
                navigationActions.onBack()
            }
        }

    }
}
