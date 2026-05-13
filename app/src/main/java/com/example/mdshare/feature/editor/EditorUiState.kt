package com.example.mdshare.feature.editor

data class EditorUiState(
    val markdown: String = "",
    val title: String = "",
    val canGenerate: Boolean = false
)
