package com.example.takstud.util

import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Testes para AdvancedValidator - validadores avançados com regras complexas.
 *
 * Cobre:
 * - Validação de nomes (caracteres, comprimento, espaços)
 * - Validação de RA (número de dígitos)
 * - Validação de notas (intervalo 0-100)
 * - Validação de datas (formato, futuro, intervalo)
 * - Validação de horários (formato HH:mm)
 * - Validação de telefone (Brasil)
 * - Validação de email (RFC)
 * - Validação de descrição/texto longo
 * - Validação de título
 * - ValidationResult sealed class
 */
class AdvancedValidatorTest {

    // ==================== VALIDATE NAME (3 tests) ====================

    @Test
    fun `validateName - accepts valid full name`() {
        // Arrange & Act
        val result = AdvancedValidator.validateName("João da Silva")

        // Assert
        assertTrue(result.isValid())
        assertEquals("João da Silva", result.getOrNull())
    }

    @Test
    fun `validateName - rejects name too short`() {
        // Arrange & Act
        val result = AdvancedValidator.validateName("Jo")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("3 caracteres") == true)
    }

    @Test
    fun `validateName - rejects name with numbers`() {
        // Arrange & Act
        val result = AdvancedValidator.validateName("João Silva 123")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("caracteres inválidos") == true)
    }

    // ==================== VALIDATE RA (3 tests) ====================

    @Test
    fun `validateRA - accepts valid RA`() {
        // Arrange & Act
        val result = AdvancedValidator.validateRA("12345678")

        // Assert
        assertTrue(result.isValid())
        assertEquals("12345678", result.getOrNull())
    }

    @Test
    fun `validateRA - rejects RA with non-digits`() {
        // Arrange & Act
        val result = AdvancedValidator.validateRA("1234ABC")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("apenas números") == true)
    }

    @Test
    fun `validateRA - rejects RA too short`() {
        // Arrange & Act
        val result = AdvancedValidator.validateRA("1")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("2 caracteres") == true)
    }

    // ==================== VALIDATE GRADE (4 tests) ====================

    @Test
    fun `validateGrade - accepts valid grade 85`() {
        // Arrange & Act
        val result = AdvancedValidator.validateGrade("85")

        // Assert
        assertTrue(result.isValid())
        assertEquals(85.0, result.getOrNull())
    }

    @Test
    fun `validateGrade - rejects grade above 100`() {
        // Arrange & Act
        val result = AdvancedValidator.validateGrade("150")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("100") == true)
    }

    @Test
    fun `validateGrade - rejects negative grade`() {
        // Arrange & Act
        val result = AdvancedValidator.validateGrade("-5")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("0") == true)
    }

    @Test
    fun `validateGrade - rejects non-numeric grade`() {
        // Arrange & Act
        val result = AdvancedValidator.validateGrade("ABC")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("número válido") == true)
    }

    // ==================== VALIDATE DATE (4 tests) ====================

    @Test
    fun `validateDate - accepts valid past date`() {
        // Arrange
        val yesterday = LocalDate.now().minusDays(1).format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )

        // Act
        val result = AdvancedValidator.validateDate(yesterday, allowFutureDate = false)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun `validateDate - rejects future date when not allowed`() {
        // Arrange
        val tomorrow = LocalDate.now().plusDays(1).format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )

        // Act
        val result = AdvancedValidator.validateDate(tomorrow, allowFutureDate = false)

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("futuro") == true)
    }

    @Test
    fun `validateDate - accepts future date when allowed`() {
        // Arrange
        val tomorrow = LocalDate.now().plusDays(1).format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )

        // Act
        val result = AdvancedValidator.validateDate(tomorrow, allowFutureDate = true)

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun `validateDate - rejects invalid date format`() {
        // Arrange & Act
        val result = AdvancedValidator.validateDate("2025-11-14")  // Wrong format

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("dd/MM/yyyy") == true)
    }

    // ==================== VALIDATE TIME RANGE (2 tests) ====================

    @Test
    fun `validateTimeRange - accepts valid time range`() {
        // Arrange & Act
        val result = AdvancedValidator.validateTimeRange("09:00", "10:30")

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun `validateTimeRange - rejects invalid time order`() {
        // Arrange & Act
        val result = AdvancedValidator.validateTimeRange("10:30", "09:00")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("antes") == true)
    }

    // ==================== VALIDATE PHONE (2 tests) ====================

    @Test
    fun `validatePhone - accepts valid phone format`() {
        // Arrange & Act
        val result = AdvancedValidator.validatePhone("11 99999-9999")

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun `validatePhone - rejects invalid phone format`() {
        // Arrange & Act
        val result = AdvancedValidator.validatePhone("123")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("Formato") == true)
    }

    // ==================== VALIDATE EMAIL (2 tests) ====================

    @Test
    fun `validateEmail - accepts valid email`() {
        // Arrange & Act
        val result = AdvancedValidator.validateEmail("user@example.com")

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun `validateEmail - rejects invalid email format`() {
        // Arrange & Act
        val result = AdvancedValidator.validateEmail("invalid.email")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("Email inválido") == true)
    }

    // ==================== VALIDATE DESCRIPTION (2 tests) ====================

    @Test
    fun `validateDescription - accepts valid description`() {
        // Arrange & Act
        val result = AdvancedValidator.validateDescription("This is a valid description")

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun `validateDescription - rejects description too short`() {
        // Arrange & Act
        val result = AdvancedValidator.validateDescription("Hi")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("5 caracteres") == true)
    }

    // ==================== VALIDATE TITLE (2 tests) ====================

    @Test
    fun `validateTitle - accepts valid title`() {
        // Arrange & Act
        val result = AdvancedValidator.validateTitle("Math Homework")

        // Assert
        assertTrue(result.isValid())
    }

    @Test
    fun `validateTitle - rejects title too short`() {
        // Arrange & Act
        val result = AdvancedValidator.validateTitle("Hi")

        // Assert
        assertTrue(result.isInvalid())
        assertTrue(result.getErrorMessage()?.contains("3 caracteres") == true)
    }

    // ==================== VALIDATION RESULT (3 tests) ====================

    @Test
    fun `ValidationResult.Valid - holds data correctly`() {
        // Arrange
        val validResult = AdvancedValidator.validateName("João Silva")

        // Act & Assert
        assertTrue(validResult.isValid())
        assertFalse(validResult.isInvalid())
        assertEquals("João Silva", validResult.getOrNull())
    }

    @Test
    fun `ValidationResult.Invalid - holds error message correctly`() {
        // Arrange
        val invalidResult = AdvancedValidator.validateName("Jo")

        // Act & Assert
        assertFalse(invalidResult.isValid())
        assertTrue(invalidResult.isInvalid())
        assertTrue(invalidResult.getErrorMessage()?.isNotEmpty() == true)
    }

    @Test
    fun `ValidationResult - extension functions work correctly`() {
        // Arrange
        val valid = AdvancedValidator.validateGrade("85")
        val invalid = AdvancedValidator.validateGrade("150")

        // Act & Assert
        assertTrue(valid.isValid())
        assertFalse(valid.isInvalid())
        assertTrue(invalid.isInvalid())
        assertFalse(invalid.isValid())
    }
}
