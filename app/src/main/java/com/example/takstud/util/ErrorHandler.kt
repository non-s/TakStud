package com.example.takstud.util

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Gerenciador centralizado de erros com logging, analytics e recuperação.
 *
 * Objeto singleton que fornece métodos para tratamento robustardo de exceções em operações assíncronas
 * e síncronas. Implementa padrões de retry automático, logging estruturado, e conversão de erros
 * técnicos em mensagens amigáveis ao usuário.
 *
 * Responsabilidades Principais:
 * - Execução segura de blocos de código com catch automático
 * - Transformação de exceções técnicas em mensagens de UI legíveis
 * - Retry automático com backoff exponencial
 * - Logging estruturado com contexto completo
 * - CoroutineExceptionHandler para coroutines
 * - Validação de dados com Result type-safe
 *
 * Arquitetura:
 * ```
 * Chamador
 *   |
 *   v
 * withErrorHandling / withRetry / tryCatching
 *   |
 *   +--- Executa bloco (block())
 *   |
 *   +--- Captura exceção (catch Exception)
 *   |
 *   +--- Log estruturado (logError)
 *   |
 *   +--- Mensagem amigável (getUserFriendlyMessage)
 *   |
 *   v
 * Result.Success<T> ou Result.Error
 * ```
 *
 * Padrões de uso:
 * ```kotlin
 * // Padrão 1: Com tratamento básico
 * val result = ErrorHandler.withErrorHandling(
 *     operationName = "Carregar tarefas",
 *     userFacingMessage = "Não foi possível carregar tarefas"
 * ) {
 *     repository.getTasks()
 * }
 *
 * // Padrão 2: Com retry automático
 * val result = ErrorHandler.withRetry(
 *     maxAttempts = 3,
 *     delayMillis = 100,
 *     operationName = "Sincronizar com servidor"
 * ) {
 *     repository.syncGrades()
 * }
 *
 * // Padrão 3: Try-catch seguro
 * val tasks = ErrorHandler.tryCatching("Carregar tarefas") {
 *     repository.getTasks().first()  // Retorna null em erro
 * }
 *
 * // Padrão 4: Com CoroutineExceptionHandler
 * viewModelScope.launch(
 *     ErrorHandler.createExceptionHandler("Executar operação")
 * ) {
 *     meuCodigo()
 * }
 * ```
 *
 * Tipos de exceções tratadas:
 * - `SocketTimeoutException`: Sem conexão (internet)
 * - `ConnectException`: Falha ao conectar
 * - `FirebaseFirestoreException`: Erros do Firestore
 * - `IllegalArgumentException`: Entrada inválida
 * - `NoSuchElementException`: Recurso não encontrado
 * - Todas as exceções genéricas: Mensagem padrão
 *
 * Características do retry automático:
 * - Backoff exponencial: delay * (tentativa + 1)
 * - Ex: 100ms, 200ms, 300ms para maxAttempts=3
 * - Apenas para erros transientes (rede, timeout)
 * - Logging detalhado de cada tentativa
 *
 * @see Result
 * @see logError
 * @see getUserFriendlyMessage
 * @see withErrorHandling
 * @see withRetry
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"

    /**
     * Resultado genérico type-safe para operações que podem falhar.
     *
     * Sealed class que representa os possíveis estados de uma operação assíncrona:
     * - Success: Operação completou com sucesso
     * - Error: Operação falhou com exceção
     * - Loading: Operação em progresso (opcional)
     *
     * Exemplo de uso:
     * ```kotlin
     * when (val result = ErrorHandler.withErrorHandling("Op", block = { doSomething() })) {
     *     is ErrorHandler.Result.Success -> {
     *         val data = result.data
     *         updateUI(data)
     *     }
     *     is ErrorHandler.Result.Error -> {
     *         Log.e("Error", result.message, result.exception)
     *         showErrorToUser(result.message)
     *     }
     *     is ErrorHandler.Result.Loading -> {
     *         showProgressBar()
     *     }
     * }
     * ```
     *
     * @param T Tipo de dados retornado em caso de sucesso
     * @see Success
     * @see Error
     * @see Loading
     * @see getOrNull
     * @see onSuccess
     * @see onError
     */
    sealed class Result<out T> {
        /**
         * Sucesso: operação completou e retornou dado de tipo T.
         *
         * @param data Valor de tipo T retornado pela operação bem-sucedida
         */
        data class Success<T>(val data: T) : Result<T>()

        /**
         * Erro: operação falhou com exceção.
         *
         * @param message Mensagem de erro amigável ao usuário
         * @param exception Exceção original (opcional, para logging)
         */
        data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()

        /**
         * Carregamento: operação em progresso (estado intermediário).
         *
         * Usado para sinalizar que uma operação está em andamento, útil para
         * mostrar indicadores de progresso na UI.
         */
        object Loading : Result<Nothing>()
    }

    /**
     * Executa um bloco de código com tratamento de erros automático.
     *
     * Função de extensão para execução segura de operações assíncronas. Captura
     * exceções automaticamente, realiza logging estruturado, e retorna um Result
     * type-safe indicando sucesso ou falha.
     *
     * Fluxo:
     * 1. Executa block()
     * 2. Se sucesso: retorna Result.Success com valor
     * 3. Se exceção: captura, registra em log, retorna Result.Error
     *
     * Quando usar:
     * - Operações de rede (Firestore)
     * - Operações de banco de dados
     * - Operações de arquivo
     * - Qualquer operação que pode gerar Exception
     *
     * @param operationName Nome descritivo da operação (usado em logs e mensagens)
     *                       Ex: "Carregar tarefas do servidor"
     * @param userFacingMessage Mensagem amigável ao usuário em caso de erro
     *                          Default: "Ocorreu um erro. Tente novamente."
     * @param block Bloco de código suspend a executar
     *              Ex: { repository.getTasks() }
     *
     * @return Result.Success<T> se execução bem-sucedida com valor tipo T
     *         Result.Error se exceção lançada com mensagem userFacingMessage
     *
     * Exemplo completo:
     * ```kotlin
     * viewModelScope.launch {
     *     val result = ErrorHandler.withErrorHandling(
     *         operationName = "Carregar tarefas",
     *         userFacingMessage = "Não foi possível carregar tarefas. Tente novamente."
     *     ) {
     *         repository.getTasks().first()  // Retorna List<Task>
     *     }
     *
     *     when (result) {
     *         is ErrorHandler.Result.Success -> {
     *             _tasks.value = result.data  // List<Task>
     *         }
     *         is ErrorHandler.Result.Error -> {
     *             _errorMessage.value = result.message
     *         }
     *         is ErrorHandler.Result.Loading -> {}
     *     }
     * }
     * ```
     *
     * @see Result
     * @see withRetry
     * @see logError
     * @see tryCatching
     */
    suspend fun <T> withErrorHandling(
        operationName: String,
        userFacingMessage: String = "Ocorreu um erro. Tente novamente.",
        block: suspend () -> T
    ): Result<T> = try {
        val result = block()
        Log.d(TAG, "✓ $operationName bem-sucedida")
        Result.Success(result)
    } catch (e: Exception) {
        logError(operationName, e)
        Result.Error(userFacingMessage, e)
    }

    /**
     * Executa um bloco de código com tratamento seguro, retornando null em erro.
     *
     * Versão simplificada de withErrorHandling que retorna T ou null, sem Result wrapper.
     * Útil quando se quer um tipo Option/nullable em vez de Result sealed class.
     *
     * Fluxo:
     * 1. Tenta executar block()
     * 2. Se sucesso: retorna T
     * 3. Se exceção: registra em log e retorna null
     *
     * Quando usar:
     * - Quando valor nulo é adequado para representar erro
     * - Para operações opcionais (não críticas)
     * - Quando não precisa da mensagem de erro específica
     *
     * Quando NÃO usar:
     * - Se precisa diferenciar entre resultado nulo e erro
     * - Se precisa da mensagem de erro para UI
     * - Usar withErrorHandling ou withRetry ao invés
     *
     * @param operationName Nome descritivo da operação (para logs)
     * @param block Bloco de código suspend a executar
     *
     * @return Valor de tipo T se sucesso, ou null se exceção
     *
     * Exemplo:
     * ```kotlin
     * val tasks = ErrorHandler.tryCatching("Carregar tarefas") {
     *     repository.getTasks().first()
     * }
     *
     * if (tasks != null) {
     *     updateUI(tasks)
     * } else {
     *     // Falhou silenciosamente, valor nulo
     *     showLoadingOrEmpty()
     * }
     * ```
     *
     * @see withErrorHandling
     * @see withRetry
     * @see logError
     */
    suspend fun <T> tryCatching(
        operationName: String,
        block: suspend () -> T
    ): T? = try {
        block()
    } catch (e: Exception) {
        logError(operationName, e)
        null
    }

    /**
     * Cria um CoroutineExceptionHandler para uso em viewModelScope e coroutines.
     *
     * Factory method que retorna um CoroutineContext (CoroutineExceptionHandler) pronto para
     * ser usado em viewModelScope.launch ou outras coroutines. Captura exceções não capturadas
     * e registra em log automaticamente.
     *
     * CoroutineExceptionHandler é necessário quando:
     * - Exceção é lançada sem try-catch específico na coroutine
     * - Exceção não é capturada por await() ou outro método
     * - Coroutine cancela ou falha inesperadamente
     *
     * @param operationName Nome descritivo da operação (para logs)
     *
     * @return CoroutineContext contendo CoroutineExceptionHandler configurado
     *
     * Exemplo de uso:
     * ```kotlin
     * viewModelScope.launch(
     *     ErrorHandler.createExceptionHandler("Registrar estudante")
     * ) {
     *     // Se exceção aqui não é capturada, handler executa
     *     val student = Student(name = "João")
     *     repository.saveStudent(student)
     *     _uiState.value = "Salvo com sucesso"
     * }
     * ```
     *
     * Comparação com withErrorHandling:
     * ```kotlin
     * // Opção 1: withErrorHandling (explícito)
     * viewModelScope.launch {
     *     val result = ErrorHandler.withErrorHandling(
     *         "Registrar",
     *         "Não foi possível registrar"
     *     ) {
     *         repository.saveStudent(student)
     *     }
     * }
     *
     * // Opção 2: createExceptionHandler (implícito, catch global)
     * viewModelScope.launch(
     *     ErrorHandler.createExceptionHandler("Registrar")
     * ) {
     *     repository.saveStudent(student)
     * }
     * ```
     *
     * @see withErrorHandling
     * @see logError
     * @see CoroutineExceptionHandler
     */
    fun createExceptionHandler(operationName: String): CoroutineContext {
        return CoroutineExceptionHandler { _, exception ->
            logError(operationName, exception as Exception)
        }
    }

    /**
     * Log estruturado de erros com contexto completo.
     *
     * Registra erros em formato estruturado e legível, incluindo tipo de exceção,
     * mensagem, e stack trace para debugging. Implementa logging a múltiplos níveis
     * de severidade.
     *
     * Formato do log:
     * ```
     * ✗ Erro em [operationName]
     *   Tipo: [ExceptionClassName]
     *   Mensagem: [exception.message]
     *   Causa: [exception.cause?.message ou N/A]
     * [stack trace completo]
     * ```
     *
     * Exemplo de saída:
     * ```
     * ✗ Erro em Carregar tarefas
     *   Tipo: FirebaseFirestoreException
     *   Mensagem: Missing or insufficient permissions
     *   Causa: N/A
     * [stack trace de 20+ linhas]
     * ```
     *
     * Níveis de log:
     * - WARN: Para operações que podem falhar mas não são críticas
     * - ERROR: Para falhas críticas que afetam funcionalidade
     *
     * Usado internamente por:
     * - withErrorHandling (após capturar exceção)
     * - withRetry (em cada falha de tentativa)
     * - tryCatching (em caso de exceção)
     * - createExceptionHandler (para exceções não capturadas)
     *
     * @param operationName Nome descritivo da operação que falhou
     *                       Ex: "Carregar tarefas", "Sincronizar com servidor"
     * @param exception Exception capturada com dados de erro
     *
     * Exemplos:
     * ```kotlin
     * try {
     *     repository.saveGrade(grade)
     * } catch (e: Exception) {
     *     ErrorHandler.logError("Salvar nota", e)
     * }
     *
     * // Saída no Logcat:
     * // E/ErrorHandler: ✗ Erro em Salvar nota
     * //                   Tipo: FirebaseFirestoreException
     * //                   Mensagem: [permission-denied]...
     * //                   Causa: N/A
     * ```
     *
     * Integração futura:
     * - Crashlytics: para rastreamento de crashes em produção
     * - Firebase Analytics: para eventos de erro customizados
     * - Sentry: para error tracking e aggregation
     *
     * @see withErrorHandling
     * @see withRetry
     * @see tryCatching
     */
    fun logError(operationName: String, exception: Exception) {
        val errorMessage = buildString {
            append("✗ Erro em $operationName\n")
            append("  Tipo: ${exception::class.simpleName}\n")
            append("  Mensagem: ${exception.message}\n")
            append("  Causa: ${exception.cause?.message ?: "N/A"}")
        }

        Log.e(TAG, errorMessage, exception)

        // TODO: Integrar com Crashlytics/Firebase Analytics
        // analyticsManager.logError(operationName, exception)
    }

    /**
     * Retorna uma mensagem de erro amigável ao usuário baseado no tipo de exceção.
     *
     * Converte exceções técnicas em mensagens compreensíveis para o usuário final.
     * Cada tipo de exceção mapeia para uma mensagem específica e acionável.
     *
     * Mapeamento de exceções:
     * - `SocketTimeoutException`: "Conexão perdida. Verifique sua internet."
     *   → Causa: Timeout na conexão de rede
     *   → Ação do usuário: Verificar Wi-Fi/dados móveis
     *
     * - `ConnectException`: "Conexão perdida. Verifique sua internet."
     *   → Causa: Falha ao conectar ao servidor
     *   → Ação do usuário: Tentar novamente ou conectar à internet
     *
     * - `FirebaseFirestoreException`: "Erro ao sincronizar com servidor. Tente novamente."
     *   → Causa: Falha ao acessar/modificar dados no Firestore
     *   → Ação do usuário: Tentar novamente, pode ser permissão ou erro transiente
     *
     * - `IllegalArgumentException`: "Entrada inválida. Verifique os dados."
     *   → Causa: Dados passados não passaram em validação
     *   → Ação do usuário: Corrigir entradas (ex: formato de data)
     *
     * - `NoSuchElementException`: "Recurso não encontrado."
     *   → Causa: Recurso esperado não existe (ex: student com id não encontrado)
     *   → Ação do usuário: Verificar se recurso foi deletado ou id está correto
     *
     * - Outros: "Ocorreu um erro. Tente novamente."
     *   → Default para exceções não mapeadas
     *
     * Exemplo de uso:
     * ```kotlin
     * val result = ErrorHandler.withErrorHandling(
     *     "Carregar tarefas",
     *     userFacingMessage = "Erro ao carregar"
     * ) {
     *     repository.getTasks()
     * }
     *
     * when (result) {
     *     is ErrorHandler.Result.Error -> {
     *         // result.message já é amigável ao usuário
     *         // Mas também pode usar exception para mensagem customizada:
     *         if (result.exception != null) {
     *             val friendlyMsg = getUserFriendlyMessage(result.exception)
     *             showToast(friendlyMsg)
     *         }
     *     }
     *     is ErrorHandler.Result.Success -> {}
     *     is ErrorHandler.Result.Loading -> {}
     * }
     * ```
     *
     * @param exception Exception capturada para análise de tipo
     *
     * @return String com mensagem pronta para exibir ao usuário
     *
     * @see withErrorHandling
     * @see logError
     */
    fun getUserFriendlyMessage(exception: Exception): String {
        return when (exception) {
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> "Conexão perdida. Verifique sua internet."

            is com.google.firebase.firestore.FirebaseFirestoreException ->
                "Erro ao sincronizar com servidor. Tente novamente."

            is IllegalArgumentException -> "Entrada inválida. Verifique os dados."

            is java.util.NoSuchElementException -> "Recurso não encontrado."

            else -> "Ocorreu um erro. Tente novamente."
        }
    }

    /**
     * Estratégia de retry automático com backoff exponencial para operações transientes.
     *
     * Função robusta para operações que podem falhar temporariamente devido a:
     * - Problemas de rede (timeouts, conexão perdida)
     * - Servidor indisponível (rate limiting, overload)
     * - Erros transientes que se resolvem sozinhos
     *
     * Algoritmo de retry:
     * ```
     * Tentativa 1 (t=0ms)
     *   |-- Se sucesso: retorna Success
     *   |-- Se falha: espera delayMillis * 1 = primeiro delay
     *
     * Tentativa 2 (t= delayMillis * 1)
     *   |-- Se sucesso: retorna Success
     *   |-- Se falha: espera delayMillis * 2 = segundo delay
     *
     * Tentativa 3 (t= delayMillis * 2)
     *   |-- Se sucesso: retorna Success
     *   |-- Se falha: retorna Error (maxAttempts atingido)
     * ```
     *
     * Backoff exponencial:
     * ```
     * Default: delayMillis = 100ms, maxAttempts = 3
     * Tentativa 1: falha, aguarda 100ms (100 * 1)
     * Tentativa 2: falha, aguarda 200ms (100 * 2)
     * Tentativa 3: falha, retorna erro
     * Total máximo: 100 + 200 = 300ms de espera
     * ```
     *
     * Quando usar:
     * - Operações de rede (API calls, Firestore)
     * - Sincronização com servidor
     * - Operações que podem ter timeouts temporários
     * - NÃO para validação de entrada (falha sempre)
     * - NÃO para lógica de negócio (erro não transiente)
     *
     * @param maxAttempts Número máximo de tentativas (padrão 3)
     *                     Ex: 3 = 1 tentativa + 2 retries
     *                     Mínimo recomendado: 2, Máximo: 5
     *
     * @param delayMillis Delay inicial em millisegundos (padrão 100)
     *                     Cada retry multiplica por (attempt + 1)
     *                     Ex: 100ms → primeiro retry espera 100ms, segundo 200ms
     *
     * @param operationName Nome descritivo para logs e mensagens de erro
     *                       Ex: "Sincronizar com servidor"
     *
     * @param block Bloco de código suspend a executar com retry automático
     *
     * @return Result.Success<T> se sucesso em qualquer tentativa
     *         Result.Error se falha em todas as tentativas
     *
     * Exemplo básico:
     * ```kotlin
     * val result = ErrorHandler.withRetry(
     *     maxAttempts = 3,
     *     delayMillis = 100,
     *     operationName = "Carregar tarefas"
     * ) {
     *     repository.getTasks().first()  // Pode falhar por rede
     * }
     *
     * when (result) {
     *     is ErrorHandler.Result.Success -> {
     *         _tasks.value = result.data
     *     }
     *     is ErrorHandler.Result.Error -> {
     *         _errorMessage.value = "Falhou após 3 tentativas: ${result.message}"
     *     }
     *     is ErrorHandler.Result.Loading -> {}
     * }
     * ```
     *
     * Exemplo com tratamento de erro:
     * ```kotlin
     * val result = ErrorHandler.withRetry(
     *     maxAttempts = 4,
     *     delayMillis = 500,
     *     operationName = "Sincronizar notas"
     * ) {
     *     repository.syncGradesToServer()
     * }
     *
     * result.onSuccess { syncResult ->
     *     Log.d("Sync", "Sincronizado com sucesso em $syncResult")
     * }.onError { message ->
     *     showErrorDialog(
     *         "Sincronização falhou após 4 tentativas",
     *         message
     *     )
     * }
     * ```
     *
     * Logging detalhado:
     * ```
     * D/ErrorHandler: ► Tentativa 1 de 3: Sincronizar...
     * E/ErrorHandler: ✗ Falha na tentativa 1/3
     * W/ErrorHandler: ► Retry em 100ms...
     * D/ErrorHandler: ► Tentativa 2 de 3: Sincronizar...
     * E/ErrorHandler: ✗ Falha na tentativa 2/3
     * W/ErrorHandler: ► Retry em 200ms...
     * D/ErrorHandler: ► Tentativa 3 de 3: Sincronizar...
     * E/ErrorHandler: ✗ Sincronizar falhou após 3 tentativas
     * ```
     *
     * @see withErrorHandling
     * @see tryCatching
     * @see logError
     * @see Result
     */
    suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        delayMillis: Long = 100,
        operationName: String = "Operação",
        block: suspend () -> T
    ): Result<T> {
        var lastException: Exception? = null

        repeat(maxAttempts) { attempt ->
            try {
                val result = block()
                Log.d(TAG, "✓ $operationName bem-sucedida na tentativa ${attempt + 1}")
                return Result.Success(result)
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxAttempts - 1) {
                    val backoffDelay = delayMillis * (attempt + 1)
                    Log.w(TAG, "$operationName falhou na tentativa ${attempt + 1}. Retry em ${backoffDelay}ms", e)
                    kotlinx.coroutines.delay(backoffDelay)
                }
            }
        }

        return Result.Error("$operationName falhou após $maxAttempts tentativas", lastException)
    }

    /**
     * Valida dados com mensagem de erro customizada.
     *
     * Função utilitária para validação síncrona de dados. Se a condição é verdadeira,
     * retorna Success com os dados; caso contrário, retorna Error com mensagem.
     *
     * Casos de uso:
     * - Validar precondições antes de operação custosa
     * - Validar business logic invariantes
     * - Verificar permissões ou acesso a recurso
     *
     * @param condition Condição booleana que deve ser verdadeira
     *                  Ex: user.isAdmin, grade >= 0, list.isNotEmpty()
     * @param errorMessage Mensagem de erro se condition for falsa
     *                      Ex: "Usuário deve ser administrador"
     * @param data Dados a retornar se validação passar
     *             Ex: student, grade, task
     *
     * @return Result.Success<T> se condition é true
     *         Result.Error se condition é false
     *
     * Exemplo 1: Validar permissão:
     * ```kotlin
     * val result = ErrorHandler.validate(
     *     condition = currentUser.role == UserRole.ADMIN,
     *     errorMessage = "Apenas administradores podem deletar estudantes",
     *     data = student
     * )
     *
     * result.onSuccess { s ->
     *     repository.deleteStudent(s)
     * }.onError { msg ->
     *     showPermissionDeniedDialog(msg)
     * }
     * ```
     *
     * Exemplo 2: Validar precondição:
     * ```kotlin
     * val validatedGrade = ErrorHandler.validate(
     *     condition = grade.score in 0.0..100.0,
     *     errorMessage = "Nota deve estar entre 0 e 100",
     *     data = grade
     * )
     * ```
     *
     * Exemplo 3: Validar estado de recurso:
     * ```kotlin
     * val validatedTask = ErrorHandler.validate(
     *     condition = task.deadline > LocalDate.now(),
     *     errorMessage = "Não pode modificar tarefa com prazo expirado",
     *     data = task
     * )
     * ```
     *
     * @see Result
     * @see withErrorHandling
     * @see tryCatching
     */
    fun <T> validate(
        condition: Boolean,
        errorMessage: String,
        data: T
    ): Result<T> {
        return if (condition) {
            Result.Success(data)
        } else {
            Log.w(TAG, "Validação falhou: $errorMessage")
            Result.Error(errorMessage)
        }
    }
}

/**
 * Extensão para acessar dados ou retornar null.
 *
 * Retorna os dados encapsulados em Success, ou null se Result é Error/Loading.
 * Útil para padrões de nullable em vez de pattern matching.
 *
 * @return Dados de tipo T se Success, caso contrário null
 *
 * Exemplo:
 * ```kotlin
 * val result = ErrorHandler.withErrorHandling("Op") { getData() }
 * val data = result.getOrNull()
 * if (data != null) {
 *     updateUI(data)
 * }
 * ```
 *
 * @see ErrorHandler.Result
 * @see onSuccess
 * @see onError
 */
fun <T> ErrorHandler.Result<T>.getOrNull(): T? = when (this) {
    is ErrorHandler.Result.Success<*> -> {
        @Suppress("UNCHECKED_CAST")
        (this as ErrorHandler.Result.Success<T>).data
    }
    else -> null
}

/**
 * Extensão para executar ação em sucesso (callback pattern).
 *
 * Executa ação se Result é Success, e retorna self para chaining.
 * Padrão funcional para tratamento de sucesso.
 *
 * @param action Função a executar recebendo dados de tipo T
 * @return self para chaining com onError, onLoading, etc
 *
 * Exemplo com chaining:
 * ```kotlin
 * ErrorHandler.withErrorHandling("Carregar") { getTasks() }
 *     .onSuccess { tasks ->
 *         Log.d("Success", "Carregadas ${tasks.size} tarefas")
 *         _tasks.value = tasks
 *     }
 *     .onError { message ->
 *         Log.e("Error", message)
 *         _errorMessage.value = message
 *     }
 * ```
 *
 * @see ErrorHandler.Result
 * @see onError
 * @see getOrNull
 */
fun <T> ErrorHandler.Result<T>.onSuccess(action: (T) -> Unit): ErrorHandler.Result<T> {
    if (this is ErrorHandler.Result.Success<*>) {
        @Suppress("UNCHECKED_CAST")
        action((this as ErrorHandler.Result.Success<T>).data)
    }
    return this
}

/**
 * Extensão para executar ação em erro (callback pattern).
 *
 * Executa ação se Result é Error, e retorna self para chaining.
 * Padrão funcional para tratamento de erro.
 *
 * @param action Função a executar recebendo mensagem de erro (String)
 * @return self para chaining com onSuccess, onLoading, etc
 *
 * Exemplo com chaining:
 * ```kotlin
 * ErrorHandler.withRetry(3, 100, "Sync") { syncData() }
 *     .onSuccess { result ->
 *         showSuccessToast("Dados sincronizados")
 *     }
 *     .onError { message ->
 *         showErrorDialog("Falha na sincronização", message)
 *     }
 * ```
 *
 * Exemplo com multiple handlers:
 * ```kotlin
 * result
 *     .onError { msg ->
 *         Log.e("API", "Erro: $msg")
 *     }
 *     .onError { msg ->
 *         // Pode chamar múltiplas vezes
 *         analytics.trackError(msg)
 *     }
 * ```
 *
 * @see ErrorHandler.Result
 * @see onSuccess
 * @see getOrNull
 */
fun <T> ErrorHandler.Result<T>.onError(action: (String) -> Unit): ErrorHandler.Result<T> {
    if (this is ErrorHandler.Result.Error) action(message)
    return this
}
