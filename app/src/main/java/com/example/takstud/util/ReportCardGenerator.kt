package com.example.takstud.util

import com.example.takstud.model.grade.*
import com.example.takstud.model.Student
import com.example.takstud.model.task.TaskExtended
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerador de Boletins Digitais.
 * 
 * Responsável por agregar notas, calcular médias e gerar o objeto ReportCard.
 */
@Singleton
class ReportCardGenerator @Inject constructor() {

    /**
     * Gera um boletim para um estudante específico.
     */
    fun generateReportCard(
        student: Student,
        grades: List<Grade>,
        tasks: List<TaskExtended>,
        academicYear: Int,
        semester: Int,
        quarter: Int? = null
    ): ReportCard {
        
        // Agrupar notas por disciplina (assumindo que Task tem subjectId ou similar, 
        // ou inferindo pelo professor/turma. Por enquanto, vamos simular agrupamento por 'materia' se existir,
        // ou usar um placeholder se a estrutura de Task não tiver subject explícito ainda)
        
        // Nota: O modelo Task atual tem 'studentClass', mas não 'subject'. 
        // Vamos assumir que em uma expansão futura Task terá 'subject'.
        // Para este MVP, vamos agrupar por 'studentClass' como se fosse a disciplina, 
        // ou criar uma lógica de subjects baseada em metadados.
        
        // TODO: Refinar agrupamento de disciplinas quando Task tiver campo subjectId
        val gradesBySubject = grades.groupBy { grade ->
            val task = tasks.find { it.id == grade.taskId }
            task?.title?.split(" - ")?.firstOrNull() ?: "Geral" // Tentativa heurística de extrair matéria do título
        }

        val subjectGrades = gradesBySubject.map { (subjectName, subjectGradesList) ->
            calculateSubjectGrade(subjectName, subjectGradesList, tasks)
        }

        val overallAverage = if (subjectGrades.isNotEmpty()) {
            subjectGrades.map { it.finalScore }.average()
        } else {
            0.0
        }

        // GPA simples (escala 4.0)
        val gpa = (overallAverage / 10.0) * 4.0

        return ReportCard(
            id = UUID.randomUUID().toString(),
            studentId = student.id,
            studentName = student.name,
            studentRegistrationNumber = student.ra,
            academicYear = academicYear,
            semester = semester,
            quarter = quarter,
            classId = student.classId,
            className = student.studentClass,
            subjectGrades = subjectGrades,
            overallAverage = overallAverage,
            gpa = gpa,
            isPassed = overallAverage >= 6.0, // Média 6.0 para aprovação
            hasRecovery = subjectGrades.any { it.needsRecovery },
            isPublished = false
        )
    }

    private fun calculateSubjectGrade(
        subjectName: String,
        grades: List<Grade>,
        tasks: List<TaskExtended>
    ): SubjectGrade {
        val assessmentSummaries = grades.map { grade ->
            val task = tasks.find { it.id == grade.taskId }
            AssessmentGradeSummary(
                assessmentId = grade.assessmentId,
                assessmentTitle = task?.title ?: "Atividade",
                assessmentType = AssessmentType.ASSIGNMENT, // Default
                score = grade.score ?: 0.0,
                maxScore = 10.0, // Default
                weight = 1.0, // Default
                date = grade.createdAt
            )
        }

        val average = if (grades.isNotEmpty()) {
            grades.mapNotNull { it.score }.average()
        } else {
            0.0
        }

        // Simulação de média ponderada (por enquanto igual à média simples)
        val weightedAverage = average

        val isPassed = weightedAverage >= 6.0
        
        return SubjectGrade(
            subjectId = UUID.randomUUID().toString(), // Placeholder
            subjectName = subjectName,
            assessmentGrades = assessmentSummaries,
            average = average,
            weightedAverage = weightedAverage,
            isPassed = isPassed,
            needsRecovery = !isPassed,
            finalScore = weightedAverage,
            attendanceRate = 100.0, // Placeholder, integrar com AttendanceRepository depois
            totalClasses = 0,
            attendedClasses = 0,
            absences = 0
        )
    }
}
