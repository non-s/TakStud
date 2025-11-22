package com.example.takstud.util

import com.example.takstud.model.grade.Grade
import com.example.takstud.model.AttendanceRecord
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Motor de Análise Preditiva.
 * 
 * Utiliza dados históricos para prever desempenho futuro e identificar riscos.
 */
@Singleton
class PredictionEngine @Inject constructor() {

    data class PredictionResult(
        val predictedFinalGrade: Double,
        val riskLevel: RiskLevel,
        val trend: Trend,
        val confidence: Double, // 0.0 a 1.0
        val recommendations: List<String>
    )

    enum class RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    enum class Trend {
        IMPROVING, STABLE, DECLINING, VOLATILE
    }

    /**
     * Analisa o desempenho do estudante e gera previsões.
     */
    fun predictPerformance(
        grades: List<Grade>,
        attendance: List<AttendanceRecord>
    ): PredictionResult {
        if (grades.isEmpty()) {
            return PredictionResult(0.0, RiskLevel.LOW, Trend.STABLE, 0.0, emptyList())
        }

        // 1. Análise de Tendência de Notas
        // Ordenar por data
        val sortedGrades = grades.sortedBy { it.createdAt }.mapNotNull { it.score }
        
        val trend = calculateTrend(sortedGrades)
        val average = sortedGrades.average()
        
        // 2. Previsão Linear Simples
        // Se a tendência continuar, qual será a nota final?
        val predictedGrade = predictNextGrade(sortedGrades)

        // 3. Análise de Frequência
        val attendanceRate = if (attendance.isNotEmpty()) {
            attendance.count { it.isPresent }.toDouble() / attendance.size
        } else {
            1.0 // Assumir 100% se sem dados
        }

        // 4. Cálculo de Risco
        val riskLevel = calculateRisk(average, trend, attendanceRate)

        // 5. Recomendações
        val recommendations = generateRecommendations(riskLevel, trend, attendanceRate)

        return PredictionResult(
            predictedFinalGrade = predictedGrade,
            riskLevel = riskLevel,
            trend = trend,
            confidence = calculateConfidence(grades.size),
            recommendations = recommendations
        )
    }

    private fun calculateTrend(scores: List<Double>): Trend {
        if (scores.size < 3) return Trend.STABLE

        val recent = scores.takeLast(3).average()
        val previous = scores.dropLast(3).takeLast(3).let { if (it.isEmpty()) recent else it.average() }

        return when {
            recent > previous + 1.0 -> Trend.IMPROVING
            recent < previous - 1.0 -> Trend.DECLINING
            abs(recent - previous) <= 1.0 -> Trend.STABLE
            else -> Trend.VOLATILE
        }
    }

    private fun predictNextGrade(scores: List<Double>): Double {
        if (scores.isEmpty()) return 0.0
        if (scores.size == 1) return scores.first()

        // Regressão linear simples (simplificada)
        val n = scores.size
        val xSum = (0 until n).sum()
        val ySum = scores.sum()
        
        // Se tivermos poucos pontos, a média ponderada recente é mais segura
        if (n < 5) {
            val weights = (1..n).map { it.toDouble() }
            val weightedSum = scores.zip(weights).sumOf { it.first * it.second }
            return weightedSum / weights.sum()
        }
        
        return scores.average() // Fallback seguro
    }

    private fun calculateRisk(average: Double, trend: Trend, attendanceRate: Double): RiskLevel {
        var score = 0

        // Fator Nota
        if (average < 5.0) score += 3
        else if (average < 7.0) score += 1

        // Fator Tendência
        if (trend == Trend.DECLINING) score += 2

        // Fator Frequência
        if (attendanceRate < 0.75) score += 3
        else if (attendanceRate < 0.85) score += 1

        return when {
            score >= 5 -> RiskLevel.CRITICAL
            score >= 3 -> RiskLevel.HIGH
            score >= 1 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    private fun generateRecommendations(risk: RiskLevel, trend: Trend, attendanceRate: Double): List<String> {
        val list = mutableListOf<String>()

        if (attendanceRate < 0.75) {
            list.add("Alerta de Frequência: Risco de reprovação por faltas.")
        }

        if (trend == Trend.DECLINING) {
            list.add("Desempenho em queda: Agendar reforço escolar.")
        }

        if (risk == RiskLevel.CRITICAL) {
            list.add("Ação Imediata: Convocar reunião com responsáveis.")
        }

        if (list.isEmpty() && risk == RiskLevel.LOW) {
            list.add("Desempenho sólido. Manter o ritmo.")
        }

        return list
    }

    private fun calculateConfidence(sampleSize: Int): Double {
        // Mais dados = maior confiança, até um teto
        return (sampleSize / 10.0).coerceAtMost(0.95)
    }
}
