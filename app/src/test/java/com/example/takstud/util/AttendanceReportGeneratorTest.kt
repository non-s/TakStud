package com.example.takstud.util

import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.RiskLevel
import com.example.takstud.model.AttendanceTrend
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertContains

/**
 * Testes para AttendanceReportGenerator.
 *
 * Cobre:
 * - Geração de relatórios individuais (generateStudentReport)
 * - Relatórios detalhados com padrões (generateDetailedReport)
 * - Relatórios de turma (generateClassReport)
 * - Detecção de padrões de frequência
 * - Cálculo de percentuais e tendências
 * - Exportação em CSV
 * - Edge cases (empty lists, single records, invalid dates)
 */
class AttendanceReportGeneratorTest {

    private lateinit var generator: AttendanceReportGenerator

    @Before
    fun setUp() {
        generator = AttendanceReportGenerator
    }

    // ==================== Helper Functions ====================

    private fun createAttendanceRecord(
        studentId: String = "student_001",
        studentName: String = "John Doe",
        studentRa: String = "RA001",
        classId: String = "class_6A",
        studentClass: String = "6A",
        date: String = "2025-11-14",
        isPresent: Boolean = true
    ): AttendanceRecord {
        return AttendanceRecord(
            id = "$studentId-$date",
            studentId = studentId,
            studentName = studentName,
            studentRa = studentRa,
            classId = classId,
            studentClass = studentClass,
            date = date,
            isPresent = isPresent,
            modifiedAt = System.currentTimeMillis()
        )
    }

    // ==================== GENERATE STUDENT REPORT (8 tests) ====================

    @Test
    fun `generateStudentReport - perfect attendance 100%`() {
        // Arrange - All present
        val records = (1..10).map { i ->
            createAttendanceRecord(
                studentId = "student_001",
                date = "2025-11-${10 + i}",
                isPresent = true
            )
        }

        // Act
        val report = generator.generateStudentReport(
            records = records,
            studentId = "student_001",
            period = "November 2025",
            startDate = "2025-11-10",
            endDate = "2025-11-20"
        )

        // Assert
        assertEquals(10, report.totalDays)
        assertEquals(10, report.presentDays)
        assertEquals(0, report.absentDays)
        assertEquals(100.0, report.attendancePercentage)
    }

    @Test
    fun `generateStudentReport - 50% attendance`() {
        // Arrange - Mix of present and absent
        val records = listOf(
            createAttendanceRecord(date = "2025-11-01", isPresent = true),
            createAttendanceRecord(date = "2025-11-02", isPresent = false),
            createAttendanceRecord(date = "2025-11-03", isPresent = true),
            createAttendanceRecord(date = "2025-11-04", isPresent = false)
        )

        // Act
        val report = generator.generateStudentReport(
            records = records,
            studentId = "student_001",
            period = "Q1 2025"
        )

        // Assert
        assertEquals(4, report.totalDays)
        assertEquals(2, report.presentDays)
        assertEquals(2, report.absentDays)
        assertEquals(50.0, report.attendancePercentage)
    }

    @Test
    fun `generateStudentReport - zero attendance`() {
        // Arrange - All absent
        val records = (1..5).map { i ->
            createAttendanceRecord(
                date = "2025-11-${10 + i}",
                isPresent = false
            )
        }

        // Act
        val report = generator.generateStudentReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(5, report.totalDays)
        assertEquals(0, report.presentDays)
        assertEquals(5, report.absentDays)
        assertEquals(0.0, report.attendancePercentage)
        assertTrue(report.isCritical)
    }

    @Test
    fun `generateStudentReport - empty records returns zero data`() {
        // Arrange
        val records = emptyList<AttendanceRecord>()

        // Act
        val report = generator.generateStudentReport(
            records = records,
            studentId = "student_001",
            period = "Q1 2025"
        )

        // Assert
        assertEquals(0, report.totalDays)
        assertEquals(0, report.presentDays)
        assertEquals(0, report.absentDays)
    }

    @Test
    fun `generateStudentReport - filters by date range`() {
        // Arrange - Records across multiple months
        val records = listOf(
            createAttendanceRecord(studentId = "student_001", date = "2025-10-15", isPresent = true),
            createAttendanceRecord(studentId = "student_001", date = "2025-10-20", isPresent = true),
            createAttendanceRecord(studentId = "student_001", date = "2025-11-01", isPresent = false),
            createAttendanceRecord(studentId = "student_001", date = "2025-11-10", isPresent = true),
            createAttendanceRecord(studentId = "student_001", date = "2025-12-01", isPresent = true)
        )

        // Act - Filter for November only
        val report = generator.generateStudentReport(
            records = records,
            studentId = "student_001",
            period = "November 2025",
            startDate = "2025-11-01",
            endDate = "2025-11-30"
        )

        // Assert - Only November records counted
        assertEquals(2, report.totalDays)
        assertEquals(1, report.presentDays)
        assertEquals(1, report.absentDays)
    }

    @Test
    fun `generateStudentReport - includes student metadata from first record`() {
        // Arrange
        val records = listOf(
            createAttendanceRecord(
                studentId = "student_001",
                studentName = "Alice Smith",
                studentRa = "RA123",
                studentClass = "7B"
            )
        )

        // Act
        val report = generator.generateStudentReport(
            records = records,
            studentId = "student_001",
            period = "Q1 2025"
        )

        // Assert
        assertEquals("Alice Smith", report.studentName)
        assertEquals("RA123", report.studentRa)
        assertEquals("7B", report.className)
    }

    @Test
    fun `generateStudentReport - high attendance is not critical`() {
        // Arrange - 95% attendance (high)
        val records = (1..20).map { i ->
            createAttendanceRecord(
                date = "2025-11-${10 + (i % 20)}",
                isPresent = (i % 20) != 0  // 19/20 present
            )
        }

        // Act
        val report = generator.generateStudentReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(20, report.totalDays)
        assertFalse(report.isCritical)
    }

    // ==================== GENERATE DETAILED REPORT (8 tests) ====================

    @Test
    fun `generateDetailedReport - includes base report data`() {
        // Arrange
        val records = listOf(
            createAttendanceRecord(date = "2025-11-01", isPresent = true),
            createAttendanceRecord(date = "2025-11-02", isPresent = false)
        )

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals("student_001", detailedReport.baseReport.studentId)
        assertEquals(2, detailedReport.baseReport.totalDays)
        assertEquals(1, detailedReport.baseReport.presentDays)
    }

    @Test
    fun `generateDetailedReport - detects patterns in attendance`() {
        // Arrange - Absences on Mondays (simulated by date pattern)
        val records = listOf(
            createAttendanceRecord(date = "2025-11-10", isPresent = false),  // Monday
            createAttendanceRecord(date = "2025-11-17", isPresent = false),  // Monday
            createAttendanceRecord(date = "2025-11-24", isPresent = true),
            createAttendanceRecord(date = "2025-11-11", isPresent = true)
        )

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertNotNull(detailedReport.patterns)
        assertTrue(detailedReport.patterns.frequentAbsentDays.isNotEmpty())
    }

    @Test
    fun `generateDetailedReport - identifies critical risk level`() {
        // Arrange - 50% attendance (critical)
        val records = (1..10).map { i ->
            createAttendanceRecord(
                date = "2025-11-${10 + i}",
                isPresent = (i % 2 == 0)  // 50% present
            )
        }

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(RiskLevel.CRITICAL, detailedReport.patterns.riskLevel)
    }

    @Test
    fun `generateDetailedReport - identifies high risk level`() {
        // Arrange - 70% attendance (high)
        val records = (1..10).map { i ->
            createAttendanceRecord(
                date = "2025-11-${10 + i}",
                isPresent = (i <= 7)  // 70% present
            )
        }

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(RiskLevel.HIGH, detailedReport.patterns.riskLevel)
    }

    @Test
    fun `generateDetailedReport - identifies medium risk level`() {
        // Arrange - 85% attendance (medium)
        val records = (1..20).map { i ->
            createAttendanceRecord(
                date = "2025-11-${10 + (i % 20)}",
                isPresent = (i <= 17)  // 85% present
            )
        }

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(RiskLevel.MEDIUM, detailedReport.patterns.riskLevel)
    }

    @Test
    fun `generateDetailedReport - identifies low risk level`() {
        // Arrange - 95% attendance (low risk)
        val records = (1..20).map { i ->
            createAttendanceRecord(
                date = "2025-11-${10 + (i % 20)}",
                isPresent = (i != 19)  // 95% present
            )
        }

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(RiskLevel.LOW, detailedReport.patterns.riskLevel)
    }

    @Test
    fun `generateDetailedReport - detects improving trend`() {
        // Arrange - Improving from 50% to 100%
        val records = listOf(
            // First half (4 records, 50% present)
            createAttendanceRecord(date = "2025-11-01", isPresent = true),
            createAttendanceRecord(date = "2025-11-02", isPresent = false),
            createAttendanceRecord(date = "2025-11-03", isPresent = true),
            createAttendanceRecord(date = "2025-11-04", isPresent = false),
            // Second half (4 records, 100% present)
            createAttendanceRecord(date = "2025-11-05", isPresent = true),
            createAttendanceRecord(date = "2025-11-06", isPresent = true),
            createAttendanceRecord(date = "2025-11-07", isPresent = true),
            createAttendanceRecord(date = "2025-11-08", isPresent = true)
        )

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(AttendanceTrend.IMPROVING, detailedReport.patterns.trend)
    }

    @Test
    fun `generateDetailedReport - detects declining trend`() {
        // Arrange - Declining from 100% to 50%
        val records = listOf(
            // First half (4 records, 100% present)
            createAttendanceRecord(date = "2025-11-01", isPresent = true),
            createAttendanceRecord(date = "2025-11-02", isPresent = true),
            createAttendanceRecord(date = "2025-11-03", isPresent = true),
            createAttendanceRecord(date = "2025-11-04", isPresent = true),
            // Second half (4 records, 50% present)
            createAttendanceRecord(date = "2025-11-05", isPresent = false),
            createAttendanceRecord(date = "2025-11-06", isPresent = true),
            createAttendanceRecord(date = "2025-11-07", isPresent = false),
            createAttendanceRecord(date = "2025-11-08", isPresent = true)
        )

        // Act
        val detailedReport = generator.generateDetailedReport(
            records = records,
            studentId = "student_001",
            period = "November 2025"
        )

        // Assert
        assertEquals(AttendanceTrend.DECLINING, detailedReport.patterns.trend)
    }

    // ==================== GENERATE CLASS REPORT (5 tests) ====================

    @Test
    fun `generateClassReport - aggregates multiple students`() {
        // Arrange - 3 students in class 6A
        val records = listOf(
            createAttendanceRecord(studentId = "student_001", date = "2025-11-01", isPresent = true),
            createAttendanceRecord(studentId = "student_002", date = "2025-11-01", isPresent = false),
            createAttendanceRecord(studentId = "student_003", date = "2025-11-01", isPresent = true),
            createAttendanceRecord(studentId = "student_001", date = "2025-11-02", isPresent = true),
            createAttendanceRecord(studentId = "student_002", date = "2025-11-02", isPresent = true),
            createAttendanceRecord(studentId = "student_003", date = "2025-11-02", isPresent = false)
        )

        // Act
        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Assert
        assertEquals(3, classReport.totalStudents)
        assertEquals(3, classReport.attendanceReports.size)
        assertEquals("class_6A", classReport.classId)
    }

    @Test
    fun `generateClassReport - calculates average attendance`() {
        // Arrange
        val records = listOf(
            // Student 1: 100% (2/2)
            createAttendanceRecord(studentId = "student_001", date = "2025-11-01", isPresent = true),
            createAttendanceRecord(studentId = "student_001", date = "2025-11-02", isPresent = true),
            // Student 2: 50% (1/2)
            createAttendanceRecord(studentId = "student_002", date = "2025-11-01", isPresent = true),
            createAttendanceRecord(studentId = "student_002", date = "2025-11-02", isPresent = false),
            // Student 3: 0% (0/2)
            createAttendanceRecord(studentId = "student_003", date = "2025-11-01", isPresent = false),
            createAttendanceRecord(studentId = "student_003", date = "2025-11-02", isPresent = false)
        )

        // Act
        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Assert
        assertEquals(3, classReport.totalStudents)
        // Average = (100 + 50 + 0) / 3 = 50%
        assertEquals(50.0, classReport.formatAverageAttendance().toDoubleOrNull())
    }

    @Test
    fun `generateClassReport - empty class returns zero students`() {
        // Arrange
        val records = emptyList<AttendanceRecord>()

        // Act
        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Assert
        assertEquals(0, classReport.totalStudents)
        assertEquals(0, classReport.attendanceReports.size)
    }

    @Test
    fun `generateClassReport - identifies students at risk`() {
        // Arrange - Student with 80% (at risk)
        val records = (1..10).map { i ->
            createAttendanceRecord(
                studentId = "student_001",
                date = "2025-11-${10 + i}",
                isPresent = (i <= 8)  // 80% present
            )
        }

        // Act
        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Assert
        assertEquals(1, classReport.totalStudents)
        assertTrue(classReport.studentsAtRisk > 0)
    }

    @Test
    fun `generateClassReport - identifies students in critical risk`() {
        // Arrange - Student with 50% (critical)
        val records = (1..10).map { i ->
            createAttendanceRecord(
                studentId = "student_001",
                date = "2025-11-${10 + i}",
                isPresent = (i % 2 == 0)  // 50% present
            )
        }

        // Act
        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Assert
        assertEquals(1, classReport.totalStudents)
        assertTrue(classReport.studentsInCritical > 0)
    }

    // ==================== EXPORT TO CSV (4 tests) ====================

    @Test
    fun `exportToCSV - includes header information`() {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = "student_001", date = "2025-11-01", isPresent = true)
        )

        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Act
        val csv = generator.exportToCSV(classReport)

        // Assert
        assertTrue(csv.contains("Relatório de Frequência"))
        assertTrue(csv.contains("class_6A"))
        assertTrue(csv.contains("November 2025"))
    }

    @Test
    fun `exportToCSV - includes student data rows`() {
        // Arrange
        val records = listOf(
            createAttendanceRecord(
                studentId = "student_001",
                studentName = "Alice Smith",
                studentRa = "RA123",
                date = "2025-11-01",
                isPresent = true
            ),
            createAttendanceRecord(
                studentId = "student_001",
                studentName = "Alice Smith",
                studentRa = "RA123",
                date = "2025-11-02",
                isPresent = false
            )
        )

        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Act
        val csv = generator.exportToCSV(classReport)

        // Assert
        assertTrue(csv.contains("Alice Smith"))
        assertTrue(csv.contains("RA123"))
        assertTrue(csv.contains("1"))  // Present days
        assertTrue(csv.contains("2"))  // Total days
    }

    @Test
    fun `exportToCSV - includes class summary`() {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = "student_001", date = "2025-11-01", isPresent = true)
        )

        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Act
        val csv = generator.exportToCSV(classReport)

        // Assert
        assertTrue(csv.contains("Resumo da Turma"))
        assertTrue(csv.contains("Média de Frequência"))
        assertTrue(csv.contains("Estudantes em Risco"))
    }

    @Test
    fun `exportToCSV - formats percentage correctly`() {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = "student_001", date = "2025-11-01", isPresent = true),
            createAttendanceRecord(studentId = "student_001", date = "2025-11-02", isPresent = false)
        )

        val classReport = generator.generateClassReport(
            records = records,
            classId = "class_6A",
            period = "November 2025"
        )

        // Act
        val csv = generator.exportToCSV(classReport)

        // Assert
        assertTrue(csv.contains("50"))  // 50% attendance
    }
}
