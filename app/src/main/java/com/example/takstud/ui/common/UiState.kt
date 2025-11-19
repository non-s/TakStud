package com.example.takstud.ui.common

import androidx.compose.runtime.Stable

/**
 * UiState - Representa o estado da UI de forma type-safe.
 *
 * FUNCIONALIDADES:
 * - Estados bem definidos: Loading, Success, Error
 * - Sem null pointers
 * - Type-safe data access
 * - Facilita composição em Composables
 * - Padrão usado em Google Samples
 *
 * PADRÃO:
 * - Loading: Mostrar progress bar
 * - Success: Mostrar dados
 * - Error: Mostrar mensagem de erro com retry
 *
 * EXEMPLO:
 * when (uiState) {
 *     is UiState.Loading -> CircularProgressIndicator()
 *     is UiState.Success -> MyContent(uiState.data)
 *     is UiState.Error -> ErrorMessage(uiState.message)
 * }
 */

/**
 * Sealed class para representar estado da UI.
 * T é o tipo dos dados em caso de sucesso.
 */
@Stable
sealed class UiState<out T> {
    /**
     * Estado de carregamento.
     * Mostrar loading indicator, spinner, skeleton screen, etc.
     */
    data class Loading<T>(
        val previousData: T? = null,  // Dados anteriores se estiver recarregando
        val message: String = "Carregando..."
    ) : UiState<T>()

    /**
     * Estado de sucesso com dados.
     * T contém os dados para renderizar.
     */
    @Stable
    data class Success<T>(
        val data: T,
        val message: String? = null  // Mensagem de sucesso opcional
    ) : UiState<T>()

    /**
     * Estado de erro.
     * message: mensagem de erro para mostrar
     * exception: exceção original para logging
     * retryable: se o usuário pode fazer retry
     */
    @Stable
    data class Error<T>(
        val message: String,
        val exception: Throwable? = null,
        val retryable: Boolean = true,
        val previousData: T? = null  // Dados anteriores se estiver em refresh
    ) : UiState<T>()

    /**
     * Estado vazio (sem dados ainda).
     * Útil quando nenhum dado foi carregado.
     */
    data class Empty<T>(
        val message: String = "Nenhum dado disponível"
    ) : UiState<T>()
}

// ============== EXTENSÕES ÚTEIS ==============

/**
 * Obtém os dados se estiver em estado Success.
 * Retorna null caso contrário.
 */
fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> this.data
    is UiState.Loading -> this.previousData
    is UiState.Error -> this.previousData
    is UiState.Empty -> null
}

/**
 * Obtém a exceção se estiver em estado Error.
 */
fun <T> UiState<T>.exceptionOrNull(): Throwable? = when (this) {
    is UiState.Error -> this.exception
    else -> null
}

/**
 * Verifica se está carregando.
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Verifica se é sucesso.
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success

/**
 * Verifica se é erro.
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Transforma os dados se estiver em Success.
 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> = when (this) {
    is UiState.Success -> UiState.Success(transform(this.data), this.message)
    is UiState.Loading -> UiState.Loading(null, this.message)
    is UiState.Error -> UiState.Error(this.message, this.exception, this.retryable, null)
    is UiState.Empty -> UiState.Empty(this.message)
}

/**
 * Executa função se Success, retorna UiState.Error caso contrário.
 */
inline fun <T, R> UiState<T>.flatMap(transform: (T) -> UiState<R>): UiState<R> = when (this) {
    is UiState.Success -> transform(this.data)
    is UiState.Loading -> UiState.Loading(null, this.message)
    is UiState.Error -> UiState.Error(this.message, this.exception, this.retryable, null)
    is UiState.Empty -> UiState.Empty(this.message)
}

/**
 * Obtém mensagem apropriada para o estado.
 */
fun <T> UiState<T>.getMessage(): String = when (this) {
    is UiState.Loading -> this.message
    is UiState.Success -> this.message ?: "Carregado com sucesso"
    is UiState.Error -> this.message
    is UiState.Empty -> this.message
}

// ============== FACTORY FUNCTIONS ==============

/**
 * Cria UiState.Success com dados.
 */
fun <T> successUiState(data: T, message: String? = null): UiState<T> =
    UiState.Success(data, message)

/**
 * Cria UiState.Error com mensagem.
 */
fun <T> errorUiState(
    message: String,
    exception: Throwable? = null,
    retryable: Boolean = true
): UiState<T> = UiState.Error(message, exception, retryable)

/**
 * Cria UiState.Loading.
 */
fun <T> loadingUiState(message: String = "Carregando..."): UiState<T> =
    UiState.Loading(null, message)

/**
 * Cria UiState.Empty.
 */
fun <T> emptyUiState(message: String = "Nenhum dado disponível"): UiState<T> =
    UiState.Empty(message)
