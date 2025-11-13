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

    fun getStudents(): Flow<List<Student>> = firestoreCollectionFlow(
        db.collection("students"),
        Student::class.java,
        "TakStud"
    )

    fun getGrades(): Flow<List<Grade>> = firestoreCollectionFlow(
        db.collection("grades"),
        Grade::class.java,
        "TakStud"
    )

    fun getAttendanceRecords(): Flow<List<AttendanceRecord>> = firestoreCollectionFlow(
        db.collection("attendance"),
        AttendanceRecord::class.java,
        "TakStud"
    )

    fun getClasses(): Flow<List<Class>> = firestoreCollectionFlow(
        db.collection("classes"),
        Class::class.java,
        "TakStud"
    )
    
    fun deleteTask(task: Task) {
        db.collection("tasks").document(task.id).delete()
    }

    fun deleteNotice(notice: Notice) {
        db.collection("notices").document(notice.id).delete()
    }

    fun deleteSchedule(schedule: Schedule) {
        db.collection("schedules").document(schedule.id).delete()
    }

    fun saveSchedule(schedule: Schedule, onComplete: () -> Unit) {
        val docId = if (schedule.id.isNotBlank()) schedule.id else "${schedule.studentClass.replace(" ", "")}-${schedule.periodo}"
        db.collection("schedules").document(docId).set(schedule.copy(id = docId)).addOnSuccessListener {
            onComplete()
        }
    }

    fun saveTask(task: Task, onComplete: () -> Unit) {
        val taskRef = if (task.id.isBlank()) db.collection("tasks").document() else db.collection("tasks").document(task.id)
        taskRef.set(task.copy(id = taskRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    fun saveNotice(notice: Notice, onComplete: () -> Unit) {
         val noticeRef = if (notice.id.isBlank()) db.collection("notices").document() else db.collection("notices").document(notice.id)
        noticeRef.set(notice.copy(id = noticeRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    fun saveStudent(student: Student, onComplete: () -> Unit) {
        val studentRef = if (student.id.isBlank()) db.collection("students").document() else db.collection("students").document(student.id)
        studentRef.set(student.copy(id = studentRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    fun deleteStudent(student: Student) {
        db.collection("students").document(student.id).delete()
    }

    fun saveGrade(grade: Grade) {
        val docId = if(grade.id.isNotBlank()) grade.id else "${grade.taskId}-${grade.studentId}"
        db.collection("grades").document(docId).set(grade.copy(id = docId))
    }

    fun saveAttendanceRecord(record: AttendanceRecord) {
        val docId = if(record.id.isNotBlank()) record.id else "${record.studentId}-${record.date}"
        db.collection("attendance").document(docId).set(record.copy(id = docId))
    }

    fun saveClass(schoolClass: Class, onComplete: () -> Unit) {
        val classRef = if (schoolClass.id.isBlank()) db.collection("classes").document() else db.collection("classes").document(schoolClass.id)
        classRef.set(schoolClass.copy(id = classRef.id, createdAt = System.currentTimeMillis())).addOnSuccessListener {
            onComplete()
        }
    }

    fun deleteClass(schoolClass: Class) {
        db.collection("classes").document(schoolClass.id).delete()
    }

    fun getStudentsByClass(classId: String): Flow<List<Student>> = firestoreQueryFlow(
        db.collection("students").whereEqualTo("classId", classId),
        Student::class.java,
        "TakStud"
    )

    fun getAttendanceRecordsByClassAndDate(classId: String, date: String): Flow<List<AttendanceRecord>> = firestoreQueryFlow(
        db.collection("attendance")
            .whereEqualTo("classId", classId)
            .whereEqualTo("date", date),
        AttendanceRecord::class.java,
        "TakStud"
    )

    // Extrair turmas únicas dos Schedule agrupadas por período
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
