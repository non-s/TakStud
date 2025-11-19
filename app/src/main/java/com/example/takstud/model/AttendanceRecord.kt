package com.example.takstud.model

/**
 * AttendanceRecord data model for tracking student attendance.
 * Records presence or absence for each student on each day.
 *
 * @param id Unique record ID
 * @param date Date of attendance (yyyy-MM-dd format)
 * @param studentId Student's unique ID
 * @param studentRa Student's registration number
 * @param studentName Student's name
 * @param studentClass Class name (legacy field, use classId instead)
 * @param classId Class ID for relation with Class entity
 * @param isPresent True if student was present, false if absent
 * @param createdAt Timestamp when record was created
 * @param modifiedAt Timestamp when record was last modified (for sync)
 * @param isSynced Whether record is synced with Firebase
 */
data class AttendanceRecord(
    val id: String = "",
    val date: String = "",
    val studentId: String = "",
    val studentRa: String = "",
    val studentName: String = "",
    val studentClass: String = "",
    val classId: String = "",
    val isPresent: Boolean = true,
    val createdAt: Long = 0,
    val modifiedAt: Long = 0,
    val isSynced: Boolean = false
)
