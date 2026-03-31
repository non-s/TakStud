package com.example.takstud.util

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * FirestoreFlowHelper - Eliminação de duplicação de callbackFlow em Repository.
 *
 * Módulo de utilidade que fornece funções genéricas para criar Flows reativos
 * a partir de coleções e queries do Firestore. Reduz duplicação de código através
 * de funções genéricas type-safe com tratamento de erros consistente.
 *
 * Problema resolvido:
 * - Antes: 6+ funções quase idênticas (getTasks, getNotices, getGrades, etc)
 *   Cada uma com seu próprio callbackFlow, listener management, error handling
 *   Total: ~300+ linhas de código duplicado
 *
 * - Depois: 2 funções genéricas (firestoreCollectionFlow, firestoreQueryFlow)
 *   + 1 utilidade (copyIdToModel)
 *   Total: ~130 linhas reutilizáveis
 *
 * - Redução: ~70% menos código no Repository
 *
 * Funcionalidades:
 * - Funções genéricas para qualquer tipo T
 * - Conversão automática DocumentSnapshot → Modelo (via toObject)
 * - Copy reflexivo de ID do documento para campo 'id' do modelo
 * - Tratamento consistente de erros Firestore
 * - Logging estruturado com custom tags
 * - Suporte para coleções e queries (com filtros)
 * - Cleanup automático de listeners via awaitClose
 *
 * Padrão anterior (duplicado):
 * ```kotlin
 * fun getTasks(): Flow<List<Task>> = callbackFlow {
 *     val listener = db.collection("tasks").addSnapshotListener { snapshots, e ->
 *         if (e != null) {
 *             Log.w(TAG, "Erro getTasks", e)
 *             close(e)
 *             return@addSnapshotListener
 *         }
 *         val items = snapshots?.mapNotNull { doc ->
 *             try {
 *                 doc.toObject(Task::class.java)
 *             } catch (e: Exception) {
 *                 Log.e(TAG, "Erro converter Task", e)
 *                 null
 *             }
 *         } ?: emptyList()
 *         trySend(items)
 *     }
 *     awaitClose { listener.remove() }
 * }
 * // Repetir para getTasks, getGrades, getNotices, getSchedules, getStudents...
 * ```
 *
 * Padrão novo (reutilizável):
 * ```kotlin
 * fun getTasks() = firestoreCollectionFlow(tasksCollection, Task::class.java)
 * fun getGrades() = firestoreCollectionFlow(gradesCollection, Grade::class.java)
 * fun getNotices() = firestoreCollectionFlow(noticesCollection, Notice::class.java)
 * // ... todas as 6+ funções em 1 linha cada
 * ```
 *
 * @see firestoreCollectionFlow
 * @see firestoreQueryFlow
 * @see copyIdToModel
 * @see FirestoreFlowResult
 */

/**
 * Cria um Flow reativo a partir de uma coleção Firestore.
 *
 * Função genérica que encapsula o padrão completo de listener no Firestore:
 * 1. Registra SnapshotListener na coleção
 * 2. Converte DocumentSnapshots para modelos T via toObject()
 * 3. Copia ID do documento para campo 'id' do modelo (se existir)
 * 4. Emite lista de T sempre que dados mudam
 * 5. Trata erros Firestore e problemas de conversão
 * 6. Remove listener automaticamente quando Flow é cancelado
 *
 * Fluxo de dados:
 * ```
 * Firestore (coleção)
 *   |
 *   v
 * SnapshotListener (registra)
 *   |
 *   v
 * DocumentSnapshot[] (recebe)
 *   |
 *   +-> toObject(modelClass) → T
 *   +-> copyIdToModel(item, doc.id)
 *   |
 *   v
 * List<T> (emite via trySend)
 *   |
 *   v
 * Flow.collect { items: List<T> -> ... }
 * ```
 *
 * Características:
 * - Emissões reativas: sempre que dados mudam no Firestore
 * - Type-safe: genérico para qualquer modelo T
 * - Erro handling: erros de conversão geram logs, não quebram flow
 * - Memory safe: listener é removido automaticamente com awaitClose
 * - Logging: todos os eventos logados com tag customizável
 *
 * @param collection CollectionReference do Firestore (ex: db.collection("tasks"))
 * @param modelClass Classe do modelo para deserialização (ex: Task::class.java)
 *                   Deve ter construtor sem-argumentos (compatível com Firestore)
 * @param logTag Tag customizável para Log.d/e (default: "FirestoreFlow")
 *
 * @return Flow<List<T>> que emite List<T> sempre que dados mudam
 *         - Inicia vazio quando listener é registrado
 *         - Emite sempre que Firestore envia update
 *         - Fechado automaticamente quando collector desinscreve
 *         - Erros são logados e não emitidos (continua funcionando)
 *
 * Exemplo básico:
 * ```kotlin
 * @Inject lateinit var db: FirebaseFirestore
 *
 * fun getTasks(): Flow<List<Task>> {
 *     return firestoreCollectionFlow(
 *         db.collection("tasks"),
 *         Task::class.java
 *     )
 * }
 *
 * // Usar em ViewModel:
 * viewModelScope.launch {
 *     repository.getTasks().collect { tasks ->
 *         _tasks.value = tasks  // Atualiza sempre que Firestore muda
 *     }
 * }
 * ```
 *
 * Exemplo com filtros de coleta:
 * ```kotlin
 * // Filtrar e mapear
 * repository.getTasks()
 *     .map { tasks -> tasks.filter { it.isActive } }
 *     .distinctUntilChanged()
 *     .collect { activeTasks ->
 *         _activeTasks.value = activeTasks
 *     }
 *
 * // Combinar múltiplos flows
 * combine(
 *     repository.getTasks(),
 *     repository.getStudents()
 * ) { tasks, students ->
 *     Pair(tasks, students)
 * }.collect { (tasks, students) ->
 *     updateUI(tasks, students)
 * }
 * ```
 *
 * @see firestoreQueryFlow
 * @see copyIdToModel
 * @see FirestoreFlowResult
 */
inline fun <reified T> firestoreCollectionFlow(
    collection: CollectionReference,
    modelClass: Class<T>,
    logTag: String = "FirestoreFlow"
): Flow<List<T>> = callbackFlow {
    Log.d(logTag, "Iniciando listener para ${collection.path}")

    val listener = collection.addSnapshotListener { snapshots, e ->
        if (e != null) {
            // Erro transitório — não fechar o flow, apenas logar
            Log.w(logTag, "Erro ao ouvir ${collection.path} (mantendo listener ativo)", e)
            return@addSnapshotListener
        }

        try {
            val items = snapshots?.mapNotNull { doc ->
                try {
                    val item = doc.toObject(modelClass)
                    copyIdToModel(item, doc.id)
                    item
                } catch (e: Exception) {
                    Log.e(logTag, "Erro ao converter documento ${doc.id}", e)
                    null
                }
            } ?: emptyList()

            Log.d(logTag, "✓ ${collection.path}: ${items.size} itens")
            trySend(items)
        } catch (e: Exception) {
            Log.e(logTag, "Erro ao processar snapshot de ${collection.path}", e)
        }
    }

    awaitClose {
        Log.d(logTag, "Removendo listener de ${collection.path}")
        listener.remove()
    }
}

/**
 * Cria um Flow reativo a partir de uma Query Firestore (coleção com filtros).
 *
 * Variação de firestoreCollectionFlow que funciona com Queries, permitindo
 * adicionar filtros (where, orderBy, limit, etc) antes de criar o Flow.
 *
 * Idêntico a firestoreCollectionFlow em comportamento, apenas toma Query
 * em vez de CollectionReference como parâmetro.
 *
 * Casos de uso:
 * - Filtrar por campo: whereEqualTo, whereGreaterThan, whereIn
 * - Ordenar: orderBy
 * - Limitar: limit
 * - Combinações: query.where(...).orderBy(...).limit(...)
 *
 * Fluxo de dados:
 * ```
 * Firestore Query (com filtros)
 *   |
 *   v
 * SnapshotListener (registra)
 *   |
 *   v
 * DocumentSnapshot[] (recebe - filtrados)
 *   |
 *   +-> toObject(modelClass) → T
 *   +-> copyIdToModel(item, doc.id)
 *   |
 *   v
 * List<T> (emite via trySend)
 * ```
 *
 * @param query Query do Firestore com filtros/ordenação (ex: db.collection("students").whereEqualTo("classId", "123"))
 * @param modelClass Classe do modelo para deserialização (ex: Student::class.java)
 * @param logTag Tag customizável para Log.d/e (default: "FirestoreFlow")
 *
 * @return Flow<List<T>> que emite List<T> sempre que query resulta mudam
 *         Comportamento idêntico a firestoreCollectionFlow
 *
 * Exemplo com filtro simples:
 * ```kotlin
 * fun getStudentsByClass(classId: String): Flow<List<Student>> {
 *     return firestoreQueryFlow(
 *         db.collection("students")
 *             .whereEqualTo("classId", classId),
 *         Student::class.java
 *     )
 * }
 *
 * // Usar:
 * viewModelScope.launch {
 *     repository.getStudentsByClass("class-123").collect { students ->
 *         _students.value = students
 *     }
 * }
 * ```
 *
 * Exemplo com múltiplos filtros e ordenação:
 * ```kotlin
 * fun getActiveTasksOrderedByDeadline(): Flow<List<Task>> {
 *     return firestoreQueryFlow(
 *         db.collection("tasks")
 *             .whereEqualTo("isActive", true)
 *             .whereGreaterThan("deadline", LocalDate.now())
 *             .orderBy("deadline", Query.Direction.ASCENDING),
 *         Task::class.java
 *     )
 * }
 * ```
 *
 * Exemplo com filtros por range:
 * ```kotlin
 * fun getGradesByRange(minScore: Double, maxScore: Double): Flow<List<Grade>> {
 *     return firestoreQueryFlow(
 *         db.collection("grades")
 *             .whereGreaterThanOrEqualTo("score", minScore)
 *             .whereLessThanOrEqualTo("score", maxScore)
 *             .orderBy("score", Query.Direction.DESCENDING)
 *             .limit(100),
 *         Grade::class.java
 *     )
 * }
 * ```
 *
 * Exemplo com filtro por lista:
 * ```kotlin
 * fun getAttendanceByDates(dates: List<LocalDate>): Flow<List<AttendanceRecord>> {
 *     val dateStrings = dates.map { it.toString() }
 *     return firestoreQueryFlow(
 *         db.collection("attendance")
 *             .whereIn("date", dateStrings),
 *         AttendanceRecord::class.java
 *     )
 * }
 * ```
 *
 * @see firestoreCollectionFlow
 * @see copyIdToModel
 */
inline fun <reified T> firestoreQueryFlow(
    query: Query,
    modelClass: Class<T>,
    logTag: String = "FirestoreFlow"
): Flow<List<T>> = callbackFlow {
    Log.d(logTag, "Iniciando listener para query")

    val listener = query.addSnapshotListener { snapshots, e ->
        if (e != null) {
            // Erro transitório — não fechar o flow, apenas logar
            Log.w(logTag, "Erro ao ouvir query (mantendo listener ativo)", e)
            return@addSnapshotListener
        }

        try {
            val items = snapshots?.mapNotNull { doc ->
                try {
                    val item = doc.toObject(modelClass)
                    copyIdToModel(item, doc.id)
                    item
                } catch (e: Exception) {
                    Log.e(logTag, "Erro ao converter documento", e)
                    null
                }
            } ?: emptyList()

            Log.d(logTag, "✓ Query resultado: ${items.size} itens")
            trySend(items)
        } catch (e: Exception) {
            Log.e(logTag, "Erro ao processar snapshot de query", e)
        }
    }

    awaitClose {
        Log.d(logTag, "Removendo listener de query")
        listener.remove()
    }
}

/**
 * Cópia reflexiva do ID do documento Firestore para campo 'id' do modelo.
 *
 * Função auxiliar que tenta setar o campo 'id' em uma instância de modelo
 * usando reflexão. Útil porque Firestore armazena IDs de documentos separadamente
 * do JSON, mas queremos incluir o ID no objeto modelo.
 *
 * Problema:
 * - Firestore armazena dados em JSON, sem o ID do documento
 * - Quando converte DocumentSnapshot para modelo via toObject(Class<T>),
 *   o ID do documento é perdido
 * - Muitos modelos precisam do ID para operações CRUD
 *
 * Solução:
 * - Após toObject(modelClass), chama copyIdToModel(item, doc.id)
 * - Tenta setar o campo 'id' usando reflexão
 * - Se falhar (sem campo id ou val), ignora silenciosamente
 *
 * Exemplo fluxo:
 * ```
 * DocumentSnapshot doc (id="task-123")
 *   |
 *   +-> toObject(Task::class.java) → Task(title="Tarefa", ...)
 *   +-> copyIdToModel(task, "task-123")
 *   |
 *   v
 * Task(id="task-123", title="Tarefa", ...)  // ID now set!
 * ```
 *
 * Reflexão implementada:
 * 1. getDeclaredField("id") - obtém o field
 * 2. isAccessible = true - permite acesso mesmo se privado/protected
 * 3. idField.set(obj, documentId) - seta o valor
 *
 * Funcionamento:
 * - Funciona com data classes Kotlin que têm campo 'id: String'
 * - Funciona com classes normais com atributo 'id'
 * - Falha silenciosamente se:
 *   - Campo 'id' não existe
 *   - Campo 'id' é val (imutável)
 *   - Campo 'id' tem tipo diferente de String
 *   - Outras exceções de reflexão
 *
 * Por que não usar interface Identifiable?
 * ```kotlin
 * interface Identifiable { var id: String }
 * // Todos os modelos implementariam Identifiable
 * // Mais Kotlin-idiomatic, mas requer mais mudanças
 * ```
 *
 * @param item Instância do modelo onde setar o ID
 * @param documentId String contendo o ID do documento (ex: "task-123")
 *
 * Exemplos:
 * ```kotlin
 * // Automático dentro de firestoreCollectionFlow
 * val items = snapshots?.mapNotNull { doc ->
 *     val item = doc.toObject(Task::class.java)
 *     copyIdToModel(item, doc.id)  // Agora item.id = doc.id
 *     item
 * }
 *
 * // Manual se necessário
 * val task = doc.toObject(Task::class.java)
 * copyIdToModel(task, "meu-task-id")
 * // task.id agora é "meu-task-id" (se tiver campo id)
 * ```
 *
 * @see firestoreCollectionFlow
 * @see firestoreQueryFlow
 */
inline fun <T> copyIdToModel(item: T, documentId: String) {
    try {
        // Tentar setar via reflexão (funciona se houver campo id)
        item?.let { obj ->
            val idField = obj::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(obj, documentId)
        }
    } catch (e: Exception) {
        // Campo 'id' não existe ou é val (imutável) - ignorar silenciosamente
        // Log.d("copyIdToModel", "Não foi possível setar ID para ${item::class.simpleName}")
        // O modelo provavelmente já tem seu próprio ID ou não precisa
    }
}

/**
 * Versão mais simples: assumindo que modelo é data class com campo 'id'.
 * Usa extension function do data class.
 *
 * ALTERNATIVA (mais Kotlin-idiomatic):
 * Pode-se usar reflexão completa ou extension functions:
 *
 * inline fun <reified T : Identifiable> firestoreFlow(...)
 *
 * interface Identifiable {
 *     var id: String
 * }
 *
 * Então Task, Grade, etc implementariam Identifiable
 */

/**
 * Resultado envolvido de um Flow Firestore com estado (Success/Error/Loading).
 *
 * Sealed class alternativa para encapsular o resultado completo de um Flow,
 * incluindo estado de carregamento. Útil quando precisa-se sinalizar diferentes
 * estados da operação ao invés de apenas emitir dados.
 *
 * Comparação com Flow<List<T>> direto:
 * ```kotlin
 * // Padrão 1: Flow direto (recomendado para reatividade simples)
 * fun getTasks(): Flow<List<Task>> = firestoreCollectionFlow(...)
 * // Emite sempre que dados mudam, sem estado de loading/erro
 *
 * // Padrão 2: Flow envolvido com resultado (para UI state complexo)
 * fun getTasks(): Flow<FirestoreFlowResult<Task>> =
 *     firestoreCollectionFlow(...).map { Success(it) }
 * // Emite Success(data), Error(exception), Loading
 * ```
 *
 * Estados:
 * - Success<T>: Dados carregados com sucesso
 * - Error<T>: Erro durante carregamento/monitoramento
 * - Loading<T>: Operação em progresso
 *
 * @param T Tipo do modelo (Task, Grade, Student, etc)
 *
 * @see Success
 * @see Error
 * @see Loading
 * @see firestoreCollectionFlow
 * @see firestoreQueryFlow
 */
sealed class FirestoreFlowResult<T> {
    /**
     * Sucesso: dados foram carregados e emitidos com sucesso.
     *
     * @param data List<T> contendo os dados carregados do Firestore
     */
    data class Success<T>(val data: List<T>) : FirestoreFlowResult<T>()

    /**
     * Erro: falha durante carregamento ou monitoramento.
     *
     * @param exception Exception capturada durante operação
     */
    data class Error<T>(val exception: Exception) : FirestoreFlowResult<T>()

    /**
     * Carregamento: operação em progresso, aguardando resposta.
     *
     * Usado para sinalizar ao collector que dados estão sendo carregados.
     */
    class Loading<T> : FirestoreFlowResult<T>()
}
