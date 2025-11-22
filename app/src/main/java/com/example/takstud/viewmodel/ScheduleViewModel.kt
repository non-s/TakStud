package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.model.Schedule
import com.example.takstud.model.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    val schedules: StateFlow<List<Schedule>> = scheduleRepository.getSchedules()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val classesByPeriod: StateFlow<Map<String, List<String>>> = scheduleRepository.getClassesByPeriod()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getSchedulesForStudent(student: Student): StateFlow<List<Schedule>> {
        return schedules.combine(schedules) { schedules, _ ->
            schedules.filter { it.studentClass == student.studentClass }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun saveSchedule(schedule: Schedule, onBack: () -> Unit) {
        scheduleRepository.saveSchedule(schedule, onBack)
    }

    fun deleteSchedule(schedule: Schedule) {
        scheduleRepository.deleteSchedule(schedule)
    }
}
