package com.example.takstud.model

/**
 * Class (Turma) data model for managing student groups.
 * Contains class information used for organizing students and taking attendance.
 *
 * @param id Unique document ID
 * @param name Class name (e.g., "6A", "7B", "1º Ano A")
 * @param grade Grade level
 * @param year Academic year
 * @param createdAt Timestamp when class was created
 */
data class Class(
    val id: String = "",
    val name: String = "",
    val grade: String = "",
    val year: String = "",
    val createdAt: Long = 0
)
