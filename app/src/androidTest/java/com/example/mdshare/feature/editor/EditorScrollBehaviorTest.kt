package com.example.mdshare.feature.editor

import android.content.Intent
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mdshare.MainActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorScrollBehaviorTest {
    @Test
    fun edit_text_scrolls_after_long_markdown_is_loaded() {
        val longMarkdown = buildString {
            appendLine("# 每日总结")
            repeat(80) { index ->
                appendLine("第${index + 1}行 很长的 Markdown 内容，用于验证输入后编辑区仍然可以向下滚动。")
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            setClassName("com.example.mdshare", "com.example.mdshare.MainActivity")
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, longMarkdown)
        }

        ActivityScenario.launch<MainActivity>(intent).use {
            onView(isAssignableFrom(EditText::class.java)).perform(click(), swipeUp())
            onView(isAssignableFrom(EditText::class.java)).check(matches(hasScrollYGreaterThanZero()))
            pressBack()
            onView(isAssignableFrom(EditText::class.java)).perform(swipeUp())
            onView(isAssignableFrom(EditText::class.java)).check(matches(hasScrollYGreaterThanZero()))
        }
    }

    private fun hasScrollYGreaterThanZero(): Matcher<android.view.View> {
        return object : TypeSafeMatcher<android.view.View>() {
            override fun describeTo(description: Description) {
                description.appendText("view with scrollY greater than zero")
            }

            override fun matchesSafely(view: android.view.View): Boolean {
                return view.scrollY > 0
            }
        }
    }
}
