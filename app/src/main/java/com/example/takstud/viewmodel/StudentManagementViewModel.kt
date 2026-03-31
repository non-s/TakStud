package com.example.takstud.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.StudentAuthRepository
import com.example.takstud.model.Student
import com.example.takstud.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for student management screen.
 * Teachers use this to register new students and manage their RAs.
 * Handles registration validation and Firestore operations.
 */
@HiltViewModel
class StudentManagementViewModel @Inject constructor(
    private val repository: StudentAuthRepository
) : ViewModel() {

    // Registration state
    private val _registrationState = MutableStateFlow<Result<Unit>>(Result.Loading)
    val registrationState: StateFlow<Result<Unit>> = _registrationState

    // List of all students
    private val _allStudents = MutableStateFlow<Result<List<Student>>>(Result.Loading)
    val allStudents: StateFlow<Result<List<Student>>> = _allStudents

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Register a new student in the system.
     * Teachers call this to add a student with their RA.
     *
     * @param ra Student registration number
     * @param name Student full name
     * @param studentClass Class/grade
     * @param parent Parent/guardian name
     * @param phone Contact phone number
     */
    fun registerStudent(
        ra: String,
        name: String,
        studentClass: String,
        parent: String,
        phone: String
    ) {
        // Validate inputs are not empty
        if (ra.isBlank() || name.isBlank() || studentClass.isBlank() || parent.isBlank() || phone.isBlank()) {
            _registrationState.value = Result.Error(Exception("Todos os campos são obrigatórios"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check if RA already exists
                val existsResult = repository.raExists(ra)
                when (existsResult) {
                    is Result.Success -> {
                        if (existsResult.data) {
                            _registrationState.value = Result.Error(Exception("RA já existe no sistema"))
                            _isLoading.value = false
                            return@launch
                        }
                    }
                    is Result.Error -> {
                        _registrationState.value = existsResult
                        _isLoading.value = false
                        return@launch
                    }
                    is Result.Loading -> {}
                }

                // Register the student
                val result = repository.registerStudent(ra, name, studentClass, parent, phone)
                _registrationState.value = result

                if (result is Result.Success) {
                    Log.d("StudentManagement", "Student registered successfully: $ra")
                    // Refresh the student list
                    loadAllStudents()
                }
            } catch (e: Exception) {
                Log.e("StudentManagement", "Error during student registration", e)
                _registrationState.value = Result.Error(e)
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all registered students from Firestore.
     * Teachers use this to view all students in the system.
     */
    fun loadAllStudents() {
        viewModelScope.launch {
            try {
                val result = repository.getAllStudents()
                _allStudents.value = result
                Log.d("StudentManagement", "Loaded all students: ${(result as? Result.Success)?.data?.size ?: 0}")
            } catch (e: Exception) {
                Log.e("StudentManagement", "Error loading students", e)
                _allStudents.value = Result.Error(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a student from the system by RA.
     *
     * @param ra The RA of the student to delete
     */
    fun deleteStudent(ra: String) {
        if (ra.isBlank()) {
            _registrationState.value = Result.Error(Exception("RA não pode estar vazio"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.deleteStudent(ra)
                _registrationState.value = result

                if (result is Result.Success) {
                    Log.d("StudentManagement", "Student deleted successfully: $ra")
                    // Refresh the student list
                    loadAllStudents()
                }
            } catch (e: Exception) {
                Log.e("StudentManagement", "Error deleting student", e)
                _registrationState.value = Result.Error(e)
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset registration state after successful operation.
     */
    fun resetRegistrationState() {
        _registrationState.value = Result.Loading
    }

    /**
     * Initialize the ViewModel by loading all students.
     */
    init {
        loadAllStudents()
    }
}
