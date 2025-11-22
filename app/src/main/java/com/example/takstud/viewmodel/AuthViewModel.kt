package com.example.takstud.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.model.Student
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
                remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val secret = remoteConfig.getString("admin_secret").trim()
                        if (secret.isNotEmpty()) {
                            _adminSecret.value = secret
                        } else {
                            _errorMessage.value = "Código admin não configurado. Contate o administrador."
                            Log.e("TakStud", "admin_secret não está configurado no Firebase Remote Config")
                        }
                    } else {
                        Log.e("TakStud", "Falha ao carregar admin_secret do Remote Config", task.exception)
                    }
                }
            } catch (e: Exception) {
                Log.e("TakStud", "Erro ao carregar configuração admin", e)
                _errorMessage.value = "Erro ao carregar configuração. Tente novamente."
            }
        }
    }

    fun onParentLogin(ra: String, navigateToParent: (Student) -> Unit) {
        // TODO: This is not ideal as it collects the flow once. 
        // Ideally we should query the repository for a single student.
        // For now, we'll launch a coroutine to collect the first emission.
        viewModelScope.launch {
            studentRepository.getStudents().collect { students ->
                val student = students.find { it.ra.equals(ra, ignoreCase = true) }
                if (student != null) {
                    navigateToParent(student)
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "RA não encontrado!"
                }
                // We only need to check once, so we can cancel or just return
                return@collect
            }
        }
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}
