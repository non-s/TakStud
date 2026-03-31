package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.NoticeRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.TaskRepository
import com.example.takstud.domain.usecase.GetAvailableClassesUseCase
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.model.Task
import com.example.takstud.model.task.toTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ParentViewModel @Inject constructor(
    taskRepository: TaskRepository,
    noticeRepository: NoticeRepository,
    scheduleRepository: ScheduleRepository,
    getAvailableClassesUseCase: GetAvailableClassesUseCase
) : ViewModel() {

    private val _selectedClass = MutableStateFlow<String?>(null)
    val selectedClass: StateFlow<String?> = _selectedClass

    private val _allTasks = taskRepository.getTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allNotices = noticeRepository.getNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allSchedules = scheduleRepository.getSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Utilizando o Use Case injetado
    val availableClasses: StateFlow<List<String>> = getAvailableClassesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<Task>> = combine(
        _allTasks,
        selectedClass
    ) { tasks, selectedClass ->
        if (selectedClass == null) {
            tasks.map { it.toTask() }
        } else {
            tasks.filter { it.classId == selectedClass }.map { it.toTask() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredNotices: StateFlow<List<Notice>> = combine(
        _allNotices,
        selectedClass
    ) { notices, selectedClass ->
        if (selectedClass == null) {
            notices
        } else {
            notices.filter { it.studentClass == selectedClass }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSchedules: StateFlow<List<Schedule>> = combine(
        _allSchedules,
        selectedClass
    ) { schedules, selectedClass ->
        if (selectedClass == null) {
            schedules
        } else {
            schedules.filter { it.studentClass == selectedClass }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectClass(className: String?) {
        _selectedClass.value = className
    }
}
