package com.example.takstud.model

/**
 * Modelo de Nota (Grade) - avaliação de estudante em tarefa.
 *
 * Representa a nota (avaliação) de um estudante em uma tarefa específica.
 * Notas são processadas em batch (GradeBatchManager) para performance
 * e sincronizadas com Firestore de forma robusta.
 *
 * Responsabilidades:
 * - Registrar desempenho do estudante em uma tarefa
 * - Manter histórico de notas com timestamps
 * - Sincronizar notas com servidor (offline support)
 * - Fornecer dados para relatórios de desempenho
 *
 * Arquitetura:
 * ```
 * Task (ex: \"Atividade de Português\")
 *   |
 *   +--- Grade 1: Student A (score=85)
 *   +--- Grade 2: Student B (score=92)
 *   +--- Grade 3: Student C (score=78)
 *   |
 *   v
 * [Batch Processing] (GradeBatchManager)
 *   |
 *   +-- Validação de scores (0-100)
 *   +-- Detecção de duplicatas
 *   +-- Sincronização em lotes
 *   |
 *   v
 * Firestore (collection: \"grades\")
 * ```
 *
 * Ciclo de vida:
 * 1. Criação: Professor lança nota para tarefa
 * 2. Validação: Validar score entre 0-100
 * 3. Local: Armazenar em Room, isSynced=false
 * 4. Sincronização: GradeBatchManager envia para Firestore
 * 5. Confirmação: Quando Firestore aceita, isSynced=true
 * 6. Atualização: Professor altera nota
 * 7. Exclusão: Professor remove nota (soft delete possível)
 *
 * Validação:
 * - score: \"0\" a \"100\" (use AdvancedValidator.validateGrade)
 * - value: String numérico (duplicata de score por compatibilidade)
 * - taskId: Referência válida a Task existente
 * - studentId: Referência válida a Student existente
 *
 * Relacionamentos:
 * - Referencia: Task (via taskId)
 * - Referencia: Student (via studentId ou studentRa)
 * - Contém: histárico de criação/modificação (createdAt/modifiedAt)
 * - Sincronização: OfflineSyncQueue (via isSynced)
 *
 * Particularidades:
 * - score E value: Ambos armazenam a nota (duplicata por histórico)
 * - studentRa: Desnormalização de RA para queries rápidas
 * - GradeBatchManager: Operações em lote para eficiência (até 500 por lote)
 *
 * Exemplo de criação e validação:
 * ```kotlin\n * val grade = Grade(
 *     taskId = \"task-123\",
 *     studentId = \"student-456\",
 *     studentRa = \"2024001\",
 *     score = \"85\",
 *     value = \"85\"
 * )
 *
 * // Validação
 * val validScore = AdvancedValidator.validateGrade(grade.score, 0.0, 100.0)
 * if (validScore.isValid()) {
 *     val scoreValue = validScore.getOrNull<Double>()  // 85.0
 *     repository.saveGrade(grade)
 * }
 * ```
 *
 * Exemplo de batch update (GradeBatchManager):
 * ```kotlin
 * // Múltiplas notas para mesma tarefa
 * val updates = mapOf(
 *     \"grade-001\" to GradeUpdateData(score = \"85\"),
 *     \"grade-002\" to GradeUpdateData(score = \"92\"),
 *     \"grade-003\" to GradeUpdateData(score = \"78\")\n * )
 *
 * // GradeBatchManager faz update em chunks de 500
 * gradeBatchManager.updateGradesBatch(updates) { result ->
 *     println(\"Success: ${result.successCount}, Failed: ${result.failureCount}\")
 * }
 * ```
 *
 * Performance:
 * - Batch size: até 500 notas por transação Firestore
 * - Índices: Recomendado em (taskId, studentId) para queries
 * - Sincronização: Prioridade alta (operações críticas)
 *
 * @property id Identificador único da nota (gerado pelo Firestore)
 *              Ex: \"grade-abc123def456\"
 *              Default: \"\" (será preenchido por Firestore)
 *              Usado como chave primária em Room
 *
 * @property taskId ID da tarefa à qual a nota se refere
 *                  Ex: \"task-123\"
 *                  Default: \"\"
 *                  Chave estrangeira para Task
 *
 * @property studentId ID do estudante que recebeu a nota
 *                     Ex: \"student-456\" ou igual ao RA \"2024001\"
 *                     Default: \"\"
 *                     Chave estrangeira para Student
 *
 * @property studentRa Desnormalização de RA do estudante
 *                     Cópia do Student.ra armazenada na nota
 *                     Ex: \"2024001\"
 *                     Default: \"\"
 *                     Permite queries diretas sem join
 *
 * @property score Nota numérica como String (0-100)
 *                 Ex: \"85\", \"92\", \"78\"
 *                 Default: \"\"
 *                 Validável com AdvancedValidator.validateGrade
 *                 String em vez de Int para flexibilidade (0-10, 0-100, etc)
 *
 * @property value Valor duplicado da nota (compatibilidade histórica)
 *                 Deve ser igual a score em novos registros
 *                 Ex: \"85\"
 *                 Default: \"\"
 *                 TODO: Consolidar em um único campo
 *
 * @property createdAt Timestamp de criação em millisegundos
 *                     Ex: 1700000000000
 *                     Default: 0 (não definido)
 *                     Automaticamente preenchido no saveGrade
 *
 * @property modifiedAt Timestamp da última modificação
 *                      Ex: 1700050000000
 *                      Default: 0 (não modificado)
 *                      Atualizado a cada update
 *
 * @property isSynced Status de sincronização local/remoto
 *                    true: Sincronizado com Firestore
 *                    false: Pendente de sincronização (offline)
 *                    Default: false
 *                    Usado pelo GradeBatchManager para processamento
 *
 * @see Task
 * @see Student
 * @see GradeBatchManager
 * @see AdvancedValidator
 * @see TakStudRepository.getGrades
 * @see TakStudRepository.saveGrade
 * @see TakStudRepository.deleteGrade
 */
data class Grade(
    val id: String = "",
    val taskId: String = "",
    val studentId: String = "",
    val studentRa: String = "",
    val score: String = "",
    val value: String = "",  // Valor da nota (0-10 ou 0-100)
    val createdAt: Long = 0,
    val modifiedAt: Long = 0,
    val isSynced: Boolean = false
)
