package com.example.mdshare.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorViewModelTest {
    @Test
    fun `keeps shared markdown as initial content`() {
        val viewModel = EditorViewModel("# Shared")

        assertEquals("# Shared", viewModel.uiState.value.markdown)
    }

    @Test
    fun `derives title and generate state from current markdown`() {
        val viewModel = EditorViewModel("# Shared")

        assertEquals("Shared", viewModel.uiState.value.title)
        assertEquals(true, viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `clear resets content title and generate state`() {
        val viewModel = EditorViewModel("# Shared")

        viewModel.clear()

        assertEquals("", viewModel.uiState.value.markdown)
        assertEquals("", viewModel.uiState.value.title)
        assertEquals(false, viewModel.uiState.value.canGenerate)
    }

    @Test
    fun `update markdown refreshes fallback title and generate state`() {
        val viewModel = EditorViewModel("")

        viewModel.updateMarkdown("Plain title\n\nBody")

        assertEquals("Plain title", viewModel.uiState.value.title)
        assertEquals(true, viewModel.uiState.value.canGenerate)
    }
}
