package com.example.takstud.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.model.Student
import com.example.takstud.util.ErrorHandler
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val remoteConfig: FirebaseRemoteConfig
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _adminSecret = MutableStateFlow("")
    val adminSecret: StateFlow<String> = _adminSecret.asStateFlow()

    init {
        loadAdminSecret()
    }

    private fun loadAdminSecret() {
        viewModelScope.launch {
            try {
                val configSettings = remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600
                }
                remoteConfig.setConfigSettingsAsync(configSettings)
                remoteConfig.fetchAndActivate().await()
                val secret = remoteConfig.getString("admin_secret").trim()
                if (secret.isNotEmpty()) {
                    _adminSecret.value = secret
                } else {
                    _errorMessage.value = "admin_secret não configurado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar configuração: ${e.message}"
            }
        }
    }

    fun onParentLogin(ra: String, navigateToParent: (Student) -> Unit) {
        viewModelScope.launch {
            try {
                val students = studentRepository.getStudents().first()
                val student = students.find { it.ra.equals(ra, ignoreCase = true) }

                if (student != null) {
                    navigateToParent(student)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "RA não encontrado!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao realizar login: ${e.message}"
            }
        }
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}
