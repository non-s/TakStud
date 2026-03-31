package com.example.takstud.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * BaseViewModel - ViewModel base com UiState management.
 *
 * Fornece padrão comum para todos ViewModels:
 * - _uiState: MutableStateFlow interno
 * - uiState: StateFlow público (read-only)
 * - setLoading(), setSuccess(), setError()
 * - Helpers para operações async com tratamento de erro
 *
 * EXEMPLO DE USO:
 * class StudentListViewModel : BaseViewModel<List<Student>>() {
 *     fun loadStudents() = launchUI {
 *         val students = repository.getStudents()
 *         setSuccess(students)
 *     }
 * }
 */

/**
 * Base ViewModel genérico para qualquer tela.
 * T é o tipo dos dados que a tela mostra.
 */
abstract class BaseViewModel<T> : ViewModel() {

    // Estado interno mutável
    protected val _uiState = MutableStateFlow<UiState<T>>(UiState.Empty())

    // Exposição pública read-only
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    // ============== SETTERS DO ESTADO ==============

    /**
     * Muda estado para Loading.
     */
    protected fun setLoading(previousData: T? = null, message: String = "Carregando...") {
        _uiState.value = UiState.Loading(previousData, message)
    }

    /**
     * Muda estado para Success com dados.
     */
    protected fun setSuccess(data: T, message: String? = null) {
        _uiState.value = UiState.Success(data, message)
    }

    /**
     * Muda estado para Error.
     */
    protected fun setError(
        message: String,
        exception: Throwable? = null,
        retryable: Boolean = true,
        previousData: T? = null
    ) {
        _uiState.value = UiState.Error(message, exception, retryable, previousData)
    }

    /**
     * Muda estado para Empty.
     */
    protected fun setEmpty(message: String = "Nenhum dado disponível") {
        _uiState.value = UiState.Empty(message)
    }

    // ============== HELPERS PARA OPERAÇÕES ASYNC ==============

    /**
     * Executa bloco de código com tratamento automático de Loading/Success/Error.
     *
     * EXEMPLO:
     * fun loadGrades(studentId: String) = launchUI {
     *     val grades = repository.getGradesByStudent(studentId)
     *     setSuccess(grades)  // Automático: Loading -> Success/Error
     * }
     *
     * Se exception: automático -> Error
     */
    protected fun launchUI(
        loadingMessage: String = "Carregando...",
        block: suspend () -> Unit
    ) = viewModelScope.launch {
        try {
            setLoading(null, loadingMessage)
            block()
        } catch (e: Exception) {
            setError("Erro ao carregar dados", e)
        }
    }

    /**
     * Executa bloco e automaticamente chama setSuccess() com resultado.
     *
     * EXEMPLO:
     * fun loadStudents() = withUI { repository.getStudents() }
     *
     * Reduz boilerplate: não precisa chamar setSuccess manualmente
     */
    protected fun <R> withUI(
        loadingMessage: String = "Carregando...",
        block: suspend () -> R
    ) = viewModelScope.launch {
        try {
            setLoading(null, loadingMessage)
            val result = block()
            @Suppress("UNCHECKED_CAST")
            setSuccess(result as T)
        } catch (e: Exception) {
            setError("Erro ao carregar dados", e)
        }
    }

    /**
     * Executa bloco com previous data mantido (para refresh).
     *
     * ÚTIL: Quando user faz "Pull to Refresh"
     * - Mantém dados antigos visíveis durante reload
     * - Muda para Loading (com dados anteriores)
     * - Ao terminar, mostra novos dados
     *
     * EXEMPLO:
     * fun refreshGrades() = launchRefresh { repository.getGradesByStudent(studentId) }
     */
    protected fun <R> launchRefresh(
        loadingMessage: String = "Atualizando...",
        block: suspend () -> R
    ) = viewModelScope.launch {
        try {
            val currentData = (_uiState.value as? UiState.Success<T>)?.data
            setLoading(currentData, loadingMessage)
            val result = block()
            @Suppress("UNCHECKED_CAST")
            setSuccess(result as T)
        } catch (e: Exception) {
            val previousData = (_uiState.value as? UiState.Loading<T>)?.previousData
            setError("Erro ao atualizar", e, retryable = true, previousData)
        }
    }

    /**
     * Executa operação sem gerenciar estado (para side effects).
     *
     * EXEMPLO:
     * fun deleteGrade(gradeId: String) = launchSideEffect {
     *     repository.deleteGrade(gradeId)
     * }
     */
    protected fun launchSideEffect(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: Exception) {
            // Log mas não muda UI state (para operations não relacionadas à listagem)
            e.printStackTrace()
        }
    }

    // ============== UTILITY METHODS ==============

    /**
     * Obtém os dados atuais se estiver em Success.
     */
    fun getCurrentData(): T? = _uiState.value.getOrNull()

    /**
     * Verifica se está carregando.
     */
    fun isLoading(): Boolean = _uiState.value.isLoading()

    /**
     * Verifica se é erro.
     */
    fun isError(): Boolean = _uiState.value.isError()

    /**
     * Retry manual (para quando user clica "Tentar Novamente").
     * Sobrescrever em subclasses para definir o que fazer no retry.
     */
    open fun retry() {
        // Override em subclasses
    }
}

/**
 * Interface para ViewModels que suportam retry.
 * Implementar em ViewModels que podem fazer retry.
 */
interface Retryable {
    fun retry()
}
