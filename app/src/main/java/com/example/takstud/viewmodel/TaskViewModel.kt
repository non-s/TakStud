package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.TaskRepository
import com.example.takstud.model.task.TaskExtended
import com.example.takstud.model.task.TaskSubmission
import com.example.takstud.ui.common.UiState
import com.example.takstud.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskExtended>> = taskRepository.getTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentTask = MutableStateFlow<UiState<TaskExtended>>(UiState.Empty())
    val currentTask: StateFlow<UiState<TaskExtended>> = _currentTask.asStateFlow()

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
                _currentTask.value = UiState.Error("Erro ao carregar tarefa: ${e.message}")
            }
        }
    }

    fun saveTask(task: TaskExtended, onBack: () -> Unit) {
        viewModelScope.launch {
            try {
                taskRepository.saveTask(task)
                onBack()
            } catch (e: Exception) {
                // Log error or show to user
            }
        }
    }

    fun deleteTask(task: TaskExtended) {
        viewModelScope.launch {
             ErrorHandler.withErrorHandling(
                operationName = "Deletar tarefa",
                userFacingMessage = "Erro ao deletar tarefa"
            ) {
                taskRepository.deleteTask(task)
            }
        }
    }

    fun submitTask(taskId: String, submission: TaskSubmission) {
        viewModelScope.launch {
            val currentTaskState = _currentTask.value
            if (currentTaskState is UiState.Success) {
                val task = currentTaskState.data
                val newSubmissions = task.submissions + submission
                val updatedTask = task.copy(
                    submissions = newSubmissions,
                    totalSubmissions = newSubmissions.size
                )
                
                ErrorHandler.withErrorHandling(
                    operationName = "Enviar tarefa",
                    userFacingMessage = "Erro ao enviar tarefa"
                ) {
                    taskRepository.saveTask(updatedTask)
                }
            }
        }
    }
}