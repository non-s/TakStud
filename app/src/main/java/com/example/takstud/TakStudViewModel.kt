package com.example.takstud

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Class
import com.example.takstud.model.Grade
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.model.Student
import com.example.takstud.model.Task
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal do TakStud - gerencia estado de UI e dados da aplicação.
 *
 * Responsabilidades:
 * - Exposição de dados em tempo real via StateFlow (obtidos do Repository)
 * - Gerenciamento de seleções do usuário (filtros, datas, turmas)
 * - Mensagens de erro para UI
 * - Operações CRUD (Create, Read, Update, Delete) delegadas ao Repository
 * - Manutenção de estado entre recomposições Compose
 *
 * Arquitetura:
 * ```
 * Composable (UI)
 *     ↓
 * TakStudViewModel (esta classe)
 *     ↓
 * TakStudRepository
 *     ↓
 * Firebase Firestore
 * ```
 *
 * StateFlow vs Flow:
 * - StateFlow: Estado atual + cambias (subscriptions imediatas)
 * - Lazy initialization: dados começam a carregar quando observados
 * - WhileSubscribed(5000): cancela após 5s sem subscribers
 *
 * Exemplo de uso em Composables:
 * ```kotlin
 * @Composable
 * fun TaskListScreen(viewModel: TakStudViewModel) {
 *     val tasks by viewModel.tasks.collectAsState()
 *     val errorMsg by viewModel.errorMessage.collectAsState()
 *
 *     LazyColumn {
 *         items(tasks) { task ->
 *             TaskCard(task)
 *         }
 *     }
 * }
 * ```
 *
 * @see TakStudRepository
 * @see StateFlow
 * @see ViewModel
 */
class TakStudViewModel(private val repository: TakStudRepository = TakStudRepository()) : ViewModel() {

    // ==================== UI STATE ====================

    /**
     * Mensagem de erro a ser exibida no UI.
     * Null quando não há erro. Deve ser limpa após exibição.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)

    /**
     * Estado público de mensagens de erro.
     *
     * Observar para exibir snackbars ou toast notifications.
     * Exemplo: `errorMessage.collectAsState().value?.let { showSnackbar(it) }`
     *
     * @see setErrorMessage
     */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Código administrativo carregado do Firebase Remote Config.
     *
     * Usado para autenticação de admin sem credentials padrão.
     * Deve ser alterado regularmente no Firebase Console por segurança.
     *
     * @see loadAdminSecret
     */
    private val _adminSecret = MutableStateFlow("")

    /**
     * Estado público do código administrativo.
     *
     * Exemplo:
     * ```kotlin
     * val adminSecret by viewModel.adminSecret.collectAsState()
     * if (userInput == adminSecret) {
     *     navigateToAdminPanel()
     * }
     * ```
     */
    val adminSecret: StateFlow<String> = _adminSecret.asStateFlow()

    /**
     * Turma selecionada para operações de frequência.
     *
     * Usado como filtro ao registrar presença/ausência.
     *
     * @see setAttendanceData
     * @see clearAttendanceData
     */
    private val _selectedClassForAttendance = MutableStateFlow("")

    /**
     * Estado público da turma selecionada para frequência.
     *
     * Exemplo: "6A", "7B", etc.
     */
    val selectedClassForAttendance: StateFlow<String> = _selectedClassForAttendance.asStateFlow()

    /**
     * Data selecionada para operações de frequência (formato: yyyy-MM-dd).
     *
     * Usado junto com selectedClassForAttendance para filtrar registros.
     *
     * @see setAttendanceData
     * @see clearAttendanceData
     */
    private val _selectedDateForAttendance = MutableStateFlow("")

    /**
     * Estado público da data selecionada para frequência.
     *
     * Exemplo: "2025-11-13"
     */
    val selectedDateForAttendance: StateFlow<String> = _selectedDateForAttendance.asStateFlow()

    // ==================== DATA STATE ====================

    /**
     * Lista de todas as tarefas em tempo real.
     *
     * Emite atualizações sempre que tarefas mudam no Firestore.
     * StateFlow.Lazy: carrega sob demanda quando subscrito.
     *
     * Exemplo:
     * ```kotlin
     * val tasks by viewModel.tasks.collectAsState()
     * taskList.forEach { task ->
     *     println("${task.title}: ${task.dueDate}")
     * }
     * ```
     *
     * @see Task
     */
    val tasks: StateFlow<List<Task>> = repository.getTasks().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Lista de todos os avisos em tempo real.
     *
     * Avisos são comunicações dos professores para pais/responsáveis.
     *
     * @see Notice
     */
    val notices: StateFlow<List<Notice>> = repository.getNotices().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Lista de todos os horários de aula em tempo real.
     *
     * Define quando cada turma tem aula em cada período (MANHA, TARDE, EJA).
     *
     * @see Schedule
     */
    val schedules: StateFlow<List<Schedule>> = repository.getSchedules().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Lista de todos os estudantes em tempo real.
     *
     * Contém matrícula, nome, classe e dados de contato.
     *
     * @see Student
     */
    val students: StateFlow<List<Student>> = repository.getStudents().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Lista de todas as notas em tempo real.
     *
     * Scores de tarefas para estudantes individuais (0-100).
     *
     * @see Grade
     */
    val grades: StateFlow<List<Grade>> = repository.getGrades().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Lista de todos os registros de frequência em tempo real.
     *
     * Presença/ausência de estudantes em aulas.
     *
     * @see AttendanceRecord
     */
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.getAttendanceRecords().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Lista de todas as turmas em tempo real.
     *
     * Informações de série, letra, período.
     *
     * @see Class
     */
    val classes: StateFlow<List<Class>> = repository.getClasses().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Mapa de turmas agrupadas por período em tempo real.
     *
     * Estrutura: Map<periodoName, listaDeTurmas>
     * Exemplo: {"MANHA" -> ["6A", "6B"], "TARDE" -> ["7A"]}
     *
     * Usa WhileSubscribed(5000) para economia de recursos.
     * Cancela após 5 segundos sem subscribers.
     *
     * @see getClassesByPeriod
     */
    val classesByPeriod: StateFlow<Map<String, List<String>>> = repository.getClassesByPeriod().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadAdminSecret()
    }

    /**
     * Carrega o código administrativo do Firebase Remote Config.
     *
     * Sem valor padrão (hardcoded) - força admin a configurar corretamente.
     * Fetch interval: 3600 segundos (1 hora) para economia de banda.
     *
     * SEGURANÇA: O código deve ser alterado regularmente no Firebase Console
     * e nunca deve ser hardcoded no código.
     *
     * Fluxo:
     * 1. Configura Remote Config com minimumFetchInterval = 1 hora
     * 2. Busca ("fetch") e ativa configurações
     * 3. Obtém valor de "admin_secret"
     * 4. Se vazio, define erro: "Código admin não configurado"
     *
     * @throws Exception se falhar na carga do Remote Config
     *
     * @see _adminSecret
     * @see adminSecret
     */
    private fun loadAdminSecret() {
        viewModelScope.launch {
            try {
                val remoteConfig = com.google.firebase.ktx.Firebase.remoteConfig
                val configSettings = remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600 // 1 hora entre fetches
                }
                remoteConfig.setConfigSettingsAsync(configSettings)
                // REMOVIDO: setDefaultsAsync(mapOf("admin_secret" to "58239617"))
                // Agora OBRIGATÓRIO configurar no Firebase Console

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

    /**
     * Autentica parent/responsável por RA (Registro de Aluno).
     *
     * Procura estudante com RA correspondente e navega se encontrado.
     * Se não encontrado, exibe erro: "RA não encontrado!"
     *
     * @param ra Registro de aluno a buscar (case-insensitive)
     * @param navigateToParent Callback com o estudante encontrado
     *
     * Exemplo:
     * ```kotlin
     * viewModel.onParentLogin("2024001") { student ->
     *     navController.navigate("parent_screen/${student.id}")
     * }
     * ```
     *
     * @see setErrorMessage
     * @see errorMessage
     */
    fun onParentLogin(ra: String, navigateToParent: (Student) -> Unit) {
        val student = students.value.find { it.ra.equals(ra, ignoreCase = true) }
        if (student != null) {
            navigateToParent(student)
            _errorMessage.value = null
        } else {
            _errorMessage.value = "RA não encontrado!"
        }
    }

    /**
     * Define mensagem de erro para exibição no UI.
     *
     * Use null para limpar o erro após exibição.
     *
     * @param message Mensagem de erro ou null para limpar
     *
     * Exemplo:
     * ```kotlin
     * if (validation fails) {
     *     viewModel.setErrorMessage("Email inválido")
     * } else {
     *     viewModel.setErrorMessage(null)
     * }
     * ```
     *
     * @see errorMessage
     */
    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    /**
     * Define turma e data selecionadas para operações de frequência.
     *
     * Usado quando usuário seleciona qual turma e data para registrar presença.
     *
     * @param className Nome da turma (ex: "6A", "7B")
     * @param date Data no formato "yyyy-MM-dd" ou similar
     *
     * Exemplo:
     * ```kotlin
     * viewModel.setAttendanceData("6A", "2025-11-13")
     * val selectedClass = viewModel.selectedClassForAttendance.value
     * val selectedDate = viewModel.selectedDateForAttendance.value
     * ```
     *
     * @see selectedClassForAttendance
     * @see selectedDateForAttendance
     * @see clearAttendanceData
     */
    fun setAttendanceData(className: String, date: String) {
        _selectedClassForAttendance.value = className
        _selectedDateForAttendance.value = date
    }

    /**
     * Limpa seleção de turma e data para frequência.
     *
     * Reseta valores para strings vazias.
     *
     * Exemplo:
     * ```kotlin
     * viewModel.clearAttendanceData()
     * ```
     *
     * @see setAttendanceData
     */
    fun clearAttendanceData() {
        _selectedClassForAttendance.value = ""
        _selectedDateForAttendance.value = ""
    }

    /**
     * Obtém tarefas filtradas para um estudante específico.
     *
     * Filtra tarefas da turma do estudante usando combine flow.
     * Atualiza em tempo real quando tarefas/horários mudam.
     *
     * @param student Estudante para filtrar tarefas
     *
     * @return StateFlow com tarefas da turma do estudante
     *
     * Exemplo:
     * ```kotlin
     * val tasksFlow = viewModel.getTasksForStudent(student)
     * tasksFlow.collect { tasks ->
     *     tasks.forEach { task ->
     *         println("${task.title}: ${task.dueDate}")
     *     }
     * }
     * ```
     *
     * @see Task
     * @see tasks
     */
    fun getTasksForStudent(student: Student): StateFlow<List<Task>> {
        return tasks.combine(schedules) { tasks, _ ->
            tasks.filter { it.studentClass == student.studentClass }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém avisos (notices) filtrados para um estudante específico.
     *
     * Filtra avisos da turma do estudante.
     * Avisos são comunicações dos professores.
     *
     * @param student Estudante para filtrar avisos
     *
     * @return StateFlow com avisos da turma do estudante
     *
     * @see Notice
     * @see notices
     */
    fun getNoticesForStudent(student: Student): StateFlow<List<Notice>> {
        return notices.combine(schedules) { notices, _ ->
            notices.filter { it.studentClass == student.studentClass }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém horários de aula filtrados para um estudante específico.
     *
     * Filtra horários da turma do estudante.
     * Horários definem quando a turma tem aula em cada período.
     *
     * @param student Estudante para filtrar horários
     *
     * @return StateFlow com horários da turma do estudante
     *
     * @see Schedule
     * @see schedules
     */
    fun getSchedulesForStudent(student: Student): StateFlow<List<Schedule>> {
        return schedules.combine(schedules) { schedules, _ ->
            schedules.filter { it.studentClass == student.studentClass }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém notas (grades) filtradas para um estudante específico.
     *
     * Filtra notas onde studentId corresponde ao ID do estudante.
     * Inclui todas as notas de todas as tarefas do estudante.
     *
     * @param student Estudante para filtrar notas
     *
     * @return StateFlow com notas do estudante
     *
     * Exemplo:
     * ```kotlin
     * val gradesFlow = viewModel.getGradesForStudent(student)
     * gradesFlow.collect { grades ->
     *     val average = grades.map { it.score.toDouble() }.average()
     *     println("Média: $average")
     * }
     * ```
     *
     * @see Grade
     * @see grades
     */
    fun getGradesForStudent(student: Student): StateFlow<List<Grade>> {
        return grades.combine(students) { grades, _ ->
            grades.filter { it.studentId == student.id }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém registros de frequência filtrados para um estudante específico.
     *
     * Filtra registros de presença/ausência do estudante.
     * Útil para calcular percentual de frequência.
     *
     * @param student Estudante para filtrar frequência
     *
     * @return StateFlow com registros de frequência do estudante
     *
     * Exemplo:
     * ```kotlin
     * val attendanceFlow = viewModel.getAttendanceForStudent(student)
     * attendanceFlow.collect { records ->
     *     val presentDays = records.count { it.isPresent }
     *     val percentage = (presentDays * 100) / records.size
     *     println("Frequência: $percentage%")
     * }
     * ```
     *
     * @see AttendanceRecord
     * @see attendanceRecords
     */
    fun getAttendanceForStudent(student: Student): StateFlow<List<AttendanceRecord>> {
        return attendanceRecords.combine(students) { records, _ ->
            records.filter { it.studentId == student.id }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém lista de estudantes para uma turma específica.
     *
     * Filtra estudantes pela classe.
     *
     * @param studentClass Nome da turma (ex: "6A", "7B")
     *
     * @return StateFlow com estudantes da turma
     *
     * Exemplo:
     * ```kotlin
     * val studentsFlow = viewModel.getStudentsForClass("6A")
     * studentsFlow.collect { students ->
     *     val names = students.map { it.name }
     *     displayStudentList(names)
     * }
     * ```
     *
     * @see Student
     * @see students
     */
    fun getStudentsForClass(studentClass: String): StateFlow<List<Student>> {
        return students.combine(schedules) { students, _ ->
            students.filter { it.studentClass == studentClass }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém notas (grades) filtradas para uma tarefa específica.
     *
     * Filtra todas as notas de uma tarefa.
     * Útil para ver desempenho de todos os estudantes em uma tarefa.
     *
     * @param taskId ID da tarefa
     *
     * @return StateFlow com notas para a tarefa
     *
     * Exemplo:
     * ```kotlin
     * val gradesFlow = viewModel.getGradesForTask("task_001")
     * gradesFlow.collect { grades ->
     *     val average = grades.map { it.score.toDouble() }.average()
     *     println("Média da turma: $average")
     * }
     * ```
     *
     * @see Grade
     * @see grades
     */
    fun getGradesForTask(taskId: String): StateFlow<List<Grade>> {
        return grades.combine(tasks) { grades, _ ->
            grades.filter { it.taskId == taskId }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém registros de frequência para uma turma em uma data específica.
     *
     * Filtra por turma E data.
     * Útil para visualizar presença de toda a turma em um dia.
     *
     * @param studentClass Nome da turma (ex: "6A")
     * @param date Data no formato "yyyy-MM-dd"
     *
     * @return StateFlow com registros de frequência da turma naquela data
     *
     * Exemplo:
     * ```kotlin
     * val attendanceFlow = viewModel.getAttendanceForClassByDate("6A", "2025-11-13")
     * attendanceFlow.collect { records ->
     *     val presentCount = records.count { it.isPresent }
     *     println("Presentes: $presentCount")
     * }
     * ```
     *
     * @see AttendanceRecord
     * @see attendanceRecords
     */
    fun getAttendanceForClassByDate(studentClass: String, date: String): StateFlow<List<AttendanceRecord>> {
        return attendanceRecords.combine(schedules) { records, _ ->
            records.filter { it.studentClass == studentClass && it.date == date }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    // ==================== DATA MANIPULATION (CRUD) ====================

    /**
     * Deleta uma tarefa do Firestore.
     *
     * Operação é assíncrona (firestore-pending).
     * Delegada ao Repository.
     *
     * @param task Tarefa a deletar (deve ter ID válido)
     *
     * @see TakStudRepository.deleteTask
     */
    fun deleteTask(task: Task) {
        repository.deleteTask(task)
    }

    /**
     * Deleta um aviso do Firestore.
     *
     * Operação é assíncrona. Delegada ao Repository.
     *
     * @param notice Aviso a deletar (deve ter ID válido)
     *
     * @see TakStudRepository.deleteNotice
     */
    fun deleteNotice(notice: Notice) {
        repository.deleteNotice(notice)
    }

    /**
     * Deleta um horário do Firestore.
     *
     * Operação é assíncrona. Delegada ao Repository.
     *
     * @param schedule Horário a deletar (deve ter ID válido)
     *
     * @see TakStudRepository.deleteSchedule
     */
    fun deleteSchedule(schedule: Schedule) {
        repository.deleteSchedule(schedule)
    }

    /**
     * Salva ou atualiza um horário no Firestore.
     *
     * Operação assíncrona. Callback onBack é executado após sucesso.
     *
     * @param schedule Horário a salvar (ID será gerado se vazio)
     * @param onBack Callback executado quando operação completa
     *
     * Exemplo:
     * ```kotlin
     * viewModel.saveSchedule(schedule) {
     *     navController.popBackStack()
     * }
     * ```
     *
     * @see TakStudRepository.saveSchedule
     */
    fun saveSchedule(schedule: Schedule, onBack: () -> Unit) {
        repository.saveSchedule(schedule, onBack)
    }

    /**
     * Salva ou atualiza uma tarefa no Firestore.
     *
     * Operação assíncrona. Callback onBack é executado após sucesso.
     *
     * @param task Tarefa a salvar (ID será gerado se vazio)
     * @param onBack Callback executado quando operação completa
     *
     * Exemplo:
     * ```kotlin
     * viewModel.saveTask(newTask) {
     *     showMessage("Tarefa criada!")
     *     navController.popBackStack()
     * }
     * ```
     *
     * @see TakStudRepository.saveTask
     */
    fun saveTask(task: Task, onBack: () -> Unit) {
        repository.saveTask(task, onBack)
    }

    /**
     * Salva ou atualiza um aviso no Firestore.
     *
     * Operação assíncrona. Callback onBack é executado após sucesso.
     *
     * @param notice Aviso a salvar (ID será gerado se vazio)
     * @param onBack Callback executado quando operação completa
     *
     * @see TakStudRepository.saveNotice
     */
    fun saveNotice(notice: Notice, onBack: () -> Unit) {
        repository.saveNotice(notice, onBack)
    }

    /**
     * Salva ou atualiza um estudante no Firestore.
     *
     * Operação assíncrona. Callback onBack é executado após sucesso.
     *
     * @param student Estudante a salvar (ID será gerado se vazio)
     * @param onBack Callback executado quando operação completa
     *
     * @see TakStudRepository.saveStudent
     */
    fun saveStudent(student: Student, onBack: () -> Unit) {
        repository.saveStudent(student, onBack)
    }

    /**
     * Deleta um estudante do Firestore.
     *
     * IMPORTANTE: Registros de notas e frequência não são deletados.
     * Operação é assíncrona.
     *
     * @param student Estudante a deletar (deve ter ID válido)
     *
     * @see TakStudRepository.deleteStudent
     */
    fun deleteStudent(student: Student) {
        repository.deleteStudent(student)
    }

    /**
     * Registra um novo estudante no Firestore.
     *
     * Cria objeto Student com dados básicos e salva via saveStudent.
     * Gera UUID automaticamente para o ID.
     *
     * @param name Nome completo do estudante
     * @param ra Registro de aluno (matrícula)
     * @param className Nome da turma (ex: "6A"), pode ser vazio
     *
     * Exemplo:
     * ```kotlin
     * viewModel.registerStudent("João Silva", "2024001", "6A")
     * ```
     *
     * @see saveStudent
     */
    fun registerStudent(name: String, ra: String, className: String = "") {
        val newStudent = Student(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            ra = ra,
            studentClass = className,
            classId = className,  // Usar o nome da turma como ID também
            parent = "",
            phone = ""
        )
        saveStudent(newStudent) {}
    }

    /**
     * Salva ou atualiza uma nota (grade) no Firestore.
     *
     * Operação assíncrona (firestore-pending).
     *
     * @param grade Nota a salvar (ID será gerado se vazio como taskId-studentId)
     *
     * @see TakStudRepository.saveGrade
     */
    fun saveGrade(grade: Grade) {
        repository.saveGrade(grade)
    }

    /**
     * Salva ou atualiza um registro de frequência no Firestore.
     *
     * Operação assíncrona (firestore-pending).
     *
     * @param record Registro a salvar (ID será gerado se vazio como studentId-date)
     *
     * @see TakStudRepository.saveAttendanceRecord
     */
    fun saveAttendanceRecord(record: AttendanceRecord) {
        repository.saveAttendanceRecord(record)
    }

    /**
     * Salva ou atualiza uma turma (classe) no Firestore.
     *
     * Operação assíncrona. Callback onBack é executado após sucesso.
     * Timestamp de criação é definido automaticamente.
     *
     * @param schoolClass Turma a salvar (ID será gerado se vazio)
     * @param onBack Callback executado quando operação completa
     *
     * @see TakStudRepository.saveClass
     */
    fun saveClass(schoolClass: Class, onBack: () -> Unit) {
        repository.saveClass(schoolClass, onBack)
    }

    /**
     * Deleta uma turma do Firestore.
     *
     * IMPORTANTE: Estudantes, notas e frequência não são deletados.
     * Operação é assíncrona.
     *
     * @param schoolClass Turma a deletar (deve ter ID válido)
     *
     * @see TakStudRepository.deleteClass
     */
    fun deleteClass(schoolClass: Class) {
        repository.deleteClass(schoolClass)
    }

    /**
     * Obtém estudantes de uma turma específica com query.
     *
     * Usa query do Repository com whereEqualTo.
     *
     * @param classId ID da turma
     *
     * @return StateFlow com estudantes da turma
     *
     * @see TakStudRepository.getStudentsByClass
     */
    fun getStudentsByClass(classId: String): StateFlow<List<Student>> {
        return repository.getStudentsByClass(classId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    /**
     * Obtém registros de frequência com query para turma e data.
     *
     * Usa query do Repository com dois whereEqualTo.
     *
     * @param classId ID da turma
     * @param date Data no formato "yyyy-MM-dd"
     *
     * @return StateFlow com registros de frequência
     *
     * @see TakStudRepository.getAttendanceRecordsByClassAndDate
     */
    fun getAttendanceByClassAndDate(classId: String, date: String): StateFlow<List<AttendanceRecord>> {
        return repository.getAttendanceRecordsByClassAndDate(classId, date).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

}
