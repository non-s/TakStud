package com.example.takstud.viewmodel

import app.cash.turbine.test
import com.example.takstud.data.repository.AttendanceRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Student
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var viewModel: AttendanceViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        attendanceRepository = mockk(relaxed = true)
        studentRepository = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)

        // Setup default behaviors
        every { attendanceRepository.getAttendanceRecords() } returns flowOf(emptyList())
        every { studentRepository.getStudents() } returns flowOf(emptyList())
        every { scheduleRepository.getSchedules() } returns flowOf(emptyList())

        viewModel = AttendanceViewModel(
            attendanceRepository,
            studentRepository,
            scheduleRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAttendanceRecords should emit records from repository`() = runTest {
        // Given
        val mockRecords = listOf(
            createMockAttendanceRecord("1", "student1", "3A", "01/12/2024", true),
            createMockAttendanceRecord("2", "student2", "3B", "01/12/2024", false)
        )
        every { attendanceRepository.getAttendanceRecords() } returns flowOf(mockRecords)

        // When
        viewModel = AttendanceViewModel(
            attendanceRepository,
            studentRepository,
            scheduleRepository
        )

        // Then
        viewModel.attendanceRecords.test {
            assertEquals(mockRecords, awaitItem())
        }
    }

    @Test
    fun `setAttendanceData should update selected class and date`() = runTest {
        // Given
        val className = "3A"
        val date = "01/12/2024"

        // When
        viewModel.setAttendanceData(className, date)

        // Then
        viewModel.selectedClassForAttendance.test {
            assertEquals(className, awaitItem())
        }
        viewModel.selectedDateForAttendance.test {
            assertEquals(date, awaitItem())
        }
    }

    @Test
    fun `clearAttendanceData should clear selected class and date`() = runTest {
        // Given
        viewModel.setAttendanceData("3A", "01/12/2024")

        // When
        viewModel.clearAttendanceData()

        // Then
        viewModel.selectedClassForAttendance.test {
            assertEquals("", awaitItem())
        }
        viewModel.selectedDateForAttendance.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `getAttendanceForStudent should filter records by student ID`() = runTest {
        // Given
        val student = Student(
            id = "student1",
            ra = "001",
            name = "João Silva",
            studentClass = "3A",
            parent = "João's Parent",
            phone = "(11) 99999-9999"
        )
        val mockRecords = listOf(
            createMockAttendanceRecord("1", "student1", "3A", "01/12/2024", true),
            createMockAttendanceRecord("2", "student2", "3B", "01/12/2024", false),
            createMockAttendanceRecord("3", "student1", "3A", "02/12/2024", true)
        )
        every { attendanceRepository.getAttendanceRecords() } returns flowOf(mockRecords)

        // When
        viewModel = AttendanceViewModel(
            attendanceRepository,
            studentRepository,
            scheduleRepository
        )
        val studentAttendance = viewModel.getAttendanceForStudent(student)

        // Then
        studentAttendance.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.studentId == "student1" })
        }
    }

    @Test
    fun `getAttendanceForClassByDate should filter records by class and date`() = runTest {
        // Given
        val className = "3A"
        val date = "01/12/2024"
        val mockRecords = listOf(
            createMockAttendanceRecord("1", "student1", "3A", "01/12/2024", true),
            createMockAttendanceRecord("2", "student2", "3B", "01/12/2024", false),
            createMockAttendanceRecord("3", "student3", "3A", "01/12/2024", true),
            createMockAttendanceRecord("4", "student1", "3A", "02/12/2024", false)
        )
        every { attendanceRepository.getAttendanceRecords() } returns flowOf(mockRecords)

        // When
        viewModel = AttendanceViewModel(
            attendanceRepository,
            studentRepository,
            scheduleRepository
        )
        val classAttendance = viewModel.getAttendanceForClassByDate(className, date)

        // Then
        classAttendance.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.studentClass == className && it.date == date })
        }
    }

    @Test
    fun `saveAttendanceRecord should call repository saveAttendanceRecord`() {
        // Given
        val mockRecord = createMockAttendanceRecord(
            "1",
            "student1",
            "3A",
            "01/12/2024",
            true
        )

        // When
        viewModel.saveAttendanceRecord(mockRecord)

        // Then
        verify { attendanceRepository.saveAttendanceRecord(mockRecord) }
    }

    // Helper function
    private fun createMockAttendanceRecord(
        id: String,
        studentId: String,
        studentClass: String,
        date: String,
        isPresent: Boolean
    ): AttendanceRecord {
        return AttendanceRecord(
            id = id,
            studentId = studentId,
            studentClass = studentClass,
            date = date,
            isPresent = isPresent,
            createdAt = System.currentTimeMillis()
        )
    }
}
