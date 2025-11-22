package com.example.takstud.ui.teacher.student

enum class StudentSortOption {
    NAME_ASC,
    NAME_DESC,
    REGISTRATION_ASC,
    REGISTRATION_DESC,
    GPA_DESC,
    GPA_ASC,
    ATTENDANCE_DESC,
    ATTENDANCE_ASC
}

enum class StudentStatus(val displayName: String) {
    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    TRANSFERRED("Transferido"),
    DROPPED("Evadido")
}

data class StudentFilters(
    val className: String? = null,
    val grade: String? = null,
    val period: String? = null,
    val status: StudentStatus? = null,
    val isScholarship: Boolean? = null,
    val hasSpecialNeeds: Boolean = false,
    val sortBy: StudentSortOption? = null
)

sealed class StudentUiState {
    object Loading : StudentUiState()
    data class Success(val students: List<com.example.takstud.model.student.StudentExtended>) : StudentUiState()
    data class Error(val message: String) : StudentUiState()
}
