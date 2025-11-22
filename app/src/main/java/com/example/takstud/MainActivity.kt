package com.example.takstud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.takstud.viewmodel.AttendanceViewModel
import com.example.takstud.viewmodel.AuthViewModel
import com.example.takstud.viewmodel.NoticeViewModel
import com.example.takstud.viewmodel.ScheduleViewModel
import com.example.takstud.viewmodel.StudentViewModel
import com.example.takstud.viewmodel.TaskViewModel
import com.example.takstud.model.task.TaskExtended
import com.example.takstud.model.task.toTask
import com.example.takstud.model.task.toTaskExtended
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
fun TakStudApp(
    taskViewModel: TaskViewModel = hiltViewModel(),
    studentViewModel: StudentViewModel = hiltViewModel(),
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
    noticeViewModel: NoticeViewModel = hiltViewModel(),
    attendanceViewModel: AttendanceViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
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
                val student = studentViewModel.students.collectAsState().value.find { it.id == studentId }
                if (student != null) {
                    ParentScreen(
                        student = student,
                        tasks = taskViewModel.getTasksForStudent(student).collectAsState().value.map { it.toTask() },
                        notices = noticeViewModel.getNoticesForStudent(student).collectAsState().value,
                        schedules = scheduleViewModel.getSchedulesForStudent(student).collectAsState().value,
                        grades = taskViewModel.getGradesForStudent(student).collectAsState().value,
                        attendance = attendanceViewModel.getAttendanceForStudent(student).collectAsState().value,
                        onLogout = { navigationActions.navigateToHome() },
                        onScheduleClick = { schedule -> navController.navigate("${TakStudDestinations.SCHEDULE_DETAILS_ROUTE}/${schedule.id}/${student.id}") }
                    )
                }
            }
        }
        composable(TakStudDestinations.TASK_LIST_ROUTE) {
             TaskListScreen(
                tasks = taskViewModel.tasks.collectAsState().value.map { it.toTask() },
                onAddTask = { navController.navigate("${TakStudDestinations.ADD_TASK_ROUTE}/null") },
                onTaskClick = { task -> navController.navigate("${TakStudDestinations.ADD_TASK_ROUTE}/${task.id}") },
                onDeleteTask = { task -> taskViewModel.deleteTask(task.toTaskExtended()) },
                onManageGrades = { task -> navController.navigate("${TakStudDestinations.MANAGE_GRADES_ROUTE}/${task.id}") },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.ADD_TASK_ROUTE}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = if (taskId == "null") null else taskViewModel.tasks.collectAsState().value.find { it.id == taskId }?.toTask()
            AddTaskScreen(
                taskToEdit = task,
                schedules = scheduleViewModel.schedules.collectAsState().value,
                onSave = { t -> taskViewModel.saveTask(t.toTaskExtended()) { navController.popBackStack() } },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.MANAGE_GRADES_ROUTE}/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = taskViewModel.tasks.collectAsState().value.find { it.id == taskId }?.toTask()
            if (task != null) {
                 ManageGradesScreen(
                    task = task,
                    students = studentViewModel.getStudentsForClass(task.studentClass).collectAsState().value,
                    grades = taskViewModel.getGradesForTask(task.id).collectAsState().value,
                    onSaveGrade = { grade -> taskViewModel.saveGrade(grade) },
                    onBack = { navigationActions.onBack() }
                )
            }
        }
         composable(TakStudDestinations.NOTICE_LIST_ROUTE) {
            NoticeListScreen(
                notices = noticeViewModel.notices.collectAsState().value,
                onAddNotice = { navController.navigate("${TakStudDestinations.ADD_NOTICE_ROUTE}/null") },
                onNoticeClick = { notice -> navController.navigate("${TakStudDestinations.ADD_NOTICE_ROUTE}/${notice.id}") },
                onDeleteNotice = { notice -> noticeViewModel.deleteNotice(notice) },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.ADD_NOTICE_ROUTE}/{noticeId}") { backStackEntry ->
            val noticeId = backStackEntry.arguments?.getString("noticeId")
            val notice = if (noticeId == "null") null else noticeViewModel.notices.collectAsState().value.find { it.id == noticeId }
            AddNoticeScreen(
                noticeToEdit = notice,
                schedules = scheduleViewModel.schedules.collectAsState().value,
                onSave = { n -> noticeViewModel.saveNotice(n) { navController.popBackStack() } },
                onBack = { navigationActions.onBack() }
            )
        }
        composable(TakStudDestinations.MANAGE_STUDENTS_ROUTE) {
            ManageStudentsScreen(
                students = studentViewModel.students.collectAsState().value,
                classesByPeriod = scheduleViewModel.classesByPeriod.collectAsState().value,
                onRegisterStudent = { name, ra, className -> studentViewModel.registerStudent(name, ra, className) },
                onDeleteStudent = { student -> studentViewModel.deleteStudent(student) },
                onBack = { navigationActions.onBack() }
            )
        }
        
        composable(TakStudDestinations.SCHEDULES_LIST_ROUTE) {
            SchedulesListScreen(
                schedules = scheduleViewModel.schedules.collectAsState().value,
                onAddSchedule = { navController.navigate("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/null") },
                onScheduleClick = { schedule -> navController.navigate("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/${schedule.id}") },
                onDeleteSchedule = { schedule -> scheduleViewModel.deleteSchedule(schedule) },
                onAddMissingSchedules = { /* TODO: Implement add missing schedules */ },
                onBack = { navigationActions.onBack() }
            )
        }
        composable("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            val schedule = if (scheduleId == "null") null else scheduleViewModel.schedules.collectAsState().value.find { it.id == scheduleId }
            ManageScheduleScreen(
                scheduleToEdit = schedule,
                onSave = { s -> scheduleViewModel.saveSchedule(s) { navController.popBackStack() } },
                onBack = { navigationActions.onBack() }
            )
        }
         composable("${TakStudDestinations.SCHEDULE_DETAILS_ROUTE}/{scheduleId}/{studentId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            val studentId = backStackEntry.arguments?.getString("studentId")
            val schedule = scheduleViewModel.schedules.collectAsState().value.find { it.id == scheduleId }
            val student = studentViewModel.students.collectAsState().value.find { it.id == studentId }
            if (schedule != null && student != null) {
                 ScheduleDetailsScreen(
                    schedule = schedule,
                    onBack = { navigationActions.onBack() }
                ) 
            }
        }
        composable(TakStudDestinations.ATTENDANCE_ROUTE) {
            AttendanceScreen(
                classesByPeriod = scheduleViewModel.classesByPeriod.collectAsState().value,
                onTakeAttendance = { studentClass, date ->
                    attendanceViewModel.setAttendanceData(studentClass, date)
                    navController.navigate(TakStudDestinations.TAKE_ATTENDANCE_ROUTE)
                },
                onBack = { navigationActions.onBack() }
            )
        }
        composable(TakStudDestinations.TAKE_ATTENDANCE_ROUTE) {
            val studentClass = attendanceViewModel.selectedClassForAttendance.collectAsState().value
            val date = attendanceViewModel.selectedDateForAttendance.collectAsState().value

            if (studentClass.isNotBlank() && date.isNotBlank()) {
                TakeAttendanceScreen(
                    studentClass = studentClass,
                    date = date,
                    students = studentViewModel.getStudentsForClass(studentClass).collectAsState().value,
                    records = attendanceViewModel.getAttendanceForClassByDate(studentClass, date).collectAsState().value,
                    onSaveAttendance = { record -> attendanceViewModel.saveAttendanceRecord(record) },
                    onBack = {
                        attendanceViewModel.clearAttendanceData()
                        navigationActions.onBack()
                    }
                )
            } else {
                navigationActions.onBack()
            }
        }

    }
}
