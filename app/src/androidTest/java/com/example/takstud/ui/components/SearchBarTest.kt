package com.example.takstud.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * Testes de UI para SearchBar
 */
class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSearchBar_DisplaysPlaceholder() {
        composeTestRule.setContent {
            SearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "Buscar..."
            )
        }

        composeTestRule.onNodeWithText("Buscar...").assertIsDisplayed()
    }

    @Test
    fun testSearchBar_DisplaysInitialText() {
        composeTestRule.setContent {
            SearchBar(
                query = "teste",
                onQueryChange = {},
                placeholder = "Buscar..."
            )
        }

        composeTestRule.onNodeWithText("teste").assertIsDisplayed()
    }

    @Test
    fun testSearchBar_CallsOnQueryChangeWhenTextEntered() {
        var capturedText = ""
        composeTestRule.setContent {
            SearchBar(
                query = "",
                onQueryChange = { capturedText = it },
                placeholder = "Buscar..."
            )
        }

        val textField = composeTestRule.onNodeWithContentDescription("Buscar")
        textField.performTextInput("novo texto")

        assert(capturedText.isNotEmpty())
    }

    @Test
    fun testSearchBar_ShowsClearButtonWhenNotEmpty() {
        composeTestRule.setContent {
            SearchBar(
                query = "texto",
                onQueryChange = {},
                placeholder = "Buscar..."
            )
        }

        composeTestRule.onNodeWithContentDescription("Limpar").assertIsDisplayed()
    }

    @Test
    fun testSearchBar_HidesClearButtonWhenEmpty() {
        composeTestRule.setContent {
            SearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "Buscar..."
            )
        }

        composeTestRule.onNodeWithContentDescription("Limpar").assertDoesNotExist()
    }

    @Test
    fun testSearchBar_ClearsTextWhenButtonClicked() {
        var currentText = "texto"
        composeTestRule.setContent {
            SearchBar(
                query = currentText,
                onQueryChange = { currentText = it },
                placeholder = "Buscar..."
            )
        }

        val clearButton = composeTestRule.onNodeWithContentDescription("Limpar")
        clearButton.performClick()

        assert(currentText.isEmpty())
    }
}
