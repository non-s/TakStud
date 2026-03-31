package com.example.takstud.ui.navigation

import androidx.navigation.NavController

object TakStudDestinations {
    const val HOME_ROUTE = "home"
    const val LOGIN_ROUTE = "login"
    const val TEACHER_LOGIN_ROUTE = "teacher_login"
    const val PARENT_LOGIN_ROUTE = "parent_login"
    const val TEACHER_ROUTE = "teacher"
    const val PARENT_ROUTE = "parent"
    const val PARENT_TASK_LIST_ROUTE = "parent_task_list"
    const val PARENT_NOTICE_LIST_ROUTE = "parent_notice_list"
    const val PARENT_SCHEDULE_LIST_ROUTE = "parent_schedule_list"

    // Funcionalidades principais
    const val TASK_LIST_ROUTE = "task_list"
    const val ADD_TASK_ROUTE = "add_task"
    const val NOTICE_LIST_ROUTE = "notice_list"
    const val ADD_NOTICE_ROUTE = "add_notice"
    const val SCHEDULES_LIST_ROUTE = "schedules_list"
    const val MANAGE_SCHEDULE_ROUTE = "manage_schedule"
    const val SCHEDULE_DETAILS_ROUTE = "schedule_details"
}

class TakStudNavigationActions(navController: NavController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(TakStudDestinations.HOME_ROUTE) {
            popUpTo(0) { inclusive = true } }
    }
    val navigateToLogin: () -> Unit = {
        navController.navigate(TakStudDestinations.LOGIN_ROUTE) {
            popUpTo(0) { inclusive = true } }
    }
    val navigateToTeacherLogin: () -> Unit = {
        navController.navigate(TakStudDestinations.TEACHER_LOGIN_ROUTE)
    }
    val navigateToParentLogin: () -> Unit = {
        navController.navigate(TakStudDestinations.PARENT_LOGIN_ROUTE)
    }
    val navigateToTeacher: () -> Unit = {
        navController.navigate(TakStudDestinations.TEACHER_ROUTE) {
            popUpTo(TakStudDestinations.HOME_ROUTE)
        }
    }
    val navigateToParent: () -> Unit = {
        navController.navigate(TakStudDestinations.PARENT_ROUTE) {
            popUpTo(TakStudDestinations.HOME_ROUTE) { inclusive = false }
        }
    }
    val navigateToParentTaskList: () -> Unit = {
        navController.navigate(TakStudDestinations.PARENT_TASK_LIST_ROUTE)
    }
    val navigateToParentNoticeList: () -> Unit = {
        navController.navigate(TakStudDestinations.PARENT_NOTICE_LIST_ROUTE)
    }
    val navigateToParentScheduleList: () -> Unit = {
        navController.navigate(TakStudDestinations.PARENT_SCHEDULE_LIST_ROUTE)
    }
    val onBack: () -> Unit = {
        navController.popBackStack()
    }
}
