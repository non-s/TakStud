package com.example.takstud.model

/**
 * Permissions in the system
 * Each permission can be assigned to specific roles
 */
enum class Permission(val description: String) {
    // Parent permissions
    VIEW_OWN_STUDENT("Ver dados do aluno"),
    VIEW_TASKS("Ver tarefas"),
    VIEW_GRADES("Ver notas"),
    VIEW_NOTICES("Ver avisos"),
    VIEW_SCHEDULE("Ver horário"),
    VIEW_ATTENDANCE("Ver frequência"),

    // Teacher permissions
    MANAGE_STUDENTS("Gerenciar estudantes"),
    CREATE_TASK("Criar tarefas"),
    EDIT_TASK("Editar tarefas"),
    DELETE_TASK("Deletar tarefas"),
    MANAGE_GRADES("Atribuir notas"),
    CREATE_NOTICE("Criar avisos"),
    EDIT_NOTICE("Editar avisos"),
    DELETE_NOTICE("Deletar avisos"),
    MANAGE_SCHEDULES("Gerenciar horários"),
    TAKE_ATTENDANCE("Registrar frequência"),
    REGISTER_STUDENT("Registrar novo estudante"),
    DELETE_STUDENT("Deletar estudante");

    companion object {
        fun getPermissionsForRole(role: Role): Set<Permission> = when (role) {
            Role.PARENT -> setOf(
                VIEW_OWN_STUDENT,
                VIEW_TASKS,
                VIEW_GRADES,
                VIEW_NOTICES,
                VIEW_SCHEDULE,
                VIEW_ATTENDANCE
            )
            Role.TEACHER -> setOf(
                MANAGE_STUDENTS,
                CREATE_TASK,
                EDIT_TASK,
                DELETE_TASK,
                MANAGE_GRADES,
                CREATE_NOTICE,
                EDIT_NOTICE,
                DELETE_NOTICE,
                MANAGE_SCHEDULES,
                TAKE_ATTENDANCE,
                REGISTER_STUDENT,
                DELETE_STUDENT
            )
        }
    }
}