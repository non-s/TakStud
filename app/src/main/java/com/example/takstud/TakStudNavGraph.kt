package com.example.takstud

import androidx.navigation.NavController

object TakStudDestinations {
    const val HOME_ROUTE = "home"
    const val LOGIN_ROUTE = "login"
    const val TEACHER_LOGIN_ROUTE = "teacher_login"
    const val PARENT_LOGIN_ROUTE = "parent_login"
    const val ADMIN_LOGIN_ROUTE = "admin_login"
    const val TEACHER_ROUTE = "teacher"
    const val PARENT_ROUTE = "parent"
    const val TASK_LIST_ROUTE = "task_list"
    const val ADD_TASK_ROUTE = "add_task"
    const val MANAGE_GRADES_ROUTE = "manage_grades"
    const val NOTICE_LIST_ROUTE = "notice_list"
    const val ADD_NOTICE_ROUTE = "add_notice"
    const val MANAGE_STUDENTS_ROUTE = "manage_students"
    const val REGISTER_STUDENT_ROUTE = "register_student"
    const val SCHEDULES_LIST_ROUTE = "schedules_list"
    const val MANAGE_SCHEDULE_ROUTE = "manage_schedule"
    const val SCHEDULE_DETAILS_ROUTE = "schedule_details"
    const val ATTENDANCE_ROUTE = "attendance"
    const val TAKE_ATTENDANCE_ROUTE = "take_attendance"
    const val MANAGE_CLASSES_ROUTE = "manage_classes"
    const val ADD_CLASS_ROUTE = "add_class"
}

class TakStudNavigationActions(navController: NavController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(TakStudDestinations.HOME_ROUTE) {
            popUpTo(0) { inclusive = true }
        }
    }
    val navigateToLogin: () -> Unit = {
        navController.navigate(TakStudDestinations.LOGIN_ROUTE) {
            popUpTo(0) { inclusive = true }
        }
    }
    val navigateToTeacherLogin: () -> Unit = {
        navController.navigate(TakStudDestinations.TEACHER_LOGIN_ROUTE)
    }
    val navigateToParentLogin: () -> Unit = {
        navController.navigate(TakStudDestinations.PARENT_LOGIN_ROUTE)
    }
    val navigateToAdminLogin: () -> Unit = {
        navController.navigate(TakStudDestinations.ADMIN_LOGIN_ROUTE)
    }
    val navigateToTeacher: () -> Unit = {
        navController.navigate(TakStudDestinations.TEACHER_ROUTE) {
            popUpTo(TakStudDestinations.HOME_ROUTE)
        }
    }
    val navigateToParent: (String) -> Unit = { studentId ->
        navController.navigate("${TakStudDestinations.PARENT_ROUTE}/$studentId") {
            popUpTo(TakStudDestinations.HOME_ROUTE)
        }
    }
    // Adicione outras ações de navegação aqui conforme necessário
    val onBack: () -> Unit = {
        navController.popBackStack()
    }
}
