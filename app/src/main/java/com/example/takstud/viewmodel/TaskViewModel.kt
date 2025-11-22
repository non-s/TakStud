package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.GradeRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.data.repository.TaskRepository
import com.example.takstud.model.Grade
import com.example.takstud.model.Student
import com.example.takstud.model.task.TaskExtended
import com.example.takstud.model.task.TaskSubmission
import com.example.takstud.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val scheduleRepository: ScheduleRepository,
    private val gradeRepository: GradeRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskExtended>> = taskRepository.getTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val grades: StateFlow<List<Grade>> = gradeRepository.getGrades()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentTask = MutableStateFlow<UiState<TaskExtended>>(UiState.Empty)
    val currentTask: StateFlow<UiState<TaskExtended>> = _currentTask.asStateFlow()

    fun getTasksForStudent(student: Student): StateFlow<List<TaskExtended>> {
        return tasks.combine(scheduleRepository.getSchedules()) { tasks, schedules ->
            // TODO: Melhorar lógica de filtro usando schedules
            tasks.filter { it.className == student.studentClass }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun getGradesForStudent(student: Student): StateFlow<List<Grade>> {
        return grades.combine(studentRepository.getStudents()) { grades, _ ->
            grades.filter { it.studentId == student.id }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }
    
    fun getGradesForTask(taskId: String): StateFlow<List<Grade>> {
        return grades.combine(tasks) { grades, _ ->
            grades.filter { it.taskId == taskId }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _currentTask.value = UiState.Loading()
            try {
                taskRepository.getTaskById(taskId).collect { task ->
                    if (task != null) {
                        _currentTask.value = UiState.Success(task)
                    } else {
                        _currentTask.value = UiState.Error("Tarefa não encontrada")
                    }
                }
            } catch (e: Exception) {
                _currentTask.value = UiState.Error(e.message ?: "Erro ao carregar tarefa")
            }
        }
    }

    fun saveTask(task: TaskExtended, onBack: () -> Unit) {
        taskRepository.saveTask(task, onBack)
    }

    fun deleteTask(task: TaskExtended) {
        taskRepository.deleteTask(task)
    }

    fun saveGrade(grade: Grade) {
        gradeRepository.saveGrade(grade)
    }

    fun submitTask(taskId: String, submission: TaskSubmission) {
        viewModelScope.launch {
            // Em um app real, isso seria uma transação atômica no Firestore
            // Atualizando a lista de submissões dentro do documento da Task
            val currentTaskState = _currentTask.value
            if (currentTaskState is UiState.Success) {
                val task = currentTaskState.data
                val newSubmissions = task.submissions + submission
                val updatedTask = task.copy(
                    submissions = newSubmissions,
                    totalSubmissions = newSubmissions.size
                )
                taskRepository.saveTask(updatedTask) {
                    // Sucesso
                }
            }
        }
    }
}
