package com.example.mdshare.feature.editor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.mdshare.model.RenderPayload

class EditorViewModel(initialMarkdown: String) : ViewModel() {
    private val _uiState = mutableStateOf(buildUiState(initialMarkdown))
    val uiState: State<EditorUiState> = _uiState

    fun updateMarkdown(value: String) {
        _uiState.value = buildUiState(value)
    }

    fun clear() {
        updateMarkdown("")
    }

    fun toRenderPayload(): RenderPayload? {
        val state = _uiState.value
        if (!state.canGenerate) return null
        return RenderPayload(
            markdown = state.markdown,
            title = state.title.ifBlank { "Markdown Share" }
        )
    }

    private fun buildUiState(markdown: String): EditorUiState {
        val title = extractTitle(markdown)
        return EditorUiState(
            markdown = markdown,
            title = title,
            canGenerate = markdown.isNotBlank()
        )
    }

    private fun extractTitle(markdown: String): String {
        val firstLine = markdown.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()

        return when {
            firstLine.startsWith("# ") -> firstLine.removePrefix("# ").trim()
            else -> firstLine
        }
    }
}
