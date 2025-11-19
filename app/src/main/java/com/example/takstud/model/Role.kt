package com.example.takstud.model

/**
 * User roles in the system
 */
enum class Role(val displayName: String) {
    PARENT("Responsável"),
    TEACHER("Professor");

    companion object {
        fun fromString(value: String?): Role? = values().find { it.name == value }
    }
}