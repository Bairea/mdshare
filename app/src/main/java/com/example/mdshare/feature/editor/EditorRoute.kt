package com.example.mdshare.feature.editor

import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mdshare.model.RenderPayload

@Composable
fun EditorRoute(
    initialMarkdown: String,
    onPreview: (RenderPayload) -> Unit
) {
    val factory = remember(initialMarkdown) {
        EditorViewModelFactory(initialMarkdown = initialMarkdown)
    }
    val viewModel: EditorViewModel = viewModel(
        key = "editor:$initialMarkdown",
        factory = factory
    )
    val state by viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "编辑 Markdown",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = state.title.ifBlank { "未识别标题，将在后续渲染阶段兜底生成" },
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Markdown",
            style = MaterialTheme.typography.labelMedium
        )
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .testTag("editor_scroll"),
            factory = { context ->
                EditText(context).apply {
                    minLines = 20
                    maxLines = Int.MAX_VALUE
                    gravity = Gravity.TOP or Gravity.START
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    setHorizontallyScrolling(false)
                    isVerticalScrollBarEnabled = true
                    overScrollMode = EditText.OVER_SCROLL_ALWAYS
                    hint = "粘贴或分享 Markdown 内容"
                    setPadding(32, 32, 32, 32)
                    setEditorColors(colorScheme)
                    setText(state.markdown)
                    setSelection(text.length)
                    doAfterTextChanged { editable ->
                        val current = editable?.toString().orEmpty()
                        if (current != state.markdown) {
                            viewModel.updateMarkdown(current)
                        }
                    }
                }
            },
            update = { editText ->
                editText.setEditorColors(colorScheme)
                val currentText = editText.text.toString()
                if (currentText != state.markdown) {
                    val selection = editText.selectionStart.coerceAtLeast(0)
                    editText.setText(state.markdown)
                    editText.setSelection(selection.coerceAtMost(editText.text.length))
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = viewModel::clear
            ) {
                Text("清空")
            }
            Button(
                onClick = {
                    viewModel.toRenderPayload()?.let { payload ->
                        onPreview(payload)
                    }
                },
                enabled = state.canGenerate
            ) {
                Text("预览")
            }
        }
    }
}

private fun EditText.setEditorColors(colorScheme: ColorScheme) {
    setBackgroundColor(colorScheme.surface.toArgb())
    setTextColor(colorScheme.onSurface.toArgb())
    setHintTextColor(colorScheme.onSurfaceVariant.toArgb())
}

private class EditorViewModelFactory(
    private val initialMarkdown: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(EditorViewModel::class.java))
        return EditorViewModel(initialMarkdown) as T
    }
}
