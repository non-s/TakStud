package com.example.takstud.model

data class Notice(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val studentClass: String = "",
    val createdAt: Long = 0,
    val modifiedAt: Long = 0,
    val isSynced: Boolean = false
)
