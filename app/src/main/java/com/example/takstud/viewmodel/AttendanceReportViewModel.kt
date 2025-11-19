package com.example.takstud.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.takstud.TakStudRepository
import com.example.takstud.model.*
import com.example.takstud.ui.common.BaseViewModel
import com.example.takstud.ui.common.UiState
import com.example.takstud.util.AttendanceReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * AttendanceReportViewModel - Gerencia geração e exibição de relatórios de frequência.
 *
 * FUNCIONALIDADES:
 * - Carregamento de dados de frequência
 * - Geração de relatórios (individual, detalhado, por turma)
 * - Gerenciamento de filtros (período, estudante, turma)
 * - Exportação de dados
 * - Tratamento de erros com retry automático
 *
 * ESTADOS:
 * - Loading: Gerando relatório
 * - Success: Relatório gerado com sucesso
 * - Error: Falha na geração
 * - Empty: Sem dados disponíveis
 *
 * EXEMPLO DE USO:
 * class AttendanceScreen(viewModel: AttendanceReportViewModel) {
 *     val uiState by viewModel.uiState.collectAsState()
 *
 *     when (uiState) {
 *         is UiState.Loading -> CircularProgressIndicator()
 *         is UiState.Success -> DisplayReport((uiState as UiState.Success).data)
 *         is UiState.Error -> ErrorMessage((uiState as UiState.Error).message)
 *         is UiState.Empty -> EmptyState()
 *     }
 * }
 */
class AttendanceReportViewModel(
    private val repository: TakStudRepository = TakStudRepository()
) : BaseViewModel<AttendanceReport>() {

    // Estado específico para relatórios detalhados
    private val _detailedReportState = MutableStateFlow<UiState<DetailedAttendanceReport>>(UiState.Empty())
    val detailedReportState: StateFlow<UiState<DetailedAttendanceReport>> = _detailedReportState.asStateFlow()

    // Estado para relatório de turma
    private val _classReportState = MutableStateFlow<UiState<ClassAttendanceReport>>(UiState.Empty())
    val classReportState: StateFlow<UiState<ClassAttendanceReport>> = _classReportState.asStateFlow()

    // Filtros atuais
    private val _currentFilters = MutableStateFlow(ReportFilters())
    val currentFilters: StateFlow<ReportFilters> = _currentFilters.asStateFlow()

    // Período de datas para filtro
    data class ReportFilters(
        val studentId: String = "",
        val classId: String = "",
        val period: String = "",
        val startDate: String = "",
        val endDate: String = "",
        val reportType: ReportType = ReportType.STUDENT
    )

    enum class ReportType {
        STUDENT,      // Relatório de um estudante
        DETAILED,     // Relatório detalhado com padrões
        CLASS,        // Relatório agregado de turma
        ALL_CLASSES   // Relatório de todas as turmas
    }

    /**
     * Carrega e gera relatório de frequência para um estudante.
     */
    fun loadStudentReport(
        studentId: String,
        period: String = "Período Atual",
        startDate: String = "",
        endDate: String = ""
    ) = launchUI("Carregando relatório...") {
        try {
            val records = repository.getAttendanceRecords().first()

            if (records.isEmpty()) {
                setEmpty("Nenhum registro de frequência disponível")
                return@launchUI
            }

            val report = AttendanceReportGenerator.generateStudentReport(
                records = records,
                studentId = studentId,
                period = period,
                startDate = startDate,
                endDate = endDate
            )

            if (report.totalDays == 0) {
                setEmpty("Nenhum dado de frequência para este período")
            } else {
                setSuccess(report, "Relatório carregado com sucesso")
            }

            // Atualizar filtros
            _currentFilters.value = ReportFilters(
                studentId = studentId,
                period = period,
                startDate = startDate,
                endDate = endDate,
                reportType = ReportType.STUDENT
            )

        } catch (e: Exception) {
            setError(
                "Erro ao carregar relatório: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Carrega e gera relatório detalhado com análise de padrões.
     */
    fun loadDetailedReport(
        studentId: String,
        period: String = "Período Atual",
        startDate: String = "",
        endDate: String = ""
    ) = viewModelScope.launch {
        try {
            _detailedReportState.value = UiState.Loading(null, "Analisando padrões...")

            val records = repository.getAttendanceRecords().first()

            if (records.isEmpty()) {
                _detailedReportState.value = UiState.Empty("Nenhum registro disponível")
                return@launch
            }

            val report = AttendanceReportGenerator.generateDetailedReport(
                records = records,
                studentId = studentId,
                period = period,
                startDate = startDate,
                endDate = endDate
            )

            if (report.baseReport.totalDays == 0) {
                _detailedReportState.value = UiState.Empty("Nenhum dado para este período")
            } else {
                _detailedReportState.value = UiState.Success(report, "Análise completa")
            }

            // Atualizar estado base também
            setSuccess(report.baseReport)

        } catch (e: Exception) {
            _detailedReportState.value = UiState.Error(
                "Erro ao gerar análise: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Carrega e gera relatório agregado para uma turma.
     */
    fun loadClassReport(
        classId: String,
        period: String = "Período Atual",
        startDate: String = "",
        endDate: String = ""
    ) = viewModelScope.launch {
        try {
            _classReportState.value = UiState.Loading(null, "Processando dados da turma...")

            val records = repository.getAttendanceRecords().first()

            if (records.isEmpty()) {
                _classReportState.value = UiState.Empty("Nenhum registro na turma")
                return@launch
            }

            val report = AttendanceReportGenerator.generateClassReport(
                records = records,
                classId = classId,
                period = period,
                startDate = startDate,
                endDate = endDate
            )

            if (report.totalStudents == 0) {
                _classReportState.value = UiState.Empty("Nenhum estudante na turma")
            } else {
                _classReportState.value = UiState.Success(report, "Relatório gerado")
            }

            _currentFilters.value = ReportFilters(
                classId = classId,
                period = period,
                startDate = startDate,
                endDate = endDate,
                reportType = ReportType.CLASS
            )

        } catch (e: Exception) {
            _classReportState.value = UiState.Error(
                "Erro ao processar turma: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Atualiza período e recarrega relatório.
     */
    fun updatePeriod(
        startDate: String,
        endDate: String,
        period: String = "Personalizado"
    ) {
        val filters = _currentFilters.value
        when (filters.reportType) {
            ReportType.STUDENT -> loadStudentReport(
                filters.studentId,
                period,
                startDate,
                endDate
            )
            ReportType.DETAILED -> loadDetailedReport(
                filters.studentId,
                period,
                startDate,
                endDate
            )
            ReportType.CLASS -> loadClassReport(
                filters.classId,
                period,
                startDate,
                endDate
            )
            else -> {}
        }
    }

    /**
     * Exporta relatório atual em CSV.
     */
    fun exportCurrentReport(): String? {
        val classState = _classReportState.value
        return if (classState is UiState.Success) {
            AttendanceReportGenerator.exportToCSV(classState.data)
        } else {
            null
        }
    }

    /**
     * Gera período automático para este mês.
     */
    fun generateMonthPeriod(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = formatDateToString(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = formatDateToString(calendar.time)

        return startDate to endDate
    }

    /**
     * Gera período automático para este trimestre.
     */
    fun generateQuarterPeriod(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)

        val startMonth = (month / 3) * 3
        calendar.set(Calendar.MONTH, startMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = formatDateToString(calendar.time)

        calendar.set(Calendar.MONTH, startMonth + 2)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = formatDateToString(calendar.time)

        return startDate to endDate
    }

    /**
     * Formata data para string yyyy-MM-dd.
     */
    private fun formatDateToString(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    /**
     * Retry da última operação (implementado em BaseViewModel).
     */
    override fun retry() {
        val filters = _currentFilters.value
        when (filters.reportType) {
            ReportType.STUDENT -> loadStudentReport(
                filters.studentId,
                filters.period,
                filters.startDate,
                filters.endDate
            )
            ReportType.DETAILED -> loadDetailedReport(
                filters.studentId,
                filters.period,
                filters.startDate,
                filters.endDate
            )
            ReportType.CLASS -> loadClassReport(
                filters.classId,
                filters.period,
                filters.startDate,
                filters.endDate
            )
            else -> {}
        }
    }
}
