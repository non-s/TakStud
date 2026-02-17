package com.example.takstud.viewmodel

import app.cash.turbine.test
import com.example.takstud.data.repository.AttendanceRepository
import com.example.takstud.data.repository.GradeRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.data.repository.StudentTimelineRepository
import com.example.takstud.data.repository.TaskRepository
import com.example.takstud.model.Student
import com.example.takstud.util.AnalyticsEngine
import com.example.takstud.util.PredictionEngine
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelTest {

    private lateinit var studentRepository: StudentRepository
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var gradeRepository: GradeRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var studentTimelineRepository: StudentTimelineRepository
    private lateinit var predictionEngine: PredictionEngine
    private lateinit var analyticsEngine: AnalyticsEngine
    private lateinit var viewModel: StudentViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        studentRepository = mockk(relaxed = true)
        attendanceRepository = mockk(relaxed = true)
        gradeRepository = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)
        taskRepository = mockk(relaxed = true)
        studentTimelineRepository = mockk(relaxed = true)
        predictionEngine = mockk(relaxed = true)
        analyticsEngine = mockk(relaxed = true)

        // Setup default behaviors
        every { studentRepository.getStudents() } returns flowOf(emptyList())
        every { attendanceRepository.getAttendanceRecords() } returns flowOf(emptyList())
        every { gradeRepository.getGrades() } returns flowOf(emptyList())
        every { scheduleRepository.getSchedules() } returns flowOf(emptyList())
        every { taskRepository.getTasks() } returns flowOf(emptyList())

        viewModel = StudentViewModel(
            studentRepository,
            attendanceRepository,
            gradeRepository,
            scheduleRepository,
            taskRepository,
            studentTimelineRepository,
            predictionEngine,
            analyticsEngine
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getStudents should emit students from repository`() = runTest {
        // Given
        val mockStudents = listOf(
            createMockStudent("1", "001", "João Silva", "3A"),
            createMockStudent("2", "002", "Maria Santos", "3B")
        )
        every { studentRepository.getStudents() } returns flowOf(mockStudents)

        // When
        viewModel = StudentViewModel(
            studentRepository,
            attendanceRepository,
            gradeRepository,
            scheduleRepository,
            taskRepository,
            studentTimelineRepository,
            predictionEngine,
            analyticsEngine
        )

        // Then
        viewModel.students.test {
            assertEquals(mockStudents, awaitItem())
        }
    }

    @Test
    fun `getStudentsForClass should filter students by class`() = runTest {
        // Given
        val className = "3A"
        val mockStudents = listOf(
            createMockStudent("1", "001", "João Silva", "3A"),
            createMockStudent("2", "002", "Maria Santos", "3B"),
            createMockStudent("3", "003", "Pedro Oliveira", "3A")
        )
        every { studentRepository.getStudents() } returns flowOf(mockStudents)
        every { studentRepository.getStudentsByClass(className) } returns flowOf(
            mockStudents.filter { it.studentClass == className }
        )

        // When
        viewModel = StudentViewModel(
            studentRepository,
            attendanceRepository,
            gradeRepository,
            scheduleRepository,
            taskRepository,
            studentTimelineRepository,
            predictionEngine,
            analyticsEngine
        )
        val classStudents = viewModel.getStudentsForClass(className)

        // Then
        classStudents.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("João Silva", result[0].name)
            assertEquals("Pedro Oliveira", result[1].name)
        }
    }

    @Test
    fun `saveStudent should call repository saveStudent`() {
        // Given
        val mockStudent = createMockStudent("1", "001", "João Silva", "3A")
        val onComplete: () -> Unit = mockk(relaxed = true)

        // When
        viewModel.saveStudent(mockStudent, onComplete)

        // Then
        verify { studentRepository.saveStudent(mockStudent, onComplete) }
    }

    @Test
    fun `deleteStudent should call repository deleteStudent`() {
        // Given
        val mockStudent = createMockStudent("1", "001", "João Silva", "3A")

        // When
        viewModel.deleteStudent(mockStudent)

        // Then
        verify { studentRepository.deleteStudent(mockStudent) }
    }

    // Helper function
    private fun createMockStudent(
        id: String,
        ra: String,
        name: String,
        studentClass: String
    ): Student {
        return Student(
            id = id,
            ra = ra,
            name = name,
            studentClass = studentClass,
            parent = "$name's Parent",
            phone = "(11) 99999-9999"
        )
    }
}
