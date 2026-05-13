package com.example.mdshare.feature.editor

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithText
import com.example.mdshare.ui.theme.MdShareTheme
import org.junit.Rule
import org.junit.Test

class EditorRouteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shows_action_buttons_below_editor_content() {
        composeRule.setContent {
            MdShareTheme {
                EditorRoute(
                    initialMarkdown = """
                        # Kotlin 示例

                        ```kotlin
                        fun render(markdown: String): String {
                            require(markdown.isNotBlank())
                            return markdown.trim()
                        }
                        ```
                    """.trimIndent(),
                    onPreview = {}
                )
            }
        }

        composeRule.onNodeWithText("清空").assertIsDisplayed()
        composeRule.onNodeWithText("预览").assertIsDisplayed()
    }

    @Test
    fun keeps_action_buttons_visible_after_clear() {
        composeRule.setContent {
            MdShareTheme {
                EditorRoute(
                    initialMarkdown = "# Kotlin 示例",
                    onPreview = {}
                )
            }
        }

        composeRule.onNodeWithText("清空").performClick()
        composeRule.onNodeWithText("清空").assertIsDisplayed()
        composeRule.onNodeWithText("预览").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun editor_page_is_scrollable_for_long_content() {
        composeRule.setContent {
            MdShareTheme {
                EditorRoute(
                    initialMarkdown = (1..40).joinToString("\n") { "第$it 行 markdown 内容" },
                    onPreview = {}
                )
            }
        }

        composeRule.onNodeWithTag("editor_page_scroll").assert(hasScrollAction())
    }
}
