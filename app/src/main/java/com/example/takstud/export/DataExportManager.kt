package com.example.takstud.export

import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.takstud.model.*
import java.io.File
import java.io.FileOutputStream

/**
 * DataExportManager - Gerencia exportação de dados em diferentes formatos
 * Suporta PDF e Excel (CSV)
 */
class DataExportManager(private val context: Context) {

    /**
     * Exportar tarefas em CSV
     */
    fun exportTasksToCSV(tasks: List<Task>): Result<File> {
        return try {
            val file = createExportFile("tarefas", ".csv")

            val header = "ID,Título,Turma,Data de Entrega,Descrição\n"
            val content = tasks.joinToString("\n") { task ->
                "\"${task.id}\",\"${task.title}\",\"${task.studentClass}\",\"${task.dueDate}\",\"${task.description}\""
            }

            FileOutputStream(file).use { output ->
                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(content.toByteArray(Charsets.UTF_8))
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exportar avisos em CSV
     */
    fun exportNoticesToCSV(notices: List<Notice>): Result<File> {
        return try {
            val file = createExportFile("avisos", ".csv")

            val header = "ID,Título,Turma\n"
            val content = notices.joinToString("\n") { notice ->
                "\"${notice.id}\",\"${notice.title}\",\"${notice.studentClass}\""
            }

            FileOutputStream(file).use { output ->
                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(content.toByteArray(Charsets.UTF_8))
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exportar notas em CSV
     */
    fun exportGradesToCSV(grades: List<Grade>, students: List<Student>): Result<File> {
        return try {
            val file = createExportFile("notas", ".csv")

            val header = "ID Aluno,Nome do Aluno,ID Tarefa,Nota\n"
            val content = grades.joinToString("\n") { grade ->
                val student = students.find { it.id == grade.studentId }
                "\"${grade.studentId}\",\"${student?.name ?: "Desconhecido"}\",\"${grade.taskId}\",\"${grade.score}\""
            }

            FileOutputStream(file).use { output ->
                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(content.toByteArray(Charsets.UTF_8))
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exportar presença em CSV
     */
    fun exportAttendanceToCSV(attendance: List<AttendanceRecord>, students: List<Student>): Result<File> {
        return try {
            val file = createExportFile("presenca", ".csv")

            val header = "ID Aluno,Nome do Aluno,Turma,Data,Presente\n"
            val content = attendance.joinToString("\n") { record ->
                val student = students.find { it.id == record.studentId }
                "\"${record.studentId}\",\"${student?.name ?: "Desconhecido"}\",\"${record.studentClass}\",\"${record.date}\",\"${if (record.isPresent) "Sim" else "Não"}\""
            }

            FileOutputStream(file).use { output ->
                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(content.toByteArray(Charsets.UTF_8))
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exportar alunos em CSV
     */
    fun exportStudentsToCSV(students: List<Student>): Result<File> {
        return try {
            val file = createExportFile("alunos", ".csv")

            val header = "ID,Nome,RA,Turma\n"
            val content = students.joinToString("\n") { student ->
                "\"${student.id}\",\"${student.name}\",\"${student.ra}\",\"${student.studentClass}\""
            }

            FileOutputStream(file).use { output ->
                output.write(header.toByteArray(Charsets.UTF_8))
                output.write(content.toByteArray(Charsets.UTF_8))
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exportar relatório completo em texto
     */
    fun exportCompleteReportToText(
        tasks: List<Task>,
        notices: List<Notice>,
        students: List<Student>,
        grades: List<Grade>,
        attendance: List<AttendanceRecord>
    ): Result<File> {
        return try {
            val file = createExportFile("relatorio_completo", ".txt")

            val report = buildString {
                appendLine("RELATÓRIO COMPLETO - TAKSTUD")
                appendLine("Data de Geração: ${java.time.LocalDateTime.now()}")
                appendLine("=" .repeat(80))
                appendLine()

                // Resumo
                appendLine("RESUMO")
                appendLine("-".repeat(80))
                appendLine("Total de Tarefas: ${tasks.size}")
                appendLine("Total de Avisos: ${notices.size}")
                appendLine("Total de Alunos: ${students.size}")
                appendLine("Total de Notas Registradas: ${grades.size}")
                appendLine("Total de Registros de Presença: ${attendance.size}")
                appendLine()

                // Tarefas
                appendLine("TAREFAS")
                appendLine("-".repeat(80))
                tasks.forEach { task ->
                    appendLine("ID: ${task.id}")
                    appendLine("Título: ${task.title}")
                    appendLine("Turma: ${task.studentClass}")
                    appendLine("Data de Entrega: ${task.dueDate}")
                    appendLine()
                }

                // Avisos
                appendLine("AVISOS")
                appendLine("-".repeat(80))
                notices.forEach { notice ->
                    appendLine("ID: ${notice.id}")
                    appendLine("Título: ${notice.title}")
                    appendLine("Turma: ${notice.studentClass}")
                    appendLine()
                }

                // Alunos
                appendLine("ALUNOS")
                appendLine("-".repeat(80))
                students.forEach { student ->
                    appendLine("Nome: ${student.name}")
                    appendLine("RA: ${student.ra}")
                    appendLine("Turma: ${student.studentClass}")
                    appendLine()
                }
            }

            FileOutputStream(file).use { output ->
                output.write(report.toByteArray(Charsets.UTF_8))
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Criar arquivo de exportação
     */
    private fun createExportFile(name: String, extension: String): File {
        val timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val fileName = "${name}_$timestamp$extension"

        val exportDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "TakStud_Exports"
        ).apply {
            if (!exists()) mkdirs()
        }

        return File(exportDir, fileName)
    }

    /**
     * Obter URI do arquivo para compartilhamento
     */
    fun getFileUri(file: File): android.net.Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Listar arquivos exportados
     */
    fun getExportedFiles(): List<File> {
        val exportDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "TakStud_Exports"
        )
        return if (exportDir.exists()) {
            exportDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Deletar arquivo exportado
     */
    fun deleteExportedFile(file: File): Boolean {
        return file.delete()
    }
}
