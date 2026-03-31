package com.example.takstud.model

/**
 * Modelo de Estudante (Student) com informações acadêmicas e contato.
 *
 * Representa um estudante da instituição com seus dados acadêmicos (RA, turma)
 * e informações de contato (responsável, telefone). Estudantes usam RA para
 * autenticação no sistema (LoginRateLimiter usa RA como identifier).
 *
 * Responsabilidades:
 * - Armazenar dados demográficos do estudante
 * - Registrar responsável/pai para comunicação
 * - Servir como bridge entre tarefas/notas e estudante
 * - Fornecer RA para login de pais
 *
 * Fluxo de autenticação de pais:
 * ```
 * Pai insere RA (ex: "2024001")
 *   |
 *   v
 * LoginRateLimiter.isAllowedToLogin(ra)? (max 5 tentativas/hora)
 *   |
 *   v
 * Busca Student por RA
 *   |
 *   v
 * Carrega tarefas/notas do estudante
 *   |
 *   v
 * Cria SecureSession(ra, studentId)
 * ```
 *
 * Estrutura de dados:
 * ```
 * Student
 *   |
 *   +-- id: String (Firestore doc id, geralmente = RA)
 *   +-- ra: String (matrícula, usado para login)
 *   +-- name: String (nome completo)
 *   +-- studentClass: String (nome legível da turma, ex: "9º A")
 *   +-- classId: String (ID de referência para Class)
 *   +-- parent: String (nome do responsável)
 *   +-- phone: String (contato telefônico)
 *   +-- createdAt: Long (timestamp de registro)
 * ```
 *
 * Persistência:
 * - Firestore: Coleção "students" com documento id={ra}
 * - Room: Entidade @Entity(tableName = "students")
 * - Offline: Cache com sincronização de OfflineSyncQueue
 *
 * Validação:
 * - name: 3-100 caracteres, português (use AdvancedValidator.validateName)
 * - ra: 2-20 dígitos apenas (use AdvancedValidator.validateRA)
 * - phone: Formatos brasileiros (use AdvancedValidator.validatePhone)
 * - parent: 3-100 caracteres (use AdvancedValidator.validateName)
 *
 * Relacionamentos:
 * - Vinculado a: Class (via classId)
 * - Possuidor de: Task[] (via queries em TaskRepository)
 * - Receptor de: Grade[] (via id em grade.studentId)
 * - Ator de: AttendanceRecord[] (via id)
 *
 * Exemplo de criação e registro:
 * ```kotlin
 * val student = Student(
 *     ra = "2024001",
 *     name = "João Silva",
 *     studentClass = "9º A",
 *     classId = "class-123",
 *     parent = "Maria Silva",
 *     phone = "(11) 99999-9999"
 * )
 *
 * // Validação de entrada
 * val nameValid = AdvancedValidator.validateName(student.name)
 * val raValid = AdvancedValidator.validateRA(student.ra)
 * val phoneValid = AdvancedValidator.validatePhone(student.phone)
 *
 * if (nameValid.isValid() && raValid.isValid() && phoneValid.isValid()) {
 *     repository.saveStudent(student)
 * }
 * ```
 *
 * Exemplo de login de pai:
 * ```kotlin
 * // Tela de login
 * fun onParentLogin(ra: String) {
 *     val limiter = LoginRateLimiter(context)
 *
 *     // Verificar rate limiting
 *     if (!limiter.isAllowedToLogin(ra)) {
 *         val secondsLeft = limiter.getSecondsUntilRetry(ra)
 *         showError(\"Bloqueado. Tente em ${secondsLeft}s\")
 *         return
 *     }
 *
 *     // Buscar estudante
 *     viewModel.onParentLogin(ra) { success ->
 *         if (success) {
 *             limiter.clearAttempts(ra)  // Sucesso - limpar contador
 *             navigateToParentDashboard(ra)
 *         } else {\n *             limiter.recordFailedAttempt(ra)  // Falha - contar tentativa
 *             showError(\"RA ou senha incorretos\")
 *         }
 *     }
 * }
 * ```
 *
 * Considerações de segurança:
 * - RA é público (identificador) mas login é limitado (LoginRateLimiter)
 * - Telefone e parent são dados sensíveis (não expor desnecessariamente)
 * - Dados de criança (estudante) protegidos por LGPD
 * - Acessos rastreados para auditoria
 *
 * @property id Identificador único do documento Firestore
 *              Geralmente igual ao RA para facilitar lookups
 *              Ex: \"2024001\"
 *              Default: \"\" (será preenchido por Firestore)
 *
 * @property ra Registro Acadêmico (matrícula) do estudante
 *              Usado como username para login de pais
 *              2-20 dígitos apenas
 *              Ex: \"2024001\", \"2023456\"
 *              Default: \"\"
 *              Validável com AdvancedValidator.validateRA
 *
 * @property name Nome completo do estudante (português com acentuação)
 *                3-100 caracteres
 *                Ex: \"João da Silva Santos\"
 *                Default: \"\"
 *                Validável com AdvancedValidator.validateName
 *
 * @property studentClass Nome legível da turma (para UI)
 *                         Ex: \"9º A\", \"Turma 201\", \"3ª série B\"
 *                         Default: \"\"
 *                         Use classId para queries estruturadas
 *
 * @property classId Referência estruturada para Class
 *                   ID do documento Class no Firestore
 *                   Ex: \"class-123\" ou \"turma-2024-A\"
 *                   Default: \"\"
 *                   Usar para filterBy e queries
 *
 * @property parent Nome do responsável/pai para comunicação
 *                  3-100 caracteres, português
 *                  Ex: \"Maria da Silva Santos\"
 *                  Default: \"\"
 *                  Validável com AdvancedValidator.validateName
 *
 * @property phone Telefone de contato do responsável
 *                 Formatos brasileiros: 11999999999, (11) 9999-9999, etc
 *                 Ex: \"(11) 99999-9999\"
 *                 Default: \"\"
 *                 Validável com AdvancedValidator.validatePhone
 *
 * @property createdAt Timestamp de criação em millisegundos
 *                     Ex: 1700000000000
 *                     Default: 0 (não definido)
 *                     Automaticamente preenchido no saveStudent
 *
 * @see Class
 * @see Grade
 * @see AttendanceRecord
 * @see LoginRateLimiter
 * @see SecureSessionManager
 * @see AdvancedValidator
 * @see com.example.takstud.data.repository.StudentRepository.getStudents
 * @see com.example.takstud.data.repository.StudentRepository.saveStudent
 * @see com.example.takstud.data.repository.StudentRepository.deleteStudent
 */
data class Student(
    val id: String = "",
    val ra: String = "",
    val name: String = "",
    val studentClass: String = "",
    val classId: String = "",
    val parent: String = "",
    val phone: String = "",
    val createdAt: Long = 0
)
