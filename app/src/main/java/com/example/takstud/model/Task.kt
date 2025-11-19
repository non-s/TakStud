package com.example.takstud.model

/**
 * Modelo de Tarefa (Task) para tarefas escolares.
 *
 * Representa uma tarefa atribuída a uma ou mais turmas. Tarefas são acompanhadas
 * por notas (grades) que registram a performance dos estudantes em cada tarefa.
 *
 * Estrutura:
 * ```
 * Task (entidade)
 *   |
 *   +-- id: String (único, gerado por Firestore)
 *   +-- title: String (nome descritivo da tarefa)
 *   +-- description: String (detalhes e instruções)
 *   +-- dueDate: String (prazo em formato dd/MM/yyyy)
 *   +-- studentClass: String (ID da turma alvo)
 *   +-- createdAt: Long (timestamp de criação)
 *   +-- modifiedAt: Long (timestamp da última modificação)
 *   +-- isSynced: Boolean (status de sincronização local/server)
 * ```
 *
 * Persistência:
 * - Firestore: Coleção "tasks" com documento id={id}
 * - Room: Tabela tasks com entidade correspondente
 * - Offline: Cache local com flag isSynced para controle
 *
 * Ciclo de vida:
 * 1. Criação: Professor cria nova tarefa (createdAt = now)
 * 2. Atribuição: Tarefa vinculada a turma (studentClass = "class-123")
 * 3. Publicação: isSynced = true (enviado ao servidor)
 * 4. Modificação: Deadline alterado (modifiedAt = now)
 * 5. Exclusão: Deletado de Firestore e cache local
 *
 * Validação:
 * - title: 3-150 caracteres (use AdvancedValidator.validateTitle)
 * - description: 5-500 caracteres (use AdvancedValidator.validateDescription)
 * - dueDate: Formato dd/MM/yyyy (use AdvancedValidator.validateDate)
 * - studentClass: ID válido de turma existente
 *
 * Relacionamentos:
 * - Vinculado a: Class (via studentClass)
 * - Contém: Grade[] (via id em task.gradeId)
 * - Propietário: Teacher (implícito, não armazenado)
 *
 * Exemplo de criação:
 * ```kotlin
 * val task = Task(
 *     title = "Atividade de Português",
 *     description = "Leitura e análise de texto",
 *     dueDate = "20/12/2025",
 *     studentClass = "class-123"
 * )
 *
 * // Validação
 * val titleValid = AdvancedValidator.validateTitle(task.title)
 * val descValid = AdvancedValidator.validateDescription(task.description)
 * val dateValid = AdvancedValidator.validateDate(task.dueDate)
 *
 * if (titleValid.isValid() && descValid.isValid() && dateValid.isValid()) {
 *     repository.saveTask(task)
 * }
 * ```
 *
 * Exemplo de leitura em ViewModel:
 * ```kotlin
 * viewModelScope.launch {
 *     repository.getTasks()
 *         .map { tasks ->
 *             tasks.filter { it.studentClass == selectedClass }
 *         }
 *         .distinctUntilChanged()
 *         .collect { filteredTasks ->
 *             _tasks.value = filteredTasks
 *         }
 * }
 * ```
 *
 * @property id Identificador único da tarefa (gerado pelo Firestore)
 *              Ex: "task-abc123def456"
 *              Default: "" (será preenchido por Firestore)
 *
 * @property title Título descritivo da tarefa (3-150 chars)
 *                 Ex: "Atividade sobre Revolução Francesa"
 *                 Default: ""
 *
 * @property description Descrição detalhada com instruções (5-500 chars)
 *                       Ex: "Ler capítulo 5 e responder perguntas 1-10"
 *                       Default: ""
 *
 * @property dueDate Prazo para conclusão em formato dd/MM/yyyy
 *                   Ex: "15/12/2025"
 *                   Default: "" (sem prazo definido)
 *                   Suporta validação com AdvancedValidator.validateDate
 *
 * @property studentClass ID da turma a qual a tarefa é atribuída
 *                         Ex: "class-123" ou "turma-2024-A1"
 *                         Default: ""
 *                         Referencia Class.id
 *
 * @property createdAt Timestamp de criação em millisegundos (System.currentTimeMillis())
 *                     Ex: 1700000000000
 *                     Default: 0 (não definido)
 *                     Usado para ordenação e auditoria
 *
 * @property modifiedAt Timestamp da última modificação em millisegundos
 *                      Ex: 1700050000000
 *                      Default: 0 (não modificado)
 *                      Atualizado a cada save()
 *
 * @property isSynced Status de sincronização local/remoto
 *                    true: Sincronizado com Firestore
 *                    false: Pendente de sincronização (offline)
 *                    Default: false
 *                    Usado pelo OfflineSyncQueue para rastrear pendências
 *
 * @see Class
 * @see Grade
 * @see AdvancedValidator
 * @see TakStudRepository.getTasks
 * @see TakStudRepository.saveTask
 * @see TakStudRepository.deleteTask
 */
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val studentClass: String = "",
    val createdAt: Long = 0,
    val modifiedAt: Long = 0,
    val isSynced: Boolean = false
)
