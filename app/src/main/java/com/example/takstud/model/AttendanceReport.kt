package com.example.takstud.model

import androidx.compose.runtime.Stable

/**
 * AttendanceReport - Representa um relatório de frequência resumido.
 *
 * FUNCIONALIDADES:
 * - Resumo de frequência por período
 * - Cálculo de percentual de presença
 * - Identificação de padrões de faltas
 * - Dados agregados para análise
 *
 * EXEMPLO DE USO:
 * val report = AttendanceReport(
 *     studentId = "student1",
 *     studentName = "João Silva",
 *     period = "Q1 2025",
 *     totalDays = 50,
 *     presentDays = 48,
 *     absentDays = 2,
 *     justifiedAbsences = 1,
 *     unjustifiedAbsences = 1
 * )
 *
 * val attendance = report.attendancePercentage  // 96.0%
 */

/**
 * Relatório de frequência de um estudante.
 */
@Stable
data class AttendanceReport(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentRa: String = "",
    val classId: String = "",
    val className: String = "",
    val period: String = "",  // "Q1 2025", "2025", "Setembro 2025"
    val startDate: String = "",  // yyyy-MM-dd
    val endDate: String = "",    // yyyy-MM-dd
    val totalDays: Int = 0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val justifiedAbsences: Int = 0,
    val unjustifiedAbsences: Int = 0,
    val createdAt: Long = 0,
    val generatedAt: Long = 0
) {

    /**
     * Calcula percentual de frequência (0-100).
     */
    val attendancePercentage: Double
        get() = if (totalDays == 0) 0.0 else (presentDays * 100.0) / totalDays

    /**
     * Verifica se frequência está crítica (< 75%).
     */
    val isCritical: Boolean
        get() = attendancePercentage < 75.0

    /**
     * Verifica se frequência está baixa (< 85%).
     */
    val isLow: Boolean
        get() = attendancePercentage < 85.0

    /**
     * Verifica se frequência está adequada (>= 85%).
     */
    val isAdequate: Boolean
        get() = attendancePercentage >= 85.0

    /**
     * Formata percentual para exibição.
     */
    fun formatAttendancePercentage(): String = String.format("%.1f%%", attendancePercentage)

    /**
     * Retorna string com resumo do relatório.
     */
    override fun toString(): String = buildString {
        append("AttendanceReport(")
        append("studentName=$studentName, ")
        append("period=$period, ")
        append("presentDays=$presentDays/$totalDays, ")
        append("attendance=${formatAttendancePercentage()}")
        append(")")
    }
}

/**
 * Relatório detalhado com análise de padrões.
 */
@Stable
data class DetailedAttendanceReport(
    val baseReport: AttendanceReport,
    val recentAbsences: List<String> = emptyList(),  // Datas das últimas faltas
    val patterns: AttendancePatterns = AttendancePatterns(),
    val recommendation: String = ""
) {
    val studentName: String get() = baseReport.studentName
    val attendancePercentage: Double get() = baseReport.attendancePercentage
    val isCritical: Boolean get() = baseReport.isCritical
}

/**
 * Padrões detectados na frequência.
 */
@Stable
data class AttendancePatterns(
    val frequentAbsentDays: List<String> = emptyList(),  // ex: "Monday", "Friday"
    val consecutiveAbsences: Int = 0,  // Dias de falta consecutiva
    val trend: AttendanceTrend = AttendanceTrend.STABLE,  // Melhorando, Piorando, Estável
    val riskLevel: RiskLevel = RiskLevel.LOW
) {
    /**
     * Retorna descrição do padrão.
     */
    fun getDescription(): String = when {
        riskLevel == RiskLevel.CRITICAL -> "Frequência crítica: ação necessária"
        trend == AttendanceTrend.IMPROVING -> "Frequência melhorando"
        trend == AttendanceTrend.DECLINING -> "Frequência piorando"
        frequentAbsentDays.isNotEmpty() -> "Faltas recorrentes em ${frequentAbsentDays.joinToString(", ")}"
        consecutiveAbsences > 0 -> "$consecutiveAbsences dias de falta consecutiva"
        else -> "Frequência estável"
    }
}

/**
 * Tendência de frequência ao longo do tempo.
 */
enum class AttendanceTrend {
    IMPROVING,   // Frequência está melhorando
    DECLINING,   // Frequência está piorando
    STABLE       // Frequência estável
}

/**
 * Nível de risco baseado em frequência.
 */
enum class RiskLevel {
    LOW,         // >= 90%
    MEDIUM,      // 75-90%
    HIGH,        // 60-75%
    CRITICAL     // < 60%
}

/**
 * Relatório agregado para turma inteira.
 */
@Stable
data class ClassAttendanceReport(
    val classId: String = "",
    val className: String = "",
    val period: String = "",
    val totalStudents: Int = 0,
    val attendanceReports: List<AttendanceReport> = emptyList(),
    val generatedAt: Long = 0
) {
    /**
     * Frequência média da turma.
     */
    val averageAttendance: Double
        get() = if (attendanceReports.isEmpty()) {
            0.0
        } else {
            attendanceReports.map { it.attendancePercentage }.average()
        }

    /**
     * Quantos estudantes estão em risco (< 85%).
     */
    val studentsAtRisk: Int
        get() = attendanceReports.count { it.isLow }

    /**
     * Quantos estudantes em risco crítico (< 75%).
     */
    val studentsInCritical: Int
        get() = attendanceReports.count { it.isCritical }

    /**
     * Formata média para exibição.
     */
    fun formatAverageAttendance(): String = String.format("%.1f%%", averageAttendance)

    /**
     * Retorna string com resumo do relatório.
     */
    override fun toString(): String = buildString {
        append("ClassAttendanceReport(")
        append("className=$className, ")
        append("period=$period, ")
        append("students=$totalStudents, ")
        append("average=${formatAverageAttendance()}, ")
        append("atRisk=$studentsAtRisk")
        append(")")
    }
}
