package com.example.takstud.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.takstud.model.Role
import com.example.takstud.model.task.toTask
import com.example.takstud.model.task.toTaskExtended
import com.example.takstud.ui.HomeScreen
import com.example.takstud.ui.RequireAuthentication
import com.example.takstud.ui.RequireRole
import com.example.takstud.ui.login.LoginScreen
import com.example.takstud.ui.login.ParentLoginScreen
import com.example.takstud.ui.login.TeacherLoginScreen
import com.example.takstud.ui.parent.ParentNoticeListScreen
import com.example.takstud.ui.parent.ParentScheduleListScreen
import com.example.takstud.ui.parent.ParentScreen
import com.example.takstud.ui.parent.ParentTaskListScreen
import com.example.takstud.ui.teacher.AddNoticeScreen
import com.example.takstud.ui.teacher.AddTaskScreen
import com.example.takstud.ui.teacher.ManageScheduleScreen
import com.example.takstud.ui.teacher.NoticeListScreen
import com.example.takstud.ui.teacher.ScheduleDetailsScreen
import com.example.takstud.ui.teacher.SchedulesListScreen
import com.example.takstud.ui.teacher.TaskListScreen
import com.example.takstud.ui.teacher.TeacherScreen
import com.example.takstud.viewmodel.AuthViewModel
import com.example.takstud.viewmodel.NoticeViewModel
import com.example.takstud.viewmodel.ParentViewModel
import com.example.takstud.viewmodel.ScheduleViewModel
import com.example.takstud.viewmodel.TaskViewModel

@Composable
fun TakStudNavHost(
    navController: NavHostController,
    taskViewModel: TaskViewModel = hiltViewModel(),
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
    noticeViewModel: NoticeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    parentViewModel: ParentViewModel = hiltViewModel()
) {
    val navigationActions = remember(navController) { TakStudNavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = TakStudDestinations.HOME_ROUTE
    ) {
        // TELA INICIAL
        composable(TakStudDestinations.HOME_ROUTE) {
            HomeScreen(
                onProfessorClick = { navigationActions.navigateToTeacherLogin() },
                onAlunoClick = { navigationActions.navigateToParentLogin() }
            )
        }

        // TELA LOGIN RESPONSÁVEL
        composable(TakStudDestinations.PARENT_LOGIN_ROUTE) {
            ParentLoginScreen(
                onLoginSuccess = { navigationActions.navigateToParent() },
                onBackClick = { navigationActions.onBack() }
            )
        }

        // TELA LOGIN PROFESSOR
        composable(TakStudDestinations.TEACHER_LOGIN_ROUTE) {
            TeacherLoginScreen(
                onLoginSuccess = { navigationActions.navigateToTeacher() },
                onBackClick = { navigationActions.onBack() }
            )
        }

        // TELA LOGIN ANTIGO
        composable(TakStudDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onTeacherLogin = { navigationActions.navigateToTeacherLogin() },
                onParentLoginSuccess = {
                    navigationActions.navigateToHome()
                }
            )
        }

        // GRUPO PROFESSOR
        composable(TakStudDestinations.TEACHER_ROUTE) {
            RequireRole(role = Role.TEACHER, fallbackRoute = { navigationActions.navigateToHome() }) {
                TeacherScreen(
                    onManageTasks = { navController.navigate(TakStudDestinations.TASK_LIST_ROUTE) },
                    onManageNotices = { navController.navigate(TakStudDestinations.NOTICE_LIST_ROUTE) },
                    onManageSchedules = { navController.navigate(TakStudDestinations.SCHEDULES_LIST_ROUTE) },
                    onLogout = { navigationActions.navigateToHome() }
                )
            }
        }

        // GRUPO RESPONSÁVEL
        composable(TakStudDestinations.PARENT_ROUTE) {
            RequireAuthentication(fallbackRoute = { navigationActions.navigateToHome() }) {
                val availableClasses by parentViewModel.availableClasses.collectAsState()
                val selectedClass by parentViewModel.selectedClass.collectAsState()

                ParentScreen(
                    availableClasses = availableClasses,
                    selectedClass = selectedClass,
                    onClassSelected = { parentViewModel.selectClass(it) },
                    onNavigateToTasks = { navigationActions.navigateToParentTaskList() },
                    onNavigateToNotices = { navigationActions.navigateToParentNoticeList() },
                    onNavigateToSchedules = { navigationActions.navigateToParentScheduleList() },
                    onLogout = { navigationActions.navigateToHome() }
                )
            }
        }

        composable(TakStudDestinations.PARENT_TASK_LIST_ROUTE) {
            val filteredTasks by parentViewModel.filteredTasks.collectAsState()
            ParentTaskListScreen(
                tasks = filteredTasks,
                onBack = { navigationActions.onBack() }
            )
        }

        composable(TakStudDestinations.PARENT_NOTICE_LIST_ROUTE) {
            val filteredNotices by parentViewModel.filteredNotices.collectAsState()
            ParentNoticeListScreen(
                notices = filteredNotices,
                onBack = { navigationActions.onBack() }
            )
        }

        composable(TakStudDestinations.PARENT_SCHEDULE_LIST_ROUTE) {
            val filteredSchedules by parentViewModel.filteredSchedules.collectAsState()
            ParentScheduleListScreen(
                schedules = filteredSchedules,
                onBack = { navigationActions.onBack() },
                onScheduleClick = { schedule ->
                    navController.navigate("${TakStudDestinations.SCHEDULE_DETAILS_ROUTE}/${schedule.id}/null")
                }
            )
        }

        // GERENCIAMENTO DE TAREFAS (PROFESSOR)
        composable(TakStudDestinations.TASK_LIST_ROUTE) {
            TaskListScreen(
                tasks = taskViewModel.tasks.collectAsState().value.map { it.toTask() },
                onAddTask = { navController.navigate("${TakStudDestinations.ADD_TASK_ROUTE}/null") },
                onTaskClick = { task -> navController.navigate("${TakStudDestinations.ADD_TASK_ROUTE}/${task.id}") },
                onDeleteTask = { task -> taskViewModel.deleteTask(task.toTaskExtended()) },
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

        // GERENCIAMENTO DE COMUNICADOS (PROFESSOR)
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

        // GERENCIAMENTO DE HORÁRIOS (PROFESSOR)
        composable(TakStudDestinations.SCHEDULES_LIST_ROUTE) {
            SchedulesListScreen(
                schedules = scheduleViewModel.schedules.collectAsState().value,
                onAddSchedule = { navController.navigate("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/null") },
                onScheduleClick = { schedule -> navController.navigate("${TakStudDestinations.MANAGE_SCHEDULE_ROUTE}/${schedule.id}") },
                onDeleteSchedule = { schedule -> scheduleViewModel.deleteSchedule(schedule) },
                onAddMissingSchedules = { },
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

        composable(
            route = "${TakStudDestinations.SCHEDULE_DETAILS_ROUTE}/{scheduleId}/{studentId}",
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.StringType },
                navArgument("studentId") { nullable = true }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")
            val schedule = scheduleViewModel.schedules.collectAsState().value.find { it.id == scheduleId }
            if (schedule != null) {
                ScheduleDetailsScreen(
                    schedule = schedule,
                    onBack = { navigationActions.onBack() }
                )
            }
        }
    }
}
