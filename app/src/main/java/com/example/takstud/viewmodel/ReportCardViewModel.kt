package com.example.takstud.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.GradeRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.data.repository.TaskRepository

import com.example.takstud.model.grade.ReportCard
import com.example.takstud.ui.common.BaseViewModel
import com.example.takstud.ui.common.UiState
import com.example.takstud.util.ReportCardGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportCardViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val gradeRepository: GradeRepository,
    private val taskRepository: TaskRepository,
    private val reportCardGenerator: ReportCardGenerator
) : BaseViewModel<ReportCard>() {

    private val _reportCard = MutableStateFlow<UiState<ReportCard>>(UiState.Loading())
    val reportCard: StateFlow<UiState<ReportCard>> = _reportCard.asStateFlow()

    fun loadReportCard(studentId: String) {
        viewModelScope.launch {
            _reportCard.value = UiState.Loading()
            try {
                val student = studentRepository.getStudents().first().find { it.id == studentId }
                if (student == null) {
                    _reportCard.value = UiState.Error("Estudante não encontrado")
                    return@launch
                }

                // Carregar dados relacionados
                // Nota: Em um app real, filtraríamos por ano/semestre aqui
                val grades = gradeRepository.getGrades().first().filter { it.studentId == studentId }
                val tasks = taskRepository.getTasks().first() // Idealmente filtrar tasks relevantes

                val report = reportCardGenerator.generateReportCard(
                    student = student,
                    grades = grades,
                    tasks = tasks,
                    academicYear = 2025, // TODO: Pegar do sistema/config
                    semester = 1
                )

                _reportCard.value = UiState.Success(report)

            } catch (e: Exception) {
                _reportCard.value = UiState.Error("Erro ao gerar boletim: ${e.message}")
            }
        }
    }
}
