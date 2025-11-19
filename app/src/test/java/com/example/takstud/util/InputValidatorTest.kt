package com.example.takstud.util

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for InputValidator utility functions.
 * Tests all validation functions with both valid and invalid inputs.
 */
class InputValidatorTest {

    // ============= isNotEmpty Tests =============
    @Test
    fun isNotEmpty_withValidInput_returnsTrue() {
        assertTrue(InputValidator.isNotEmpty("Hello"))
    }

    @Test
    fun isNotEmpty_withEmptyString_returnsFalse() {
        assertFalse(InputValidator.isNotEmpty(""))
    }

    @Test
    fun isNotEmpty_withWhitespace_returnsFalse() {
        assertFalse(InputValidator.isNotEmpty("   "))
    }

    // ============= hasMinLength Tests =============
    @Test
    fun hasMinLength_withValidLength_returnsTrue() {
        assertTrue(InputValidator.hasMinLength("Hello", 5))
        assertTrue(InputValidator.hasMinLength("Hello", 3))
    }

    @Test
    fun hasMinLength_withInvalidLength_returnsFalse() {
        assertFalse(InputValidator.hasMinLength("Hi", 5))
        assertFalse(InputValidator.hasMinLength("", 1))
    }

    @Test
    fun hasMinLength_withExactLength_returnsTrue() {
        assertTrue(InputValidator.hasMinLength("Test", 4))
    }

    // ============= hasMaxLength Tests =============
    @Test
    fun hasMaxLength_withValidLength_returnsTrue() {
        assertTrue(InputValidator.hasMaxLength("Hello", 10))
        assertTrue(InputValidator.hasMaxLength("Hello", 5))
    }

    @Test
    fun hasMaxLength_withInvalidLength_returnsFalse() {
        assertFalse(InputValidator.hasMaxLength("HelloWorld", 5))
    }

    @Test
    fun hasMaxLength_withExactLength_returnsTrue() {
        assertTrue(InputValidator.hasMaxLength("Test", 4))
    }

    // ============= isValidRA Tests =============
    @Test
    fun isValidRA_withValidRA_returnsTrue() {
        assertTrue(InputValidator.isValidRA("01"))
        assertTrue(InputValidator.isValidRA("001"))
        assertTrue(InputValidator.isValidRA("ALU-001"))
        assertTrue(InputValidator.isValidRA("ALU_001"))
        assertTrue(InputValidator.isValidRA("12345"))
    }

    @Test
    fun isValidRA_withTooShort_returnsFalse() {
        assertFalse(InputValidator.isValidRA("1"))
    }

    @Test
    fun isValidRA_withTooLong_returnsFalse() {
        assertFalse(InputValidator.isValidRA("123456789012345678901")) // 21 chars
    }

    @Test
    fun isValidRA_withInvalidCharacters_returnsFalse() {
        assertFalse(InputValidator.isValidRA("ALU@001"))
        assertFalse(InputValidator.isValidRA("ALU 001"))
        assertFalse(InputValidator.isValidRA("ALU.001"))
    }

    @Test
    fun isValidRA_withEmpty_returnsFalse() {
        assertFalse(InputValidator.isValidRA(""))
    }

    // ============= isValidEmail Tests =============
    @Test
    fun isValidEmail_withValidEmail_returnsTrue() {
        assertTrue(InputValidator.isValidEmail("test@example.com"))
        assertTrue(InputValidator.isValidEmail("user.name@example.co.uk"))
    }

    @Test
    fun isValidEmail_withInvalidEmail_returnsFalse() {
        assertFalse(InputValidator.isValidEmail("invalid.email"))
        assertFalse(InputValidator.isValidEmail("@example.com"))
        assertFalse(InputValidator.isValidEmail("test@"))
        assertFalse(InputValidator.isValidEmail(""))
    }

    // ============= isValidTitle Tests =============
    @Test
    fun isValidTitle_withValidTitle_returnsTrue() {
        assertTrue(InputValidator.isValidTitle("Task"))
        assertTrue(InputValidator.isValidTitle("My Task"))
        assertTrue(InputValidator.isValidTitle("A very long title that is still valid and should pass validation"))
    }

    @Test
    fun isValidTitle_withTooShort_returnsFalse() {
        assertFalse(InputValidator.isValidTitle("Hi"))
        assertFalse(InputValidator.isValidTitle(""))
    }

    @Test
    fun isValidTitle_withTooLong_returnsFalse() {
        val longTitle = "a".repeat(201)
        assertFalse(InputValidator.isValidTitle(longTitle))
    }

    @Test
    fun isValidTitle_withExactLimits_returnsTrue() {
        assertTrue(InputValidator.isValidTitle("abc")) // 3 chars
        assertTrue(InputValidator.isValidTitle("a".repeat(200))) // 200 chars
    }

    // ============= isValidDescription Tests =============
    @Test
    fun isValidDescription_withValidDescription_returnsTrue() {
        assertTrue(InputValidator.isValidDescription("This is a description"))
        assertTrue(InputValidator.isValidDescription("Short"))
    }

    @Test
    fun isValidDescription_withEmpty_returnsFalse() {
        assertFalse(InputValidator.isValidDescription(""))
        assertFalse(InputValidator.isValidDescription("   "))
    }

    @Test
    fun isValidDescription_withTooLong_returnsFalse() {
        val longDescription = "a".repeat(5001)
        assertFalse(InputValidator.isValidDescription(longDescription))
    }

    @Test
    fun isValidDescription_withMaxLength_returnsTrue() {
        val maxDescription = "a".repeat(5000)
        assertTrue(InputValidator.isValidDescription(maxDescription))
    }

    // ============= isValidAccessCode Tests =============
    @Test
    fun isValidAccessCode_withValidCode_returnsTrue() {
        assertTrue(InputValidator.isValidAccessCode("1234"))
        assertTrue(InputValidator.isValidAccessCode("58239617"))
        assertTrue(InputValidator.isValidAccessCode("0000000000")) // 10 digits
    }

    @Test
    fun isValidAccessCode_withTooShort_returnsFalse() {
        assertFalse(InputValidator.isValidAccessCode("123"))
    }

    @Test
    fun isValidAccessCode_withTooLong_returnsFalse() {
        assertFalse(InputValidator.isValidAccessCode("12345678901")) // 11 digits
    }

    @Test
    fun isValidAccessCode_withNonDigits_returnsFalse() {
        assertFalse(InputValidator.isValidAccessCode("123a"))
        assertFalse(InputValidator.isValidAccessCode("12 34"))
        assertFalse(InputValidator.isValidAccessCode(""))
    }

    @Test
    fun isValidAccessCode_withExactLimits_returnsTrue() {
        assertTrue(InputValidator.isValidAccessCode("1234")) // 4 digits minimum
        assertTrue(InputValidator.isValidAccessCode("1234567890")) // 10 digits maximum
    }

    // ============= isValidDate Tests =============
    @Test
    fun isValidDate_withValidDate_returnsTrue() {
        assertTrue(InputValidator.isValidDate("01/01/2025"))
        assertTrue(InputValidator.isValidDate("31/12/2025"))
        assertTrue(InputValidator.isValidDate("15/06/2025"))
    }

    @Test
    fun isValidDate_withInvalidFormat_returnsFalse() {
        assertFalse(InputValidator.isValidDate("2025-01-01"))
        assertFalse(InputValidator.isValidDate("01-01-2025"))
        assertFalse(InputValidator.isValidDate("1/1/2025"))
        assertFalse(InputValidator.isValidDate(""))
    }

    @Test
    fun isValidDate_withAnyValidFormat_returnsTrue() {
        // Note: isValidDate only validates format dd/MM/yyyy, not actual date values
        assertTrue(InputValidator.isValidDate("32/01/2025"))
        assertTrue(InputValidator.isValidDate("00/13/2025"))
    }

    // ============= isValidScore Tests =============
    @Test
    fun isValidScore_withValidScore_returnsTrue() {
        assertTrue(InputValidator.isValidScore("0"))
        assertTrue(InputValidator.isValidScore("50"))
        assertTrue(InputValidator.isValidScore("100"))
    }

    @Test
    fun isValidScore_withInvalidScore_returnsFalse() {
        assertFalse(InputValidator.isValidScore("-1"))
        assertFalse(InputValidator.isValidScore("101"))
        assertFalse(InputValidator.isValidScore("abc"))
        assertFalse(InputValidator.isValidScore(""))
    }

    @Test
    fun isValidScore_withDecimalScore_returnsTrue() {
        // Note: isValidScore accepts decimals because it uses toDouble()
        assertTrue(InputValidator.isValidScore("50.5"))
        assertTrue(InputValidator.isValidScore("0.0"))
        assertTrue(InputValidator.isValidScore("99.9"))
    }

    // ============= sanitize Tests =============
    @Test
    fun sanitize_removsDangerousCharacters() {
        assertEquals("test", InputValidator.sanitize("test<>"))
        assertEquals("testalert", InputValidator.sanitize("test>;alert()"))
        assertEquals("testscript", InputValidator.sanitize("test<script>"))
    }

    @Test
    fun sanitize_withCleanInput_returnsUnchanged() {
        assertEquals("Hello World", InputValidator.sanitize("Hello World"))
        assertEquals("123", InputValidator.sanitize("123"))
    }

    // ============= isValidClass Tests =============
    @Test
    fun isValidClass_withValidClass_returnsTrue() {
        assertTrue(InputValidator.isValidClass("A1"))
        assertTrue(InputValidator.isValidClass("Turma B"))
        assertTrue(InputValidator.isValidClass("9o ano turma A"))
    }

    @Test
    fun isValidClass_withTooShort_returnsFalse() {
        assertFalse(InputValidator.isValidClass("A"))
    }

    @Test
    fun isValidClass_withTooLong_returnsFalse() {
        val longClass = "a".repeat(51)
        assertFalse(InputValidator.isValidClass(longClass))
    }

    @Test
    fun isValidClass_withEmpty_returnsFalse() {
        assertFalse(InputValidator.isValidClass(""))
    }

    @Test
    fun isValidClass_withExactLimits_returnsTrue() {
        assertTrue(InputValidator.isValidClass("ab")) // 2 chars minimum
        assertTrue(InputValidator.isValidClass("a".repeat(50))) // 50 chars maximum
    }
}