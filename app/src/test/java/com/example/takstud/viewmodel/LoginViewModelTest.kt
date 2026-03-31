package com.example.takstud.viewmodel

import com.example.takstud.data.repository.StudentAuthRepository
import com.example.takstud.util.Result
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse as ktAssertFalse
import kotlin.test.assertTrue as ktAssertTrue

/**
 * Unit tests for LoginViewModel
 * Tests parent and teacher login flows with mocked repository
 */
class LoginViewModelTest {

    private lateinit var mockRepository: StudentAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        mockRepository = mockk<StudentAuthRepository>()
        viewModel = LoginViewModel(mockRepository)
    }

    @Test
    fun testParentLoginEmptyRA() {
        // Act - Call with empty RA
        viewModel.loginAsParent("")

        // Assert - Should result in error state
        val state = viewModel.parentLoginState.value
        ktAssertTrue(state is Result.Error)
    }

    @Test
    fun testTeacherLoginEmptyCode() {
        // Act - Call with empty code
        viewModel.loginAsTeacher("")

        // Assert - Should result in error state
        val state = viewModel.teacherLoginState.value
        ktAssertTrue(state is Result.Error)
    }

    @Test
    fun testTeacherLoginBlankCode() {
        // Act - Call with blank code (spaces only)
        viewModel.loginAsTeacher("   ")

        // Assert - Should result in error state
        val state = viewModel.teacherLoginState.value
        ktAssertTrue(state is Result.Error)
    }

    @Test
    fun testResetLoginState() {
        // Act - Reset the login state
        viewModel.resetLoginState()

        // Assert - Both states should be loading after reset
        val parentState = viewModel.parentLoginState.value
        val teacherState = viewModel.teacherLoginState.value
        val isLoading = viewModel.isLoading.value

        ktAssertTrue(parentState is Result.Loading)
        ktAssertTrue(teacherState is Result.Loading)
        ktAssertFalse(isLoading)
    }

    @Test
    fun testInitialIsLoadingState() {
        // Assert - Initial loading state should be false
        ktAssertFalse(viewModel.isLoading.value)
    }
}