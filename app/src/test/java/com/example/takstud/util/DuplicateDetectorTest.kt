package com.example.takstud.util

import com.example.takstud.model.Grade
import com.example.takstud.model.Task
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para DuplicateDetector.
 *
 * Cobertura:
 * - Detecção de duplicatas por ID
 * - Detecção de duplicatas por conteúdo
 * - Merge automático
 * - Validação de integridade
 */
class DuplicateDetectorTest {

    private lateinit var detector: DuplicateDetector

    @Before
    fun setUp() {
        detector = DuplicateDetector()
    }

    // ============== TESTES DE TASKS ==============

    @Test
    fun testDetectDuplicateTasksById() {
        // ARRANGE
        val task1 = Task(id = "task1", title = "Math", dueDate = "2025-11-15")
        val task1_duplicate = Task(id = "task1", title = "Math (updated)", dueDate = "2025-11-20")
        val task2 = Task(id = "task2", title = "English", dueDate = "2025-11-16")

        val tasks = listOf(task1, task1_duplicate, task2)

        // ACT
        val result = detector.detectDuplicateTasks(tasks)

        // ASSERT
        assertEquals(2, result.unique.size)  // Deve ficar com 2 únicos
        assertEquals(1, result.duplicates.size)  // 1 duplicata
        assertTrue(result.hasDuplicates())
    }

    @Test
    fun testNoDuplicateTasksFound() {
        // ARRANGE
        val tasks = listOf(
            Task(id = "task1", title = "Math"),
            Task(id = "task2", title = "English"),
            Task(id = "task3", title = "Science")
        )

        // ACT
        val result = detector.detectDuplicateTasks(tasks)

        // ASSERT
        assertEquals(3, result.unique.size)
        assertEquals(0, result.duplicates.size)
        assertFalse(result.hasDuplicates())
    }

    @Test
    fun testMergeTasksKeepsNewerVersion() {
        // ARRANGE
        val oldTask = Task(
            id = "task1",
            title = "Math",
            modifiedAt = 1000L
        )
        val newTask = Task(
            id = "task1",
            title = "Mathematics",
            modifiedAt = 2000L
        )

        // ACT
        val merged = detector.mergeTaskDuplicates(oldTask, newTask)

        // ASSERT
        assertEquals("Mathematics", merged.title)
        assertEquals(2000L, merged.modifiedAt)
    }

    // ============== TESTES DE GRADES ==============

    @Test
    fun testDetectDuplicateGradesByStudentAndTask() {
        // ARRANGE
        val grade1 = Grade(
            id = "grade1",
            studentId = "student1",
            taskId = "task1",
            value = "8.5"
        )
        val grade1_duplicate = Grade(
            id = "grade1_dup",
            studentId = "student1",
            taskId = "task1",
            value = "9.0"
        )

        val grades = listOf(grade1, grade1_duplicate)

        // ACT
        val result = detector.detectDuplicateGrades(grades)

        // ASSERT
        assertEquals(1, result.unique.size)
        assertEquals(1, result.duplicates.size)
        assertTrue(result.hasDuplicates())
    }

    @Test
    fun testMergeGradesKeepsHigherValue() {
        // ARRANGE
        val oldGrade = Grade(
            id = "grade1",
            studentId = "student1",
            value = "8.0",
            modifiedAt = 1000L
        )
        val newGrade = Grade(
            id = "grade1",
            studentId = "student1",
            value = "9.5",
            modifiedAt = 1000L  // Mesmo timestamp
        )

        // ACT
        val merged = detector.mergeGradeDuplicates(oldGrade, newGrade)

        // ASSERT
        assertEquals("9.5", merged.value)  // Deve ficar com valor maior
    }

    @Test
    fun testMergeGradesKeepsNewerWhenDifferentTimestamp() {
        // ARRANGE
        val oldGrade = Grade(
            id = "grade1",
            studentId = "student1",
            value = "7.0",
            modifiedAt = 1000L
        )
        val newGrade = Grade(
            id = "grade1",
            studentId = "student1",
            value = "6.0",
            modifiedAt = 2000L  // Mais recente
        )

        // ACT
        val merged = detector.mergeGradeDuplicates(oldGrade, newGrade)

        // ASSERT
        assertEquals("6.0", merged.value)  // Mantém mais recente mesmo se valor menor
    }

    // ============== TESTES DE VALIDAÇÃO ==============

    @Test
    fun testValidateDeduplicationIsValid() {
        // ARRANGE
        val original = listOf(
            Task(id = "task1"),
            Task(id = "task1"),  // Duplicata
            Task(id = "task2")
        )
        val result = DuplicateDetector.DuplicateResult(
            unique = listOf(Task(id = "task1"), Task(id = "task2")),
            duplicates = listOf(Task(id = "task1")),
            removed = 1
        )

        // ACT
        val validation = detector.validateDeduplicationResult(original, result)

        // ASSERT
        assertTrue(validation.isValid)
        assertEquals(3, validation.beforeCount)
        assertEquals(3, validation.afterCount)
        assertEquals(1, validation.removedDuplicates)
        assertEquals(0, validation.dataLoss)
    }

    @Test
    fun testValidateDeduplicationWithDataLoss() {
        // ARRANGE
        val original = listOf(
            Task(id = "task1"),
            Task(id = "task2")
        )
        val result = DuplicateDetector.DuplicateResult(
            unique = listOf(Task(id = "task1")),
            duplicates = listOf(),
            removed = 0
        )

        // ACT
        val validation = detector.validateDeduplicationResult(original, result)

        // ASSERT
        assertFalse(validation.isValid)
        assertEquals(1, validation.dataLoss)
    }

    // ============== TESTES DE EDGE CASES ==============

    @Test
    fun testEmptyListOfTasks() {
        // ARRANGE
        val tasks = emptyList<Task>()

        // ACT
        val result = detector.detectDuplicateTasks(tasks)

        // ASSERT
        assertEquals(0, result.unique.size)
        assertEquals(0, result.duplicates.size)
    }

    @Test
    fun testSingleTaskNoDuplicates() {
        // ARRANGE
        val tasks = listOf(Task(id = "task1", title = "Math"))

        // ACT
        val result = detector.detectDuplicateTasks(tasks)

        // ASSERT
        assertEquals(1, result.unique.size)
        assertFalse(result.hasDuplicates())
    }

    @Test
    fun testMultipleDuplicatesOfSameItem() {
        // ARRANGE
        val task = Task(id = "task1", title = "Math")
        val tasks = listOf(task, task, task, task)  // 4 cópias

        // ACT
        val result = detector.detectDuplicateTasks(tasks)

        // ASSERT
        assertEquals(1, result.unique.size)
        assertEquals(3, result.duplicates.size)  // 3 duplicatas
    }
}
