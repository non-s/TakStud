package com.example.takstud.util

/**
 * Representa o resultado de uma operação que pode suceder ou falhar.
 *
 * Padrão Functional: encapsula sucesso ou erro de forma type-safe.
 */
sealed class Result<out T> {
    /**
     * Operação executada com sucesso.
     *
     * @param data Dados retornados pela operação
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Operação falhou com uma exceção.
     *
     * @param exception Exceção que ocorreu
     */
    data class Error(val exception: Exception) : Result<Nothing>()

    /**
     * Operação está em carregamento.
     */
    data object Loading : Result<Nothing>()

    /**
     * Executa um bloco de código quando o resultado é sucesso.
     *
     * @param onSuccess Lambda executada se bem-sucedido
     */
    inline fun onSuccess(onSuccess: (T) -> Unit) {
        if (this is Success) {
            onSuccess(data)
        }
    }

    /**
     * Executa um bloco de código quando o resultado é erro.
     *
     * @param onError Lambda executada em caso de erro
     */
    inline fun onError(onError: (Exception) -> Unit) {
        if (this is Error) {
            onError(exception)
        }
    }

    /**
     * Transforma o valor de sucesso usando uma função.
     *
     * @param transform Função de transformação
     * @return Novo Result com valor transformado
     */
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
            is Loading -> Loading
        }
    }

    /**
     * Retorna o valor de sucesso ou um valor padrão.
     *
     * @param default Valor padrão se não for sucesso
     * @return Valor de sucesso ou padrão
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Verifica se o resultado é sucesso.
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Verifica se o resultado é erro.
     */
    fun isError(): Boolean = this is Error

    /**
     * Verifica se o resultado está em carregamento.
     */
    fun isLoading(): Boolean = this is Loading
}

/**
 * Executa um bloco de código de forma segura, capturando exceções.
 *
 * @param block Bloco de código a executar
 * @return Result com o resultado da operação
 */
inline fun <T> runCatching(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    }
}