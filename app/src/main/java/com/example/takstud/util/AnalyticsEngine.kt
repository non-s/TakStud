package com.example.takstud.util

import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.example.takstud.model.Student
import com.example.takstud.model.task.TaskExtended
import com.example.takstud.model.task.TaskStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor de Análise de Dados (Analytics).
 * 
 * Responsável por agregar dados brutos e gerar métricas para dashboards.
 */
@Singleton
class AnalyticsEngine @Inject constructor() {

    data class ClassAnalytics(
        val classId: String,
        val totalStudents: Int,
        val averageAttendance: Double,
        val averageGrade: Double,
        val taskCompletionRate: Double,
        val atRiskCount: Int
    )

    data class SchoolAnalytics(
        val totalStudents: Int,
        val totalClasses: Int,
        val averageAttendance: Double,
        val averageGrade: Double,
        val activeTasks: Int,
        val criticalAlerts: Int
    )

    /**
     * Calcula métricas para uma turma específica.
     */
    fun calculateClassMetrics(
        classId: String,
        students: List<Student>,
        attendance: List<AttendanceRecord>,
        grades: List<Grade>,
        tasks: List<TaskExtended>
    ): ClassAnalytics {
        val classStudents = students.filter { it.studentClass == classId || it.classId == classId }
        val studentIds = classStudents.map { it.id }.toSet()

        // Frequência
        val classAttendance = attendance.filter { it.classId == classId }
        val avgAttendance = if (classAttendance.isNotEmpty()) {
            (classAttendance.count { it.isPresent }.toDouble() / classAttendance.size) * 100.0
        } else {
            0.0
        }

        // Notas
        val classGrades = grades.filter { it.studentId in studentIds }
        val avgGrade = if (classGrades.isNotEmpty()) {
            classGrades.mapNotNull { it.score }.average()
        } else {
            0.0
        }

        // Tarefas
        val classTasks = tasks.filter { it.className == classId || it.classId == classId }
        val completionRate = if (classTasks.isNotEmpty()) {
            classTasks.map { it.completionRate }.average()
        } else {
            0.0
        }

        // Risco (Simplificado)
        val atRiskCount = classStudents.count { student ->
            val studentGrades = classGrades.filter { it.studentId == student.id }
            val studentAvg = if (studentGrades.isNotEmpty()) studentGrades.mapNotNull { it.score }.average() else 0.0
            studentAvg < 6.0 // Critério simples de risco
        }

        return ClassAnalytics(
            classId = classId,
            totalStudents = classStudents.size,
            averageAttendance = avgAttendance,
            averageGrade = avgGrade,
            taskCompletionRate = completionRate,
            atRiskCount = atRiskCount
        )
    }

    /**
     * Calcula métricas globais da escola.
     */
    fun calculateSchoolMetrics(
        students: List<Student>,
        attendance: List<AttendanceRecord>,
        grades: List<Grade>,
        tasks: List<TaskExtended>
    ): SchoolAnalytics {
        val totalStudents = students.size
        val classes = students.map { it.studentClass }.distinct().count()

        val avgAttendance = if (attendance.isNotEmpty()) {
            (attendance.count { it.isPresent }.toDouble() / attendance.size) * 100.0
        } else {
            0.0
        }

        val avgGrade = if (grades.isNotEmpty()) {
            grades.mapNotNull { it.score }.average()
        } else {
            0.0
        }

        val activeTasks = tasks.count { it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.PENDING }

        // Alertas críticos (ex: alunos com média < 5.0 ou frequência < 75%)
        // Cálculo simplificado para exemplo
        val criticalAlerts = students.count { student ->
            val studentGrades = grades.filter { it.studentId == student.id }
            val studentAvg = if (studentGrades.isNotEmpty()) studentGrades.mapNotNull { it.score }.average() else 0.0
            studentAvg < 5.0
        }

        return SchoolAnalytics(
            totalStudents = totalStudents,
            totalClasses = classes,
            averageAttendance = avgAttendance,
            averageGrade = avgGrade,
            activeTasks = activeTasks,
            criticalAlerts = criticalAlerts
        )
    }
    
    /**
     * Gera dados para gráfico de distribuição de notas.
     */
    fun getGradeDistribution(grades: List<Grade>): Map<String, Int> {
        val distribution = mutableMapOf(
            "A (9-10)" to 0,
            "B (7-8.9)" to 0,
            "C (5-6.9)" to 0,
            "D (< 5)" to 0
        )

        grades.forEach { grade ->
            val score = grade.score ?: return@forEach
            when {
                score >= 9.0 -> distribution["A (9-10)"] = distribution["A (9-10)"]!! + 1
                score >= 7.0 -> distribution["B (7-8.9)"] = distribution["B (7-8.9)"]!! + 1
                score >= 5.0 -> distribution["C (5-6.9)"] = distribution["C (5-6.9)"]!! + 1
                else -> distribution["D (< 5)"] = distribution["D (< 5)"]!! + 1
            }
        }
        return distribution
    }
}
