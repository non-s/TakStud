package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.AttendanceRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val studentRepository: StudentRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    val attendanceRecords: StateFlow<List<AttendanceRecord>> = attendanceRepository.getAttendanceRecords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedClassForAttendance = MutableStateFlow("")
    val selectedClassForAttendance: StateFlow<String> = _selectedClassForAttendance.asStateFlow()

    private val _selectedDateForAttendance = MutableStateFlow("")
    val selectedDateForAttendance: StateFlow<String> = _selectedDateForAttendance.asStateFlow()

    fun setAttendanceData(className: String, date: String) {
        _selectedClassForAttendance.value = className
        _selectedDateForAttendance.value = date
    }

    fun clearAttendanceData() {
        _selectedClassForAttendance.value = ""
        _selectedDateForAttendance.value = ""
    }

    fun getAttendanceForStudent(student: Student): StateFlow<List<AttendanceRecord>> {
        return attendanceRecords.combine(studentRepository.getStudents()) { records, _ ->
            records.filter { it.studentId == student.id }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun getAttendanceForClassByDate(studentClass: String, date: String): StateFlow<List<AttendanceRecord>> {
        return attendanceRecords.combine(scheduleRepository.getSchedules()) { records, _ ->
            records.filter { it.studentClass == studentClass && it.date == date }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun saveAttendanceRecord(record: AttendanceRecord) {
        attendanceRepository.saveAttendanceRecord(record)
    }
}
