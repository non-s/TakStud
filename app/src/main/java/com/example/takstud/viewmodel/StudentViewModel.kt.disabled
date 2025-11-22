package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.AttendanceRepository
import com.example.takstud.data.repository.GradeRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.model.Student
import com.example.takstud.util.PredictionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val gradeRepository: GradeRepository,
    private val scheduleRepository: ScheduleRepository,
    private val predictionEngine: PredictionEngine
) : ViewModel() {

    val students: StateFlow<List<Student>> = studentRepository.getStudents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _predictionResult = MutableStateFlow<PredictionEngine.PredictionResult?>(null)
    val predictionResult: StateFlow<PredictionEngine.PredictionResult?> = _predictionResult.asStateFlow()

    fun saveStudent(student: Student, onBack: () -> Unit) {
        studentRepository.saveStudent(student, onBack)
    }

    fun deleteStudent(student: Student) {
        studentRepository.deleteStudent(student)
    }

    fun analyzeStudentPerformance(studentId: String) {
        viewModelScope.launch {
            val student = students.value.find { it.id == studentId } ?: return@launch
            // Usando first() para pegar o estado atual dos flows
            val grades = gradeRepository.getGrades().first().filter { it.studentId == studentId }
            val attendance = attendanceRepository.getAttendanceRecords().first().filter { it.studentId == studentId }
            
            _predictionResult.value = predictionEngine.predictStudentPerformance(student, grades, attendance)
        }
    }

    fun getStudentsForClass(className: String): StateFlow<List<Student>> {
        return students.map { list ->
            list.filter { it.studentClass == className }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun registerStudent(name: String, ra: String, className: String) {
        val newStudent = Student(
            name = name,
            registrationNumber = ra,
            studentClass = className
        )
        saveStudent(newStudent) {}
    }
}
