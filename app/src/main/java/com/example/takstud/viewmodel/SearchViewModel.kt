package com.example.takstud.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.AttendanceRepository
import com.example.takstud.data.repository.GradeRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.data.repository.TaskRepository
import com.example.takstud.model.*
import com.example.takstud.ui.common.BaseViewModel
import com.example.takstud.ui.common.UiState
import com.example.takstud.util.SearchEngine
import com.example.takstud.util.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SearchViewModel - Gerencia busca e filtros avançados.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val studentRepository: StudentRepository,
    private val gradeRepository: GradeRepository,
    private val attendanceRepository: AttendanceRepository
) : BaseViewModel<List<Any>>() {

    // Resultados de busca por tipo
    private val _studentResults = MutableStateFlow<UiState<List<SearchResult<Student>>>>(UiState.Empty())
    val studentResults: StateFlow<UiState<List<SearchResult<Student>>>> = _studentResults.asStateFlow()

    private val _taskResults = MutableStateFlow<UiState<List<SearchResult<Task>>>>(UiState.Empty())
    val taskResults: StateFlow<UiState<List<SearchResult<Task>>>> = _taskResults.asStateFlow()

    private val _gradeResults = MutableStateFlow<UiState<List<SearchResult<Grade>>>>(UiState.Empty())
    val gradeResults: StateFlow<UiState<List<SearchResult<Grade>>>> = _gradeResults.asStateFlow()

    private val _attendanceResults = MutableStateFlow<UiState<List<SearchResult<AttendanceRecord>>>>(UiState.Empty())
    val attendanceResults: StateFlow<UiState<List<SearchResult<AttendanceRecord>>>> = _attendanceResults.asStateFlow()

    // Query e filtros atuais
    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    private val _currentFilters = MutableStateFlow<Map<String, String>>(emptyMap())
    val currentFilters: StateFlow<Map<String, String>> = _currentFilters.asStateFlow()

    // Histórico de buscas
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // Modo de busca
    enum class SearchMode {
        STUDENTS,
        TASKS,
        GRADES,
        ATTENDANCE,
        ALL
    }

    private val _searchMode = MutableStateFlow(SearchMode.ALL)
    val searchMode: StateFlow<SearchMode> = _searchMode.asStateFlow()

    /**
     * Busca estudantes por nome ou RA.
     */
    fun searchStudents(query: String) = viewModelScope.launch {
        try {
            _currentQuery.value = query
            _searchMode.value = SearchMode.STUDENTS
            _studentResults.value = UiState.Loading(null, "Buscando estudantes...")

            val students = studentRepository.getStudents().first()
            val results = SearchEngine.searchStudents(
                students = students,
                query = query,
                filters = _currentFilters.value
            )

            if (results.isEmpty()) {
                _studentResults.value = UiState.Empty("Nenhum estudante encontrado")
            } else {
                _studentResults.value = UiState.Success(results, "Encontrados ${results.size} estudantes")
            }

            addToSearchHistory(query)

        } catch (e: Exception) {
            _studentResults.value = UiState.Error(
                "Erro na busca: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Busca tarefas por título ou descrição.
     */
    fun searchTasks(query: String) = viewModelScope.launch {
        try {
            _currentQuery.value = query
            _searchMode.value = SearchMode.TASKS
            _taskResults.value = UiState.Loading(null, "Buscando tarefas...")

            val tasks = taskRepository.getTasks().first()
            // Adaptador temporário se SearchEngine ainda esperar Task antigo, 
            // ou atualizar SearchEngine. Por enquanto, vamos assumir que SearchEngine precisa ser atualizado também.
            // Mas como SearchEngine não foi migrado ainda, vamos fazer um map simples se necessário ou atualizar SearchEngine.
            // Vamos atualizar SearchEngine no próximo passo.
            val results = SearchEngine.searchTasks(
                tasks = tasks, // Isso vai quebrar se SearchEngine esperar List<Task>
                query = query,
                filters = _currentFilters.value
            )

            if (results.isEmpty()) {
                _taskResults.value = UiState.Empty("Nenhuma tarefa encontrada")
            } else {
                _taskResults.value = UiState.Success(results, "Encontradas ${results.size} tarefas")
            }

            addToSearchHistory(query)

        } catch (e: Exception) {
            _taskResults.value = UiState.Error(
                "Erro na busca: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Busca notas por estudante ou valor.
     */
    fun searchGrades(query: String) = viewModelScope.launch {
        try {
            _currentQuery.value = query
            _searchMode.value = SearchMode.GRADES
            _gradeResults.value = UiState.Loading(null, "Buscando notas...")

            val grades = gradeRepository.getGrades().first()
            val results = SearchEngine.searchGrades(
                grades = grades,
                query = query,
                filters = _currentFilters.value
            )

            if (results.isEmpty()) {
                _gradeResults.value = UiState.Empty("Nenhuma nota encontrada")
            } else {
                _gradeResults.value = UiState.Success(results, "Encontradas ${results.size} notas")
            }

            addToSearchHistory(query)

        } catch (e: Exception) {
            _gradeResults.value = UiState.Error(
                "Erro na busca: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Busca registros de frequência por estudante ou data.
     */
    fun searchAttendance(query: String) = viewModelScope.launch {
        try {
            _currentQuery.value = query
            _searchMode.value = SearchMode.ATTENDANCE
            _attendanceResults.value = UiState.Loading(null, "Buscando frequência...")

            val records = attendanceRepository.getAttendanceRecords().first()
            val results = SearchEngine.searchAttendance(
                records = records,
                query = query,
                filters = _currentFilters.value
            )

            if (results.isEmpty()) {
                _attendanceResults.value = UiState.Empty("Nenhum registro encontrado")
            } else {
                _attendanceResults.value = UiState.Success(results, "Encontrados ${results.size} registros")
            }

            addToSearchHistory(query)

        } catch (e: Exception) {
            _attendanceResults.value = UiState.Error(
                "Erro na busca: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Busca em todos os tipos simultaneamente.
     */
    fun searchAll(query: String) {
        _searchMode.value = SearchMode.ALL
        searchStudents(query)
        searchTasks(query)
        searchGrades(query)
        searchAttendance(query)
    }

    /**
     * Adiciona filtro à busca atual.
     */
    fun addFilter(key: String, value: String) {
        val newFilters = _currentFilters.value.toMutableMap()
        newFilters[key] = value
        _currentFilters.value = newFilters

        // Re-executar busca com novos filtros
        val query = _currentQuery.value
        if (query.isNotEmpty()) {
            when (_searchMode.value) {
                SearchMode.STUDENTS -> searchStudents(query)
                SearchMode.TASKS -> searchTasks(query)
                SearchMode.GRADES -> searchGrades(query)
                SearchMode.ATTENDANCE -> searchAttendance(query)
                SearchMode.ALL -> searchAll(query)
            }
        }
    }

    /**
     * Remove um filtro.
     */
    fun removeFilter(key: String) {
        val newFilters = _currentFilters.value.toMutableMap()
        newFilters.remove(key)
        _currentFilters.value = newFilters

        // Re-executar busca
        val query = _currentQuery.value
        if (query.isNotEmpty()) {
            when (_searchMode.value) {
                SearchMode.STUDENTS -> searchStudents(query)
                SearchMode.TASKS -> searchTasks(query)
                SearchMode.GRADES -> searchGrades(query)
                SearchMode.ATTENDANCE -> searchAttendance(query)
                SearchMode.ALL -> searchAll(query)
            }
        }
    }

    /**
     * Limpa todos os filtros.
     */
    fun clearFilters() {
        _currentFilters.value = emptyMap()
    }

    /**
     * Adiciona query ao histórico.
     */
    private fun addToSearchHistory(query: String) {
        val history = _searchHistory.value.toMutableList()
        history.remove(query)  // Remove se já existir
        history.add(0, query)  // Adiciona no topo
        _searchHistory.value = history.take(10)  // Mantém últimas 10
    }

    /**
     * Busca a partir do histórico.
     */
    fun searchFromHistory(query: String) {
        when (_searchMode.value) {
            SearchMode.STUDENTS -> searchStudents(query)
            SearchMode.TASKS -> searchTasks(query)
            SearchMode.GRADES -> searchGrades(query)
            SearchMode.ATTENDANCE -> searchAttendance(query)
            SearchMode.ALL -> searchAll(query)
        }
    }

    /**
     * Limpa o histórico de buscas.
     */
    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    /**
     * Obtém total de resultados.
     */
    fun getTotalResults(): Int {
        var total = 0
        (studentResults.value as? UiState.Success)?.data?.size?.let { total += it }
        (taskResults.value as? UiState.Success)?.data?.size?.let { total += it }
        (gradeResults.value as? UiState.Success)?.data?.size?.let { total += it }
        (attendanceResults.value as? UiState.Success)?.data?.size?.let { total += it }
        return total
    }

    /**
     * Retry da última operação.
     */
    override fun retry() {
        val query = _currentQuery.value
        if (query.isNotEmpty()) {
            when (_searchMode.value) {
                SearchMode.STUDENTS -> searchStudents(query)
                SearchMode.TASKS -> searchTasks(query)
                SearchMode.GRADES -> searchGrades(query)
                SearchMode.ATTENDANCE -> searchAttendance(query)
                SearchMode.ALL -> searchAll(query)
            }
        }
    }
}
