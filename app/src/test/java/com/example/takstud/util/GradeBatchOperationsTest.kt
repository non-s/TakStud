package com.example.takstud.util

import com.example.takstud.model.Grade
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes unitários para GradeBatchOperations.
 *
 * Cobertura:
 * - BatchResult correctness
 * - Taxa de sucesso
 * - Falhas e retries
 */
class GradeBatchOperationsTest {

    @Test
    fun testBatchResultSuccessRate() {
        // ARRANGE
        val result = GradeBatchOperations.BatchResult(
            total = 100,
            succeeded = 85,
            failed = 15
        )

        // ACT & ASSERT
        assertEquals(85.0, result.successRate, 0.1)
        assertFalse(result.isSuccess())  // Tem falhas
    }

    @Test
    fun testBatchResultAllSuccess() {
        // ARRANGE
        val result = GradeBatchOperations.BatchResult(
            total = 50,
            succeeded = 50,
            failed = 0
        )

        // ACT & ASSERT
        assertEquals(100.0, result.successRate, 0.1)
        assertTrue(result.isSuccess())
    }

    @Test
    fun testBatchResultAllFailed() {
        // ARRANGE
        val result = GradeBatchOperations.BatchResult(
            total = 30,
            succeeded = 0,
            failed = 30
        )

        // ACT & ASSERT
        assertEquals(0.0, result.successRate, 0.1)
        assertFalse(result.isSuccess())
    }

    @Test
    fun testBatchResultEmpty() {
        // ARRANGE
        val result = GradeBatchOperations.BatchResult(
            total = 0,
            succeeded = 0,
            failed = 0
        )

        // ACT & ASSERT
        assertEquals(0.0, result.successRate, 0.1)  // 0 total = 0% success
        assertTrue(result.isSuccess())  // Mas sem falhas = true
    }

    @Test
    fun testBatchResultToString() {
        // ARRANGE
        val result = GradeBatchOperations.BatchResult(
            total = 100,
            succeeded = 90,
            failed = 10
        )

        // ACT
        val str = result.toString()

        // ASSERT
        assertTrue(str.contains("100"))
        assertTrue(str.contains("90"))
        assertTrue(str.contains("10"))
        assertTrue(str.contains("90.0"))  // success rate
    }
}
