package com.example.takstud.util

import com.example.takstud.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * AttendanceReportGenerator - Gera relatórios de frequência.
 *
 * FUNCIONALIDADES:
 * - Agregação de dados de frequência
 * - Cálculo de percentuais
 * - Detecção de padrões
 * - Geração de recomendações
 * - Relatórios individuais e por turma
 *
 * EXEMPLO DE USO:
 * val generator = AttendanceReportGenerator()
 * val report = generator.generateStudentReport(
 *     attendanceRecords,
 *     studentId = "student1",
 *     period = "Q1 2025",
 *     startDate = "2025-01-01",
 *     endDate = "2025-03-31"
 * )
 * val percentage = report.attendancePercentage  // 92.0
 */
object AttendanceReportGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.US)

    /**
     * Gera relatório de frequência para um estudante em um período.
     *
     * @param records Lista de registros de frequência
     * @param studentId ID do estudante
     * @param period Período descritivo (ex: "Q1 2025")
     * @param startDate Data inicial (yyyy-MM-dd)
     * @param endDate Data final (yyyy-MM-dd)
     * @return AttendanceReport com dados agregados
     */
    fun generateStudentReport(
        records: List<AttendanceRecord>,
        studentId: String,
        period: String,
        startDate: String = "",
        endDate: String = ""
    ): AttendanceReport {
        val studentRecords = records.filter { it.studentId == studentId }

        // Filtrar por período se datas fornecidas
        val filteredRecords = if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            studentRecords.filter { it.date in startDate..endDate }
        } else {
            studentRecords
        }

        if (filteredRecords.isEmpty()) {
            return AttendanceReport(
                studentId = studentId,
                period = period,
                startDate = startDate,
                endDate = endDate,
                totalDays = 0,
                presentDays = 0,
                absentDays = 0
            )
        }

        // Usar dados do primeiro registro para nome e RA
        val firstRecord = filteredRecords.first()
        val totalDays = filteredRecords.size
        val presentDays = filteredRecords.count { it.isPresent }
        val absentDays = totalDays - presentDays

        return AttendanceReport(
            id = "report_${studentId}_${System.currentTimeMillis()}",
            studentId = studentId,
            studentName = firstRecord.studentName,
            studentRa = firstRecord.studentRa,
            classId = firstRecord.classId,
            className = firstRecord.studentClass,
            period = period,
            startDate = startDate,
            endDate = endDate,
            totalDays = totalDays,
            presentDays = presentDays,
            absentDays = absentDays,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Gera relatório detalhado com análise de padrões.
     *
     * @param records Lista de registros de frequência
     * @param studentId ID do estudante
     * @param period Período descritivo
     * @param startDate Data inicial
     * @param endDate Data final
     * @return DetailedAttendanceReport com padrões e recomendações
     */
    fun generateDetailedReport(
        records: List<AttendanceRecord>,
        studentId: String,
        period: String,
        startDate: String = "",
        endDate: String = ""
    ): DetailedAttendanceReport {
        val baseReport = generateStudentReport(records, studentId, period, startDate, endDate)
        val studentRecords = records.filter { it.studentId == studentId }

        // Detectar padrões
        val patterns = detectAttendancePatterns(studentRecords)

        // Filtrar últimas 5 faltas para análise recente
        val recentAbsences = studentRecords
            .filter { !it.isPresent }
            .sortedByDescending { it.date }
            .take(5)
            .map { it.date }

        // Gerar recomendação
        val recommendation = generateRecommendation(baseReport, patterns)

        return DetailedAttendanceReport(
            baseReport = baseReport,
            recentAbsences = recentAbsences,
            patterns = patterns,
            recommendation = recommendation
        )
    }

    /**
     * Gera relatório de frequência para uma turma.
     *
     * @param records Lista de registros de frequência
     * @param classId ID da turma
     * @param period Período descritivo
     * @param startDate Data inicial
     * @param endDate Data final
     * @return ClassAttendanceReport com agregação para todos estudantes
     */
    fun generateClassReport(
        records: List<AttendanceRecord>,
        classId: String,
        period: String,
        startDate: String = "",
        endDate: String = ""
    ): ClassAttendanceReport {
        val classRecords = records.filter { it.classId == classId }

        // Obter lista única de estudantes
        val uniqueStudents = classRecords.map { it.studentId }.distinct()

        // Gerar relatório para cada estudante
        val studentReports = uniqueStudents.map { studentId ->
            generateStudentReport(classRecords, studentId, period, startDate, endDate)
        }

        val className = classRecords.firstOrNull()?.studentClass ?: ""

        return ClassAttendanceReport(
            classId = classId,
            className = className,
            period = period,
            totalStudents = uniqueStudents.size,
            attendanceReports = studentReports,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Detecta padrões na frequência de um estudante.
     *
     * @param records Registros ordenados cronologicamente
     * @return AttendancePatterns com análise
     */
    private fun detectAttendancePatterns(records: List<AttendanceRecord>): AttendancePatterns {
        if (records.isEmpty()) {
            return AttendancePatterns()
        }

        // Detectar dias da semana com mais faltas
        val absencesByDayOfWeek = records
            .filter { !it.isPresent }
            .groupBy { record ->
                try {
                    val date = dateFormat.parse(record.date) ?: return@groupBy "Unknown"
                    dayOfWeekFormat.format(date)
                } catch (e: Exception) {
                    "Unknown"
                }
            }

        val frequentDays = absencesByDayOfWeek
            .entries
            .sortedByDescending { it.value.size }
            .take(2)
            .map { it.key }

        // Detectar faltas consecutivas
        val consecutiveAbsences = calculateMaxConsecutiveAbsences(records)

        // Detectar tendência (últimas 10 aulas vs anteriores)
        val trend = detectTrend(records)

        // Calcular risco
        val attendancePercentage = if (records.size > 0) {
            (records.count { it.isPresent } * 100.0) / records.size
        } else {
            0.0
        }

        val riskLevel = when {
            attendancePercentage < 60.0 -> RiskLevel.CRITICAL
            attendancePercentage < 75.0 -> RiskLevel.HIGH
            attendancePercentage < 90.0 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return AttendancePatterns(
            frequentAbsentDays = frequentDays,
            consecutiveAbsences = consecutiveAbsences,
            trend = trend,
            riskLevel = riskLevel
        )
    }

    /**
     * Calcula número máximo de faltas consecutivas.
     */
    private fun calculateMaxConsecutiveAbsences(records: List<AttendanceRecord>): Int {
        val sorted = records.sortedBy { it.date }
        var maxConsecutive = 0
        var currentConsecutive = 0

        for (record in sorted) {
            if (!record.isPresent) {
                currentConsecutive++
                maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
            } else {
                currentConsecutive = 0
            }
        }

        return maxConsecutive
    }

    /**
     * Detecta tendência comparando períodos.
     */
    private fun detectTrend(records: List<AttendanceRecord>): AttendanceTrend {
        if (records.size < 4) return AttendanceTrend.STABLE

        val sorted = records.sortedBy { it.date }
        val midpoint = sorted.size / 2

        val firstHalf = sorted.take(midpoint)
        val secondHalf = sorted.drop(midpoint)

        val firstAttendance = if (firstHalf.isNotEmpty()) {
            (firstHalf.count { it.isPresent } * 100.0) / firstHalf.size
        } else {
            0.0
        }

        val secondAttendance = if (secondHalf.isNotEmpty()) {
            (secondHalf.count { it.isPresent } * 100.0) / secondHalf.size
        } else {
            0.0
        }

        val difference = secondAttendance - firstAttendance

        return when {
            difference > 5 -> AttendanceTrend.IMPROVING
            difference < -5 -> AttendanceTrend.DECLINING
            else -> AttendanceTrend.STABLE
        }
    }

    /**
     * Gera recomendação baseada no relatório e padrões.
     */
    private fun generateRecommendation(
        report: AttendanceReport,
        patterns: AttendancePatterns
    ): String = when {
        report.isCritical && patterns.riskLevel == RiskLevel.CRITICAL -> {
            "⚠️ CRÍTICO: Frequência perigosamente baixa. Contato imediato com responsável recomendado."
        }
        report.isCritical -> {
            "⚠️ AVISO: Frequência abaixo de 75%. Monitoramento intenso necessário."
        }
        report.isLow && patterns.trend == AttendanceTrend.DECLINING -> {
            "📉 Atenção: Frequência em queda. Investigar causas e intervir."
        }
        report.isLow && patterns.frequentAbsentDays.isNotEmpty() -> {
            "📅 Padrão detectado: Faltas recorrentes em ${patterns.frequentAbsentDays.joinToString(", ")}."
        }
        patterns.trend == AttendanceTrend.IMPROVING -> {
            "📈 Positivo: Frequência melhorando. Continuar acompanhando."
        }
        patterns.consecutiveAbsences > 3 -> {
            "⏸️ Alerta: ${patterns.consecutiveAbsences} dias de falta consecutiva. Verificar justificativa."
        }
        else -> {
            "✅ Frequência adequada. Continue acompanhando."
        }
    }

    /**
     * Exporta relatório em formato CSV.
     */
    fun exportToCSV(report: ClassAttendanceReport): String = buildString {
        appendLine("Relatório de Frequência - ${report.className}")
        appendLine("Período: ${report.period}")
        appendLine("Data de Geração: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date(report.generatedAt))}")
        appendLine()
        appendLine("Estudante,RA,Dias Presentes,Total de Dias,Frequência (%)")

        for (studentReport in report.attendanceReports) {
            val attendance = studentReport.formatAttendancePercentage()
            appendLine("${studentReport.studentName},${studentReport.studentRa},${studentReport.presentDays},${studentReport.totalDays},$attendance")
        }

        appendLine()
        appendLine("Resumo da Turma")
        appendLine("Média de Frequência: ${report.formatAverageAttendance()}")
        appendLine("Estudantes em Risco (< 85%): ${report.studentsAtRisk}")
        appendLine("Estudantes em Risco Crítico (< 75%): ${report.studentsInCritical}")
    }
}
