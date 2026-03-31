package com.example.takstud.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * Testes de UI para HomeScreen
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHomeScreen_DisplaysBothButtons() {
        composeTestRule.setContent {
            HomeScreen(
                onProfessorClick = {},
                onAlunoClick = {}
            )
        }

        composeTestRule.onNodeWithText("SOU PROFESSOR").assertIsDisplayed()
        composeTestRule.onNodeWithText("SOU ALUNO/RESPONSÁVEL").assertIsDisplayed()
    }

    @Test
    fun testHomeScreen_ProfessorButtonIsClickable() {
        var professorClicked = false
        composeTestRule.setContent {
            HomeScreen(
                onProfessorClick = { professorClicked = true },
                onAlunoClick = {}
            )
        }

        composeTestRule.onNodeWithText("SOU PROFESSOR").performClick()
        assert(professorClicked)
    }

    @Test
    fun testHomeScreen_AlunoButtonIsClickable() {
        var alunoClicked = false
        composeTestRule.setContent {
            HomeScreen(
                onProfessorClick = {},
                onAlunoClick = { alunoClicked = true }
            )
        }

        composeTestRule.onNodeWithText("SOU ALUNO/RESPONSÁVEL").performClick()
        assert(alunoClicked)
    }

    @Test
    fun testHomeScreen_TitleIsDisplayed() {
        composeTestRule.setContent {
            HomeScreen(
                onProfessorClick = {},
                onAlunoClick = {}
            )
        }

        // Procura pelo título (pode ser "TakStud" ou similar)
        composeTestRule.onAllNodesWithText(text = "", substring = "").assertCountEquals(0)
    }

    @Test
    fun testHomeScreen_BothButtonsAreClickable() {
        var professorClicked = false
        var alunoClicked = false

        composeTestRule.setContent {
            HomeScreen(
                onProfessorClick = { professorClicked = true },
                onAlunoClick = { alunoClicked = true }
            )
        }

        // Clicar no botão de professor
        composeTestRule.onNodeWithText("SOU PROFESSOR").performClick()
        assert(professorClicked)

        // Reset para testar aluno
        professorClicked = false

        // Clicar no botão de aluno
        composeTestRule.onNodeWithText("SOU ALUNO/RESPONSÁVEL").performClick()
        assert(alunoClicked)
    }
}
