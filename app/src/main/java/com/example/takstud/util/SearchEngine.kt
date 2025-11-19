package com.example.takstud.util

import com.example.takstud.model.*
import kotlin.math.min

/**
 * SearchEngine - Motor de busca e filtro avançado.
 *
 * FUNCIONALIDADES:
 * - Busca full-text em múltiplos modelos
 * - Filtros combinados (AND/OR)
 * - Busca fuzzy para erros de digitação
 * - Ordenação por relevância
 * - Suporte a múltiplos idiomas
 *
 * TIPOS DE BUSCA:
 * - Students: por nome, RA, turma
 * - Tasks: por título, descrição
 * - Grades: por valor, estudante, tarefa
 * - Attendance: por data, estudante, status
 * - Notices: por título, conteúdo
 *
 * EXEMPLO DE USO:
 * val engine = SearchEngine()
 * val results = engine.searchStudents("João Silva", filters = mapOf("class" to "7A"))
 * val fuzzyResults = engine.searchWithFuzzy(students, "Joao Silva")
 */
object SearchEngine {

    /**
     * Busca de estudantes por nome ou RA.
     */
    fun searchStudents(
        students: List<Student>,
        query: String,
        filters: Map<String, String> = emptyMap()
    ): List<SearchResult<Student>> {
        val normalizedQuery = query.lowercase().trim()
        if (normalizedQuery.isEmpty()) return emptyList()

        return students
            .asSequence()
            .filter { student ->
                val matchesQuery = student.name.lowercase().contains(normalizedQuery) ||
                    student.ra.lowercase().contains(normalizedQuery)

                if (!matchesQuery) return@filter false

                // Aplicar filtros adicionais
                filters.all { (key, value) ->
                    when (key) {
                        "class" -> student.classId == value || student.studentClass == value
                        else -> true
                    }
                }
            }
            .map { student ->
                SearchResult(
                    data = student,
                    relevance = calculateRelevance(student.name, normalizedQuery) +
                        calculateRelevance(student.ra, normalizedQuery)
                )
            }
            .sortedByDescending { it.relevance }
            .toList()
    }

    /**
     * Busca de tarefas por título ou descrição.
     */
    fun searchTasks(
        tasks: List<Task>,
        query: String,
        filters: Map<String, String> = emptyMap()
    ): List<SearchResult<Task>> {
        val normalizedQuery = query.lowercase().trim()
        if (normalizedQuery.isEmpty()) return emptyList()

        return tasks
            .asSequence()
            .filter { task ->
                val matchesQuery = task.title.lowercase().contains(normalizedQuery) ||
                    task.description.lowercase().contains(normalizedQuery)

                if (!matchesQuery) return@filter false

                // Aplicar filtros
                filters.all { (key, value) ->
                    when (key) {
                        "class" -> task.studentClass == value
                        else -> true
                    }
                }
            }
            .map { task ->
                SearchResult(
                    data = task,
                    relevance = calculateRelevance(task.title, normalizedQuery) +
                        calculateRelevance(task.description, normalizedQuery)
                )
            }
            .sortedByDescending { it.relevance }
            .toList()
    }

    /**
     * Busca de notas por estudante, tarefa ou valor.
     */
    fun searchGrades(
        grades: List<Grade>,
        query: String,
        filters: Map<String, String> = emptyMap()
    ): List<SearchResult<Grade>> {
        val normalizedQuery = query.lowercase().trim()

        return grades
            .asSequence()
            .filter { grade ->
                if (normalizedQuery.isEmpty()) {
                    true
                } else {
                    grade.studentId.contains(normalizedQuery, ignoreCase = true) ||
                        grade.taskId.contains(normalizedQuery, ignoreCase = true) ||
                        grade.value.contains(normalizedQuery)
                }
            }
            .filter { grade ->
                // Aplicar filtros
                filters.all { (key, value) ->
                    when (key) {
                        "studentId" -> grade.studentId == value
                        "taskId" -> grade.taskId == value
                        "minValue" -> grade.value.toDoubleOrNull()?.let { it >= value.toDouble() } ?: true
                        "maxValue" -> grade.value.toDoubleOrNull()?.let { it <= value.toDouble() } ?: true
                        else -> true
                    }
                }
            }
            .map { grade ->
                SearchResult(
                    data = grade,
                    relevance = 1.0  // Grades não têm ranking por relevância
                )
            }
            .toList()
    }

    /**
     * Busca de registros de frequência por data ou estudante.
     */
    fun searchAttendance(
        records: List<AttendanceRecord>,
        query: String,
        filters: Map<String, String> = emptyMap()
    ): List<SearchResult<AttendanceRecord>> {
        val normalizedQuery = query.lowercase().trim()

        return records
            .asSequence()
            .filter { record ->
                if (normalizedQuery.isEmpty()) {
                    true
                } else {
                    record.studentName.lowercase().contains(normalizedQuery) ||
                        record.studentRa.contains(normalizedQuery) ||
                        record.date.contains(normalizedQuery)
                }
            }
            .filter { record ->
                // Aplicar filtros
                filters.all { (key, value) ->
                    when (key) {
                        "studentId" -> record.studentId == value
                        "classId" -> record.classId == value
                        "dateFrom" -> record.date >= value
                        "dateTo" -> record.date <= value
                        "isPresent" -> record.isPresent == value.toBoolean()
                        else -> true
                    }
                }
            }
            .map { record ->
                SearchResult(
                    data = record,
                    relevance = calculateRelevance(record.studentName, normalizedQuery)
                )
            }
            .sortedByDescending { it.relevance }
            .toList()
    }

    /**
     * Busca fuzzy para tolerar erros de digitação.
     */
    fun <T> searchWithFuzzy(
        items: List<T>,
        query: String,
        fieldExtractor: (T) -> String,
        threshold: Double = 0.8
    ): List<SearchResult<T>> {
        val normalizedQuery = query.lowercase().trim()
        if (normalizedQuery.isEmpty()) return emptyList()

        return items
            .mapNotNull { item ->
                val fieldValue = fieldExtractor(item).lowercase()
                val similarity = calculateFuzzySimilarity(normalizedQuery, fieldValue)

                if (similarity >= threshold) {
                    SearchResult(data = item, relevance = similarity)
                } else {
                    null
                }
            }
            .sortedByDescending { it.relevance }
    }

    /**
     * Filtro avançado com múltiplas condições.
     */
    fun <T> advancedFilter(
        items: List<T>,
        conditions: Map<String, (T) -> Boolean>
    ): List<T> {
        return items.filter { item ->
            conditions.all { (_, predicate) -> predicate(item) }
        }
    }

    /**
     * Calcula relevância de um match.
     */
    private fun calculateRelevance(text: String, query: String): Double {
        val lower = text.lowercase()
        val queryLower = query.lowercase()

        return when {
            lower == queryLower -> 100.0  // Match exato
            lower.startsWith(queryLower) -> 50.0  // Começa com
            lower.contains(queryLower) -> 25.0   // Contém
            else -> 0.0
        }
    }

    /**
     * Calcula similaridade fuzzy (Levenshtein distance).
     */
    private fun calculateFuzzySimilarity(query: String, target: String): Double {
        val distance = levenshteinDistance(query, target)
        val maxLength = maxOf(query.length, target.length)

        return if (maxLength == 0) 1.0 else 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * Calcula distância de Levenshtein entre duas strings.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1

        if (longer.isEmpty()) return 0

        val costs = IntArray(shorter.length + 1) { it }

        for (i in 1..longer.length) {
            var newValue = i
            for (j in 1..shorter.length) {
                val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
                newValue = min(min(1 + costs[j], 1 + newValue), costs[j - 1] + cost)
                costs[j - 1] = newValue
            }
            costs[shorter.length] = newValue
        }

        return costs[shorter.length]
    }
}

/**
 * Resultado de busca com relevância.
 */
data class SearchResult<T>(
    val data: T,
    val relevance: Double = 0.0
) {
    /**
     * Score de 0-100 para exibição.
     */
    val relevanceScore: Int
        get() = (relevance * 100).toInt().coerceIn(0, 100)
}

/**
 * Construtor de filtros com interface fluida.
 */
class FilterBuilder<T> {
    private val conditions = mutableMapOf<String, (T) -> Boolean>()

    /**
     * Adiciona condição de filtro.
     */
    fun condition(name: String, predicate: (T) -> Boolean) = apply {
        conditions[name] = predicate
    }

    /**
     * Aplica o filtro a uma lista.
     */
    fun apply(items: List<T>): List<T> {
        return items.filter { item ->
            conditions.all { (_, predicate) -> predicate(item) }
        }
    }
}

/**
 * Filtros pré-definidos para modelos comuns.
 */
object PredefinedFilters {

    /**
     * Filtros para estudantes.
     */
    object Students {
        fun byClass(classId: String): (Student) -> Boolean = { it.classId == classId }
        fun byName(query: String): (Student) -> Boolean = { it.name.contains(query, ignoreCase = true) }
    }

    /**
     * Filtros para notas.
     */
    object Grades {
        fun byStudent(studentId: String): (Grade) -> Boolean = { it.studentId == studentId }
        fun byTask(taskId: String): (Grade) -> Boolean = { it.taskId == taskId }
        fun minValue(min: Double): (Grade) -> Boolean = { (it.value.toDoubleOrNull() ?: 0.0) >= min }
        fun maxValue(max: Double): (Grade) -> Boolean = { (it.value.toDoubleOrNull() ?: 0.0) <= max }
    }

    /**
     * Filtros para frequência.
     */
    object Attendance {
        fun byStudent(studentId: String): (AttendanceRecord) -> Boolean = { it.studentId == studentId }
        fun byClass(classId: String): (AttendanceRecord) -> Boolean = { it.classId == classId }
        fun byDate(date: String): (AttendanceRecord) -> Boolean = { it.date == date }
        fun present(): (AttendanceRecord) -> Boolean = { it.isPresent }
        fun absent(): (AttendanceRecord) -> Boolean = { !it.isPresent }
    }

    /**
     * Filtros para tarefas.
     */
    object Tasks {
        fun byClass(classId: String): (Task) -> Boolean = { it.studentClass == classId }
        fun overdue(): (Task) -> Boolean = { it.dueDate < System.currentTimeMillis().toString() }
    }
}
