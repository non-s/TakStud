package com.example.takstud.viewmodel

import app.cash.turbine.test
import com.example.takstud.data.repository.GradeRepository
import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.data.repository.StudentRepository
import com.example.takstud.data.repository.TaskRepository
import com.example.takstud.model.Grade
import com.example.takstud.model.Student
import com.example.takstud.model.task.TaskExtended
import com.example.takstud.ui.common.UiState
import io.mockk.coEvery
import io.mockk.coVerify
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
class TaskViewModelTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var gradeRepository: GradeRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var viewModel: TaskViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        taskRepository = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)
        gradeRepository = mockk(relaxed = true)
        studentRepository = mockk(relaxed = true)

        // Setup default repository behaviors
        every { taskRepository.getTasks() } returns flowOf(emptyList())
        every { gradeRepository.getGrades() } returns flowOf(emptyList())
        every { scheduleRepository.getSchedules() } returns flowOf(emptyList())
        every { studentRepository.getStudents() } returns flowOf(emptyList())

        viewModel = TaskViewModel(
            taskRepository,
            scheduleRepository,
            gradeRepository,
            studentRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getTasks should emit tasks from repository`() = runTest {
        // Given
        val mockTasks = listOf(
            createMockTask("1", "Tarefa 1"),
            createMockTask("2", "Tarefa 2")
        )
        every { taskRepository.getTasks() } returns flowOf(mockTasks)

        // When
        // ViewModel is already created in setup

        // Then
        viewModel.tasks.test {
            assertEquals(mockTasks, awaitItem())
        }
    }

    @Test
    fun `getGrades should emit grades from repository`() = runTest {
        // Given
        val mockGrades = listOf(
            createMockGrade("1", "student1", "task1", 85.0),
            createMockGrade("2", "student2", "task2", 90.0)
        )
        every { gradeRepository.getGrades() } returns flowOf(mockGrades)

        // When
        // ViewModel is already created in setup

        // Then
        viewModel.grades.test {
            assertEquals(mockGrades, awaitItem())
        }
    }

    @Test
    fun `getTasksForStudent should filter tasks by student class`() = runTest {
        // Given
        val student = Student(
            id = "student1",
            ra = "001",
            name = "João Silva",
            studentClass = "3A",
            parent = "João's Parent",
            phone = "(11) 99999-9999"
        )
        val mockTasks = listOf(
            createMockTask("1", "Tarefa 1", "3A"),
            createMockTask("2", "Tarefa 2", "3B"),
            createMockTask("3", "Tarefa 3", "3A")
        )
        every { taskRepository.getTasks() } returns flowOf(mockTasks)

        // When
        val studentTasks = viewModel.getTasksForStudent(student)

        // Then
        studentTasks.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.className == "3A" })
        }
    }

    @Test
    fun `getGradesForStudent should filter grades by student ID`() = runTest {
        // Given
        val student = Student(
            id = "student1",
            ra = "001",
            name = "João Silva",
            studentClass = "3A",
            parent = "João's Parent",
            phone = "(11) 99999-9999"
        )
        val mockGrades = listOf(
            createMockGrade("1", "student1", "task1", 85.0),
            createMockGrade("2", "student2", "task2", 90.0),
            createMockGrade("3", "student1", "task3", 95.0)
        )
        every { gradeRepository.getGrades() } returns flowOf(mockGrades)

        // When
        val studentGrades = viewModel.getGradesForStudent(student)

        // Then
        studentGrades.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.studentId == "student1" })
        }
    }

    @Test
    fun `getGradesForTask should filter grades by task ID`() = runTest {
        // Given
        val taskId = "task1"
        val mockGrades = listOf(
            createMockGrade("1", "student1", "task1", 85.0),
            createMockGrade("2", "student2", "task2", 90.0),
            createMockGrade("3", "student3", "task1", 95.0)
        )
        every { gradeRepository.getGrades() } returns flowOf(mockGrades)

        // When
        val taskGrades = viewModel.getGradesForTask(taskId)

        // Then
        taskGrades.test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.all { it.taskId == "task1" })
        }
    }

    @Test
    fun `loadTask should emit Loading then Success when task found`() = runTest {
        // Given
        val taskId = "task1"
        val mockTask = createMockTask(taskId, "Tarefa 1")
        coEvery { taskRepository.getTaskById(taskId) } returns flowOf(mockTask)

        // When
        viewModel.loadTask(taskId)

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentTask.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(mockTask, (state as UiState.Success).data)
        }
    }

    @Test
    fun `loadTask should emit Error when task not found`() = runTest {
        // Given
        val taskId = "nonexistent"
        coEvery { taskRepository.getTaskById(taskId) } returns flowOf(null)

        // When
        viewModel.loadTask(taskId)

        // Then
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.currentTask.test {
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals("Tarefa não encontrada", (state as UiState.Error).message)
        }
    }

    @Test
    fun `saveTask should call repository saveTask and onBack`() = runTest {
        // Given
        val mockTask = createMockTask("1", "Nova Tarefa")
        val onBack: () -> Unit = mockk(relaxed = true)
        coEvery { taskRepository.saveTask(mockTask) } returns Unit

        // When
        viewModel.saveTask(mockTask, onBack)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { taskRepository.saveTask(mockTask) }
        verify { onBack() }
    }

    @Test
    fun `deleteTask should call repository deleteTask`() = runTest {
        // Given
        val mockTask = createMockTask("1", "Tarefa a deletar")
        coEvery { taskRepository.deleteTask(mockTask) } returns Unit

        // When
        viewModel.deleteTask(mockTask)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { taskRepository.deleteTask(mockTask) }
    }

    // Helper functions
    private fun createMockTask(
        id: String,
        title: String,
        className: String = "3A"
    ): TaskExtended {
        return TaskExtended(
            id = id,
            title = title,
            description = "Descrição da $title",
            dueDate = System.currentTimeMillis() + 86400000, // +1 day
            className = className,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun createMockGrade(
        id: String,
        studentId: String,
        taskId: String,
        score: Double
    ): Grade {
        return Grade(
            id = id,
            studentId = studentId,
            taskId = taskId,
            score = score.toString(),
            value = score.toString(),
            createdAt = System.currentTimeMillis()
        )
    }
}
