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
 * ViewModel for login screen.
 * Handles both parent login (by RA) and teacher login (by access code).
 * Manages UI state with loading, error, and success states.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: StudentAuthRepository
) : ViewModel() {

    // Parent login state (Loading usado como estado "idle/aguardando")
    private val _parentLoginState = MutableStateFlow<Result<Student>>(Result.Loading)
    val parentLoginState: StateFlow<Result<Student>> = _parentLoginState

    // Teacher login state (Loading usado como estado "idle/aguardando")
    private val _teacherLoginState = MutableStateFlow<Result<Boolean>>(Result.Loading)
    val teacherLoginState: StateFlow<Result<Boolean>> = _teacherLoginState

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Login as parent using RA.
     * Validates the RA against registered students in Firestore and creates a session.
     *
     * @param ra The student RA
     */
    fun loginAsParent(ra: String) {
        // Validação usando LoginValidator
        val (isValid, errorMessage) = com.example.takstud.util.LoginValidator.validateRA(ra)
        if (!isValid) {
            _parentLoginState.value = Result.Error(Exception(errorMessage))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.loginParentWithSession(ra)
                _parentLoginState.value = result
                Log.d("LoginViewModel", "Parent login result: $result")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error during parent login", e)
                _parentLoginState.value = Result.Error(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Login as teacher using access code.
     * Validates the code against the stored teacher code in Firestore and creates a session.
     *
     * @param code The teacher access code
     */
    fun loginAsTeacher(code: String) {
        // Validação usando LoginValidator
        val (isValid, errorMessage) = com.example.takstud.util.LoginValidator.validateAccessCode(code)
        if (!isValid) {
            _teacherLoginState.value = Result.Error(Exception(errorMessage))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.loginTeacherWithSession(code)
                _teacherLoginState.value = result
                Log.d("LoginViewModel", "Teacher login result: $result")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error during teacher login", e)
                _teacherLoginState.value = Result.Error(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset login states.
     * Call this when leaving the login screen or when user wants to try again.
     */
    fun resetLoginState() {
        _parentLoginState.value = Result.Loading
        _teacherLoginState.value = Result.Loading
        _isLoading.value = false
    }
}
