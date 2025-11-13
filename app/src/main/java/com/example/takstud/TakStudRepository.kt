package com.example.takstud

import android.util.Log
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Class
import com.example.takstud.model.Grade
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.model.Student
import com.example.takstud.model.Task
import com.example.takstud.util.firestoreCollectionFlow
import com.example.takstud.util.firestoreQueryFlow
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositório central de dados para TakStud
 *
 * Implementa o padrão Repository para centralizar acesso a dados do Firestore.
 * Todos os dados são expostos como Flows para reatividade em tempo real.
 *
 * Arquitetura:
 * ```
 * ViewModel
 *     ↓
 * Repository (esta classe)
 *     ↓
 * Firebase Firestore
 * ```
 *
 * Características:
 * - Escuta em tempo real usando `addSnapshotListener`
 * - Tratamento de erros centralizado com logging
 * - Suporte a operações CRUD (Create, Read, Update, Delete)
 * - Conversão automática de Firestore documents para POJOs
 *
 * Exemplo de uso:
 * ```kotlin
 * val repository = TakStudRepository()
 * viewModelScope.launch {
 *     repository.getTasks().collect { tasks ->
 *         updateUI(tasks)
 *     }
 * }
 * ```
 *
 * @see TakStudViewModel
 * @see Task
 * @see Student
 */
class TakStudRepository {

    private val db = Firebase.firestore

    /**
     * Carrega todas as tarefas em tempo real.
     *
     * Escuta mudanças no Firestore e emite atualizações automaticamente.
     *
     * @return Flow que emite lista atualizada de tarefas
     *
     * @throws FirebaseFirestoreException se falhar conexão com Firestore
     *
     * Exemplo:
     * ```kotlin
     * repository.getTasks().collect { tasks ->
     *     println("Tarefas: $tasks")
     * }
     * ```
     *
     * @see Task
     * @see Flow
     */
    fun getTasks(): Flow<List<Task>> = firestoreCollectionFlow(
        db.collection("tasks"),
        Task::class.java,
        "TakStud"
    )

    /**
     * Carrega todos os avisos (notices) em tempo real.
     *
     * Avisos são comunicações dos professores para responsáveis.
     *
     * @return Flow que emite lista atualizada de avisos
     * @see Notice
     */
    fun getNotices(): Flow<List<Notice>> = firestoreCollectionFlow(
        db.collection("notices"),
        Notice::class.java,
        "TakStud"
    )

    /**
     * Carrega todos os horários de aula em tempo real.
     *
     * Horários definem quando cada turma tem aula em cada período (Manhã, Tarde, EJA).
     *
     * @return Flow que emite lista atualizada de horários
     * @see Schedule
     */
    fun getSchedules(): Flow<List<Schedule>> = firestoreCollectionFlow(
        db.collection("schedules"),
        Schedule::class.java,
        "TakStud"
    )

    /**
     * Carrega todos os estudantes em tempo real.
     *
     * Emite uma lista atualizada de estudantes sempre que houver mudanças no Firestore.
     * Cada estudante contém informações de matrícula, nome, classe e contato.
     *
     * @return Flow que emite lista atualizada de estudantes
     *
     * @throws FirebaseFirestoreException se falhar conexão com Firestore
     *
     * Exemplo:
     * ```kotlin
     * repository.getStudents().collect { students ->
     *     val names = students.map { it.name }
     *     displayStudents(names)
     * }
     * ```
     *
     * @see Student
     * @see Flow
     */
    fun getStudents(): Flow<List<Student>> = firestoreCollectionFlow(
        db.collection("students"),
        Student::class.java,
        "TakStud"
    )

    /**
     * Carrega todas as notas em tempo real.
     *
     * Emite notas de tarefas para estudantes. Cada nota contém:
     * - ID único (gerado como taskId-studentId)
     * - Score (0-100)
     * - Data de lançamento
     * - Referências para tarefa e estudante
     *
     * @return Flow que emite lista atualizada de notas
     *
     * @throws FirebaseFirestoreException se falhar conexão com Firestore
     *
     * Exemplo:
     * ```kotlin
     * repository.getGrades().collect { grades ->
     *     val average = grades.map { it.score.toDouble() }.average()
     *     updateGradeAverage(average)
     * }
     * ```
     *
     * @see Grade
     * @see Flow
     */
    fun getGrades(): Flow<List<Grade>> = firestoreCollectionFlow(
        db.collection("grades"),
        Grade::class.java,
        "TakStud"
    )

    /**
     * Carrega todos os registros de frequência em tempo real.
     *
     * Emite registros de presença/ausência de estudantes em aulas.
     * Cada registro contém:
     * - ID único (gerado como studentId-date)
     * - Status de presença (presente/ausente)
     * - Data da aula
     * - Referência para turma e estudante
     *
     * @return Flow que emite lista atualizada de registros de frequência
     *
     * @throws FirebaseFirestoreException se falhar conexão com Firestore
     *
     * Exemplo:
     * ```kotlin
     * repository.getAttendanceRecords().collect { records ->
     *     val presentDays = records.count { it.isPresent }
     *     updateAttendancePercentage(presentDays, records.size)
     * }
     * ```
     *
     * @see AttendanceRecord
     * @see Flow
     */
    fun getAttendanceRecords(): Flow<List<AttendanceRecord>> = firestoreCollectionFlow(
        db.collection("attendance"),
        AttendanceRecord::class.java,
        "TakStud"
    )

    /**
     * Carrega todas as turmas em tempo real.
     *
     * Emite lista de turmas (classes) cadastradas no sistema.
     * Cada turma contém:
     * - Identificação (série, letra, período)
     * - Quantidade de estudantes
     * - Dados de criação/atualização
     *
     * @return Flow que emite lista atualizada de turmas
     *
     * @throws FirebaseFirestoreException se falhar conexão com Firestore
     *
     * Exemplo:
     * ```kotlin
     * repository.getClasses().collect { classes ->
     *     val classNames = classes.map { "${it.grade}${it.letter}" }
     *     updateClassList(classNames)
     * }
     * ```
     *
     * @see Class
     * @see Flow
     */
    fun getClasses(): Flow<List<Class>> = firestoreCollectionFlow(
        db.collection("classes"),
        Class::class.java,
        "TakStud"
    )
    
    /**
     * Deleta uma tarefa do Firestore.
     *
     * Remove permanentemente a tarefa e seu ID do banco de dados.
     * Esta operação é irreversível. Notas associadas a esta tarefa
     * não são deletadas automaticamente.
     *
     * @param task Tarefa a ser deletada. Deve ter um ID válido
     *
     * Exemplo:
     * ```kotlin
     * val task = Task(id = "task_001", title = "Math Homework", ...)
     * repository.deleteTask(task)
     * ```
     *
     * @see Task
     */
    fun deleteTask(task: Task) {
        db.collection("tasks").document(task.id).delete()
    }

    /**
     * Deleta um aviso do Firestore.
     *
     * Remove permanentemente o aviso (notice) enviado pelo professor.
     * Esta operação é irreversível.
     *
     * @param notice Aviso a ser deletado. Deve ter um ID válido
     *
     * Exemplo:
     * ```kotlin
     * val notice = Notice(id = "notice_001", title = "Reporte de frequência", ...)
     * repository.deleteNotice(notice)
     * ```
     *
     * @see Notice
     */
    fun deleteNotice(notice: Notice) {
        db.collection("notices").document(notice.id).delete()
    }

    /**
     * Deleta um horário do Firestore.
     *
     * Remove permanentemente o horário de aula da turma.
     * Esta operação é irreversível.
     *
     * @param schedule Horário a ser deletado. Deve ter um ID válido
     *
     * Exemplo:
     * ```kotlin
     * val schedule = Schedule(id = "6A-MANHA", studentClass = "6A", ...)
     * repository.deleteSchedule(schedule)
     * ```
     *
     * @see Schedule
     */
    fun deleteSchedule(schedule: Schedule) {
        db.collection("schedules").document(schedule.id).delete()
    }

    /**
     * Salva ou atualiza um horário no Firestore.
     *
     * Se o horário não tem ID, um novo documento é criado.
     * Se tem ID, o documento existente é atualizado (merge).
     * O ID é gerado automaticamente como: turmaClass-periodo
     *
     * @param schedule Horário a ser salvo. Se ID vazio, será gerado
     * @param onComplete Callback executado quando a operação é concluída com sucesso
     *
     * Exemplo:
     * ```kotlin
     * val schedule = Schedule(
     *     id = "",
     *     studentClass = "6A",
     *     periodo = Periodo.MANHA,
     *     ...
     * )
     * repository.saveSchedule(schedule) {
     *     println("Horário salvo com sucesso!")
     * }
     * ```
     *
     * @throws FirebaseFirestoreException se falhar operação de escrita
     * @see Schedule
     */
    fun saveSchedule(schedule: Schedule, onComplete: () -> Unit) {
        val docId = if (schedule.id.isNotBlank()) schedule.id else "${schedule.studentClass.replace(" ", "")}-${schedule.periodo}"
        db.collection("schedules").document(docId).set(schedule.copy(id = docId)).addOnSuccessListener {
            onComplete()
        }
    }

    /**
     * Salva ou atualiza uma tarefa no Firestore.
     *
     * Se a tarefa não tem ID, um novo documento é criado automaticamente.
     * Se tem ID, o documento existente é atualizado.
     * O timestamp de criação é preservado em atualizações.
     *
     * @param task Tarefa a ser salva. Se ID vazio, será gerado
     * @param onComplete Callback executado quando a operação é concluída com sucesso
     *
     * Exemplo:
     * ```kotlin
     * val task = Task(
     *     id = "",
     *     title = "Atividade de Matemática",
     *     description = "Exercícios do capítulo 5",
     *     dueDate = System.currentTimeMillis(),
     *     ...
     * )
     * repository.saveTask(task) {
     *     println("Tarefa criada!")
     * }
     * ```
     *
     * @throws FirebaseFirestoreException se falhar operação de escrita
     * @see Task
     */
    fun saveTask(task: Task, onComplete: () -> Unit) {
        val taskRef = if (task.id.isBlank()) db.collection("tasks").document() else db.collection("tasks").document(task.id)
        taskRef.set(task.copy(id = taskRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    /**
     * Salva ou atualiza um aviso no Firestore.
     *
     * Avisos são comunicações importantes dos professores para pais/responsáveis.
     * Se o aviso não tem ID, um novo documento é criado.
     * Se tem ID, o documento existente é atualizado.
     *
     * @param notice Aviso a ser salvo. Se ID vazio, será gerado
     * @param onComplete Callback executado quando a operação é concluída com sucesso
     *
     * Exemplo:
     * ```kotlin
     * val notice = Notice(
     *     id = "",
     *     title = "Frequência baixa",
     *     message = "João está com frequência abaixo de 70%",
     *     createdAt = System.currentTimeMillis(),
     *     ...
     * )
     * repository.saveNotice(notice) {
     *     println("Aviso enviado aos responsáveis!")
     * }
     * ```
     *
     * @throws FirebaseFirestoreException se falhar operação de escrita
     * @see Notice
     */
    fun saveNotice(notice: Notice, onComplete: () -> Unit) {
         val noticeRef = if (notice.id.isBlank()) db.collection("notices").document() else db.collection("notices").document(notice.id)
        noticeRef.set(notice.copy(id = noticeRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    /**
     * Salva ou atualiza um estudante no Firestore.
     *
     * Cria um novo registro de estudante ou atualiza dados de um existente.
     * Se o estudante não tem ID, um novo documento é criado automaticamente.
     *
     * @param student Estudante a ser salvo. Se ID vazio, será gerado
     * @param onComplete Callback executado quando a operação é concluída com sucesso
     *
     * Exemplo:
     * ```kotlin
     * val student = Student(
     *     id = "",
     *     ra = "2024001",
     *     name = "João Silva",
     *     classId = "class_6A",
     *     email = "joao@email.com",
     *     ...
     * )
     * repository.saveStudent(student) {
     *     println("Estudante cadastrado no sistema!")
     * }
     * ```
     *
     * @throws FirebaseFirestoreException se falhar operação de escrita
     * @see Student
     */
    fun saveStudent(student: Student, onComplete: () -> Unit) {
        val studentRef = if (student.id.isBlank()) db.collection("students").document() else db.collection("students").document(student.id)
        studentRef.set(student.copy(id = studentRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    /**
     * Deleta um estudante do Firestore.
     *
     * Remove permanentemente o registro do estudante.
     * IMPORTANTE: Registros de notas e frequência associados
     * não são deletados automaticamente para manter histórico.
     *
     * @param student Estudante a ser deletado. Deve ter um ID válido
     *
     * Exemplo:
     * ```kotlin
     * val student = Student(id = "student_001", name = "João Silva", ...)
     * repository.deleteStudent(student)
     * ```
     *
     * @see Student
     */
    fun deleteStudent(student: Student) {
        db.collection("students").document(student.id).delete()
    }

    /**
     * Salva ou atualiza uma nota (grade) no Firestore.
     *
     * Notas são scores de tarefas para estudantes individuais.
     * Se a nota não tem ID, é gerado automaticamente como: taskId-studentId
     * Operação é síncrona (firestore-pending).
     *
     * @param grade Nota a ser salva. Se ID vazio, será gerado
     *
     * Exemplo:
     * ```kotlin
     * val grade = Grade(
     *     id = "",
     *     taskId = "task_001",
     *     studentId = "student_001",
     *     score = "85",
     *     classId = "class_6A",
     *     releaseDate = System.currentTimeMillis()
     * )
     * repository.saveGrade(grade)
     * ```
     *
     * @throws FirebaseFirestoreException se falhar operação de escrita
     * @see Grade
     */
    fun saveGrade(grade: Grade) {
        val docId = if(grade.id.isNotBlank()) grade.id else "${grade.taskId}-${grade.studentId}"
        db.collection("grades").document(docId).set(grade.copy(id = docId))
    }

    /**
     * Salva ou atualiza um registro de frequência no Firestore.
     *
     * Registra presença/ausência de um estudante em uma aula.
     * Se o registro não tem ID, é gerado automaticamente como: studentId-date
     * Operação é síncrona (firestore-pending).
     *
     * @param record Registro de frequência a ser salvo. Se ID vazio, será gerado
     *
     * Exemplo:
     * ```kotlin
     * val record = AttendanceRecord(
     *     id = "",
     *     studentId = "student_001",
     *     date = "2025-11-13",
     *     isPresent = true,
     *     classId = "class_6A",
     *     ...
     * )
     * repository.saveAttendanceRecord(record)
     * ```
     *
     * @throws FirebaseFirestoreException se falhar operação de escrita
     * @see AttendanceRecord
     */
    fun saveAttendanceRecord(record: AttendanceRecord) {
        val docId = if(record.id.isNotBlank()) record.id else "${record.studentId}-${record.date}"
        db.collection("attendance").document(docId).set(record.copy(id = docId))
    }

    /**
     * Salva ou atualiza uma turma (classe) no Firestore.
     *
     * Cria um novo registro de turma ou atualiza dados de uma existente.
     * Se a turma não tem ID, um novo documento é criado automaticamente.
     * O timestamp de criação é definido automaticamente para novas turmas.
     *
     * @param schoolClass Turma a ser salva. Se ID vazio, será gerado
     * @param onComplete Callback executado quando a operação é concluída com sucesso
     *
     * Exemplo:
     * ```kotlin
     * val schoolClass = Class(
     *     id = "",
     *     grade = "6",
     *     letter = "A",
     *     period = "MANHA",
     *     totalStudents = 35,
     *     ...
     * )
     * repository.saveClass(schoolClass) {
     *     println("Turma criada com sucesso!")
     * }
     * ```
     *
     * @throws FirebaseFirestoreException se falhar operação de escrita
     * @see Class
     */
    fun saveClass(schoolClass: Class, onComplete: () -> Unit) {
        val classRef = if (schoolClass.id.isBlank()) db.collection("classes").document() else db.collection("classes").document(schoolClass.id)
        classRef.set(schoolClass.copy(id = classRef.id, createdAt = System.currentTimeMillis())).addOnSuccessListener {
            onComplete()
        }
    }

    /**
     * Deleta uma turma do Firestore.
     *
     * Remove permanentemente o registro da turma.
     * IMPORTANTE: Estudantes, notas e registros de frequência
     * associados não são deletados automaticamente.
     *
     * @param schoolClass Turma a ser deletada. Deve ter um ID válido
     *
     * Exemplo:
     * ```kotlin
     * val schoolClass = Class(id = "class_6A", grade = "6", letter = "A", ...)
     * repository.deleteClass(schoolClass)
     * ```
     *
     * @see Class
     */
    fun deleteClass(schoolClass: Class) {
        db.collection("classes").document(schoolClass.id).delete()
    }

    /**
     * Carrega estudantes de uma turma específica em tempo real.
     *
     * Filtra estudantes pela classe usando whereEqualTo query.
     * Emite lista atualizada sempre que houver mudanças de estudantes nesta turma.
     *
     * @param classId ID da turma para filtrar estudantes
     *
     * @return Flow que emite lista atualizada de estudantes da turma
     *
     * @throws FirebaseFirestoreException se falhar query ou conexão
     *
     * Exemplo:
     * ```kotlin
     * repository.getStudentsByClass("class_6A").collect { students ->
     *     println("Estudantes da turma 6A: ${students.map { it.name }}")
     * }
     * ```
     *
     * @see Student
     * @see Flow
     */
    fun getStudentsByClass(classId: String): Flow<List<Student>> = firestoreQueryFlow(
        db.collection("students").whereEqualTo("classId", classId),
        Student::class.java,
        "TakStud"
    )

    /**
     * Carrega registros de frequência de uma turma em uma data específica.
     *
     * Filtra por turma E data usando two whereEqualTo queries.
     * Útil para carregar presença de toda a turma em um dia específico.
     *
     * @param classId ID da turma
     * @param date Data no formato "yyyy-MM-dd" ou similar
     *
     * @return Flow que emite lista atualizada de registros de frequência
     *
     * @throws FirebaseFirestoreException se falhar query ou conexão
     *
     * Exemplo:
     * ```kotlin
     * val today = SimpleDateFormat("yyyy-MM-dd").format(Date())
     * repository.getAttendanceRecordsByClassAndDate("class_6A", today).collect { records ->
     *     val presentCount = records.count { it.isPresent }
     *     val percentage = (presentCount * 100) / records.size
     *     println("Presença: $percentage%")
     * }
     * ```
     *
     * @see AttendanceRecord
     * @see Flow
     */
    fun getAttendanceRecordsByClassAndDate(classId: String, date: String): Flow<List<AttendanceRecord>> = firestoreQueryFlow(
        db.collection("attendance")
            .whereEqualTo("classId", classId)
            .whereEqualTo("date", date),
        AttendanceRecord::class.java,
        "TakStud"
    )

    /**
     * Carrega turmas únicas agrupadas por período escolar em tempo real.
     *
     * Extrai dados de horários (schedules) e agrupa turmas únicas por período:
     * - MANHA (Período da manhã)
     * - TARDE (Período da tarde)
     * - EJA (Educação de Jovens e Adultos)
     *
     * Útil para populating dropdowns ou seletores de turmas por período.
     * Emite mapa atualizado sempre que horários mudam.
     *
     * @return Flow que emite Map<periodoName, listaDeTurmas>
     *         Exemplo: {"MANHA" -> ["6A", "6B", "7A"], "TARDE" -> ["8A", "8B"]}
     *
     * @throws FirebaseFirestoreException se falhar conexão com Firestore
     *
     * Exemplo:
     * ```kotlin
     * repository.getClassesByPeriod().collect { classesByPeriod ->
     *     val morningClasses = classesByPeriod["MANHA"] ?: emptyList()
     *     val afternoonClasses = classesByPeriod["TARDE"] ?: emptyList()
     *     println("Turmas da manhã: $morningClasses")
     *     println("Turmas da tarde: $afternoonClasses")
     * }
     * ```
     *
     * @see Schedule
     * @see Flow
     */
    fun getClassesByPeriod(): Flow<Map<String, List<String>>> =
        firestoreCollectionFlow(
            db.collection("schedules"),
            Schedule::class.java,
            "TakStud"
        ).map { schedules ->
            // Agrupar por período e extrair turmas únicas
            schedules
                .groupBy { it.periodo.name }
                .mapValues { (_, scheduleList) ->
                    scheduleList
                        .map { it.studentClass }
                        .distinct()
                        .sorted()
                }
        }
}
