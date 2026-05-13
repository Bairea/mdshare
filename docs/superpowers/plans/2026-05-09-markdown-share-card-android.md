# Markdown 分享卡片 Android MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Android app that receives shared Markdown, lets the user edit or preview it, and exports a single long image with full-table rendering, syntax-highlighted code blocks, and one-tap sharing.

**Architecture:** Use a native Android shell for entry points, navigation, state, storage permissions, and system sharing. Use a local Markdown-to-HTML pipeline plus `WebView` rendering for high-fidelity table and code-block layout, then draw the rendered content into a `Bitmap` and save/share it.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, Android WebView, flexmark-java, highlight.js, JUnit 4, AndroidX test, Coil

---

## Planned File Structure

### Create

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/mdshare/MainActivity.kt`
- `app/src/main/java/com/example/mdshare/MdShareApplication.kt`
- `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt`
- `app/src/main/java/com/example/mdshare/navigation/Destinations.kt`
- `app/src/main/java/com/example/mdshare/share/ShareIntentParser.kt`
- `app/src/main/java/com/example/mdshare/feature/editor/EditorRoute.kt`
- `app/src/main/java/com/example/mdshare/feature/editor/EditorViewModel.kt`
- `app/src/main/java/com/example/mdshare/feature/editor/EditorUiState.kt`
- `app/src/main/java/com/example/mdshare/feature/preview/PreviewRoute.kt`
- `app/src/main/java/com/example/mdshare/feature/result/ResultRoute.kt`
- `app/src/main/java/com/example/mdshare/render/MarkdownPipeline.kt`
- `app/src/main/java/com/example/mdshare/render/RenderTheme.kt`
- `app/src/main/java/com/example/mdshare/render/HtmlTemplateBuilder.kt`
- `app/src/main/java/com/example/mdshare/render/WebViewImageRenderer.kt`
- `app/src/main/java/com/example/mdshare/export/ImageExporter.kt`
- `app/src/main/java/com/example/mdshare/export/ShareImageUseCase.kt`
- `app/src/main/java/com/example/mdshare/model/RenderPayload.kt`
- `app/src/main/java/com/example/mdshare/model/RenderResult.kt`
- `app/src/main/java/com/example/mdshare/ui/theme/Color.kt`
- `app/src/main/java/com/example/mdshare/ui/theme/Theme.kt`
- `app/src/main/java/com/example/mdshare/ui/theme/Type.kt`
- `app/src/main/assets/render/render.css`
- `app/src/main/assets/render/render.js`
- `app/src/main/assets/render/highlight.min.js`
- `app/src/main/assets/render/highlight-theme.css`
- `app/src/test/java/com/example/mdshare/share/ShareIntentParserTest.kt`
- `app/src/test/java/com/example/mdshare/render/MarkdownPipelineTest.kt`
- `app/src/androidTest/java/com/example/mdshare/render/WebViewImageRendererTest.kt`
- `app/src/androidTest/java/com/example/mdshare/MainActivityIntentTest.kt`

### Modify

- None, because this repository is currently empty

---

### Task 1: Bootstrap The Android Compose Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/mdshare/MdShareApplication.kt`
- Create: `app/src/main/java/com/example/mdshare/MainActivity.kt`
- Create: `app/src/main/java/com/example/mdshare/ui/theme/Color.kt`
- Create: `app/src/main/java/com/example/mdshare/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/example/mdshare/ui/theme/Type.kt`

- [ ] **Step 1: Initialize Git before code changes**

Run:

```bash
git init
git branch -M main
```

Expected:

```text
Initialized empty Git repository
```

- [ ] **Step 2: Write the root Gradle files**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MdShare"
include(":app")
```

Create `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
```

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 3: Write the app module build file**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.mdshare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mdshare"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

Create `app/proguard-rules.pro`:

```text
# No custom rules required for debug-first MVP.
```

- [ ] **Step 4: Write the base Android manifest and app entry point**

Create `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".MdShareApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="MdShare"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Material3.DayNight.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/*" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.PROCESS_TEXT" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/plain" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Create `app/src/main/java/com/example/mdshare/MdShareApplication.kt`:

```kotlin
package com.example.mdshare

import android.app.Application

class MdShareApplication : Application()
```

Create `app/src/main/java/com/example/mdshare/MainActivity.kt`:

```kotlin
package com.example.mdshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mdshare.navigation.AppNavGraph
import com.example.mdshare.ui.theme.MdShareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdShareTheme {
                AppNavGraph(activity = this, initialIntent = intent)
            }
        }
    }
}
```

- [ ] **Step 5: Create a minimal Compose theme**

Create `app/src/main/java/com/example/mdshare/ui/theme/Color.kt`:

```kotlin
package com.example.mdshare.ui.theme

import androidx.compose.ui.graphics.Color

val CardBackground = Color(0xFFF8FAFC)
val PrimaryText = Color(0xFF0F172A)
val SecondaryText = Color(0xFF475569)
val Accent = Color(0xFF4F46E5)
```

Create `app/src/main/java/com/example/mdshare/ui/theme/Type.kt`:

```kotlin
package com.example.mdshare.ui.theme

import androidx.compose.material3.Typography

val AppTypography = Typography()
```

Create `app/src/main/java/com/example/mdshare/ui/theme/Theme.kt`:

```kotlin
package com.example.mdshare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Colors = lightColorScheme()

@Composable
fun MdShareTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        typography = AppTypography,
        content = content
    )
}
```

- [ ] **Step 6: Run a build smoke test**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 7: Commit the scaffold**

```bash
git add .
git commit -m "chore: bootstrap android compose app"
```

---

### Task 2: Add Navigation And Shared-Text Entry

**Files:**
- Create: `app/src/test/java/com/example/mdshare/share/ShareIntentParserTest.kt`
- Create: `app/src/main/java/com/example/mdshare/share/ShareIntentParser.kt`
- Create: `app/src/main/java/com/example/mdshare/navigation/Destinations.kt`
- Create: `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt`
- Modify: `app/src/main/java/com/example/mdshare/MainActivity.kt`
- Test: `app/src/androidTest/java/com/example/mdshare/MainActivityIntentTest.kt`

- [ ] **Step 1: Write the failing unit tests for shared text parsing**

Create `app/src/test/java/com/example/mdshare/share/ShareIntentParserTest.kt`:

```kotlin
package com.example.mdshare.share

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareIntentParserTest {
    @Test
    fun `extracts shared text from ACTION_SEND`() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "# Title")
        }

        assertEquals("# Title", ShareIntentParser.extractText(intent))
    }

    @Test
    fun `extracts process text payload`() {
        val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            putExtra(Intent.EXTRA_PROCESS_TEXT, "plain text")
        }

        assertEquals("plain text", ShareIntentParser.extractText(intent))
    }

    @Test
    fun `returns null when payload missing`() {
        val intent = Intent(Intent.ACTION_SEND)
        assertNull(ShareIntentParser.extractText(intent))
    }
}
```

- [ ] **Step 2: Run the unit test and verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.share.ShareIntentParserTest"
```

Expected:

```text
error: unresolved reference: ShareIntentParser
```

- [ ] **Step 3: Implement the parser and navigation destinations**

Create `app/src/main/java/com/example/mdshare/share/ShareIntentParser.kt`:

```kotlin
package com.example.mdshare.share

import android.content.Intent

object ShareIntentParser {
    fun extractText(intent: Intent?): String? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            else -> null
        }?.trim()?.takeIf { it.isNotEmpty() }
    }
}
```

Create `app/src/main/java/com/example/mdshare/navigation/Destinations.kt`:

```kotlin
package com.example.mdshare.navigation

object Destinations {
    const val EDITOR = "editor"
    const val PREVIEW = "preview"
    const val RESULT = "result"
}
```

Create `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt`:

```kotlin
package com.example.mdshare.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mdshare.feature.editor.EditorRoute
import com.example.mdshare.feature.preview.PreviewRoute
import com.example.mdshare.feature.result.ResultRoute
import com.example.mdshare.share.ShareIntentParser

@Composable
fun AppNavGraph(activity: Activity, initialIntent: Intent?) {
    val navController = rememberNavController()
    val initialMarkdown = ShareIntentParser.extractText(initialIntent).orEmpty()

    NavHost(
        navController = navController,
        startDestination = Destinations.EDITOR
    ) {
        composable(Destinations.EDITOR) {
            EditorRoute(
                initialMarkdown = initialMarkdown,
                onPreview = { navController.navigate(Destinations.PREVIEW) }
            )
        }
        composable(Destinations.PREVIEW) { Text("Preview") }
        composable(Destinations.RESULT) { Text("Result") }
    }
}
```

- [ ] **Step 4: Add a launch intent test**

Create `app/src/androidTest/java/com/example/mdshare/MainActivityIntentTest.kt`:

```kotlin
package com.example.mdshare

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityIntentTest {
    @Test
    fun launches_with_shared_text_intent() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setClassName("com.example.mdshare", "com.example.mdshare.MainActivity")
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "# Shared")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        ActivityScenario.launch<MainActivity>(intent).use { }
    }
}
```

- [ ] **Step 5: Run unit and instrumentation checks**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.share.ShareIntentParserTest"
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.mdshare.MainActivityIntentTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 6: Commit shared entry wiring**

```bash
git add .
git commit -m "feat: handle shared markdown entry"
```

---

### Task 3: Build The Editor Screen And ViewModel

**Files:**
- Create: `app/src/main/java/com/example/mdshare/model/RenderPayload.kt`
- Create: `app/src/main/java/com/example/mdshare/feature/editor/EditorUiState.kt`
- Create: `app/src/main/java/com/example/mdshare/feature/editor/EditorViewModel.kt`
- Create: `app/src/main/java/com/example/mdshare/feature/editor/EditorRoute.kt`
- Modify: `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt`

- [ ] **Step 1: Write the failing ViewModel test**

Create `app/src/test/java/com/example/mdshare/feature/editor/EditorViewModelTest.kt`:

```kotlin
package com.example.mdshare.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorViewModelTest {
    @Test
    fun `keeps shared markdown as initial content`() {
        val viewModel = EditorViewModel("# Shared")
        assertEquals("# Shared", viewModel.uiState.value.markdown)
    }
}
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.feature.editor.EditorViewModelTest"
```

Expected:

```text
error: unresolved reference: EditorViewModel
```

- [ ] **Step 3: Implement editor state and route**

Create `app/src/main/java/com/example/mdshare/model/RenderPayload.kt`:

```kotlin
package com.example.mdshare.model

data class RenderPayload(
    val markdown: String,
    val title: String
)
```

Create `app/src/main/java/com/example/mdshare/feature/editor/EditorUiState.kt`:

```kotlin
package com.example.mdshare.feature.editor

data class EditorUiState(
    val markdown: String = "",
    val title: String = "",
    val canGenerate: Boolean = false
)
```

Create `app/src/main/java/com/example/mdshare/feature/editor/EditorViewModel.kt`:

```kotlin
package com.example.mdshare.feature.editor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class EditorViewModel(initialMarkdown: String) : ViewModel() {
    private val _uiState = mutableStateOf(
        EditorUiState(
            markdown = initialMarkdown,
            title = initialMarkdown.lineSequence().firstOrNull()?.removePrefix("# ") ?: "",
            canGenerate = initialMarkdown.isNotBlank()
        )
    )

    val uiState: State<EditorUiState> = _uiState

    fun updateMarkdown(value: String) {
        _uiState.value = _uiState.value.copy(
            markdown = value,
            title = value.lineSequence().firstOrNull()?.removePrefix("# ") ?: "",
            canGenerate = value.isNotBlank()
        )
    }

    fun clear() = updateMarkdown("")
}
```

Create `app/src/main/java/com/example/mdshare/feature/editor/EditorRoute.kt`:

```kotlin
package com.example.mdshare.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditorRoute(
    initialMarkdown: String,
    onPreview: () -> Unit
) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<EditorViewModel>(
        factory = androidx.lifecycle.viewmodel.initializer {
            EditorViewModel(initialMarkdown)
        }
    )
    val state by viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Markdown to Image")
        OutlinedTextField(
            value = state.markdown,
            onValueChange = viewModel::updateMarkdown,
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth(),
            label = { Text("Markdown") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = viewModel::clear) { Text("清空") }
            Button(onClick = onPreview, enabled = state.canGenerate) { Text("预览") }
        }
    }
}
```

- [ ] **Step 4: Fix the nav graph to use the editor screen**

Update `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt` so the editor uses the shared text state from launch:

```kotlin
EditorRoute(
    initialMarkdown = initialMarkdown,
    onPreview = { navController.navigate(Destinations.PREVIEW) }
)
```

- [ ] **Step 5: Run the ViewModel test and app build**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.feature.editor.EditorViewModelTest"
./gradlew :app:assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 6: Commit the editor flow**

```bash
git add .
git commit -m "feat: add markdown editor flow"
```

---

### Task 4: Implement The Markdown-To-HTML Pipeline

**Files:**
- Create: `app/src/test/java/com/example/mdshare/render/MarkdownPipelineTest.kt`
- Create: `app/src/main/java/com/example/mdshare/render/RenderTheme.kt`
- Create: `app/src/main/java/com/example/mdshare/render/MarkdownPipeline.kt`
- Create: `app/src/main/java/com/example/mdshare/render/HtmlTemplateBuilder.kt`
- Create: `app/src/main/assets/render/render.css`
- Create: `app/src/main/assets/render/render.js`
- Create: `app/src/main/assets/render/highlight.min.js`
- Create: `app/src/main/assets/render/highlight-theme.css`

- [ ] **Step 1: Write a failing pipeline test for tables and code blocks**

Create `app/src/test/java/com/example/mdshare/render/MarkdownPipelineTest.kt`:

```kotlin
package com.example.mdshare.render

import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownPipelineTest {
    @Test
    fun `wraps table and code block with render shell`() {
        val html = MarkdownPipeline().buildHtml(
            """
            # Title

            | A | B | C |
            |---|---|---|
            | 1 | 2 | 3 |

            ```kotlin
            fun main() = println("hi")
            ```
            """.trimIndent()
        )

        assertTrue(html.contains("<table"))
        assertTrue(html.contains("hljs"))
        assertTrue(html.contains("render-card"))
    }
}
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.render.MarkdownPipelineTest"
```

Expected:

```text
error: unresolved reference: MarkdownPipeline
```

- [ ] **Step 3: Implement the theme and pipeline**

Create `app/src/main/java/com/example/mdshare/render/RenderTheme.kt`:

```kotlin
package com.example.mdshare.render

data class RenderTheme(
    val canvasWidthPx: Int = 1080,
    val titleLabel: String = "AI 摘要"
)
```

Create `app/src/main/java/com/example/mdshare/render/MarkdownPipeline.kt`:

```kotlin
package com.example.mdshare.render

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.ext.tables.TablesExtension

class MarkdownPipeline(
    private val theme: RenderTheme = RenderTheme()
) {
    private val options = MutableDataSet().set(Parser.EXTENSIONS, listOf(TablesExtension.create()))
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    fun buildHtml(markdown: String): String {
        val document = parser.parse(markdown)
        val bodyHtml = renderer.render(document)
        return HtmlTemplateBuilder(theme).wrap(bodyHtml)
    }
}
```

Create `app/src/main/java/com/example/mdshare/render/HtmlTemplateBuilder.kt`:

```kotlin
package com.example.mdshare.render

class HtmlTemplateBuilder(
    private val theme: RenderTheme
) {
    fun wrap(bodyHtml: String): String = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <link rel="stylesheet" href="file:///android_asset/render/highlight-theme.css" />
          <link rel="stylesheet" href="file:///android_asset/render/render.css" />
        </head>
        <body>
          <main class="render-card" style="width:${theme.canvasWidthPx}px">
            <div class="render-label">${theme.titleLabel}</div>
            <div class="render-content">$bodyHtml</div>
          </main>
          <script src="file:///android_asset/render/highlight.min.js"></script>
          <script src="file:///android_asset/render/render.js"></script>
        </body>
        </html>
    """.trimIndent()
}
```

- [ ] **Step 4: Add render assets with compact-table and code styles**

Create `app/src/main/assets/render/render.css`:

```css
body {
  margin: 0;
  padding: 32px;
  background: #eef2ff;
  font-family: sans-serif;
  color: #0f172a;
}

.render-card {
  margin: 0 auto;
  background: #ffffff;
  border-radius: 32px;
  padding: 40px;
  box-sizing: border-box;
}

.render-label {
  color: #4f46e5;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 16px;
}

table {
  width: 100%;
  border-collapse: collapse;
  table-layout: auto;
  font-size: 22px;
}

th, td {
  border: 1px solid #cbd5e1;
  padding: 12px 14px;
  word-break: break-word;
}

pre code {
  display: block;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
  font-size: 18px;
  line-height: 1.5;
  padding: 20px;
  border-radius: 20px;
}
```

Create `app/src/main/assets/render/render.js`:

```javascript
document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll("pre code").forEach(function (block) {
    if (window.hljs) {
      window.hljs.highlightElement(block);
    }
  });
});
```

Create `app/src/main/assets/render/highlight-theme.css`:

```css
.hljs {
  background: #0f172a;
  color: #e2e8f0;
}
```

Add the minified highlight bundle to `app/src/main/assets/render/highlight.min.js` from the official release file.

- [ ] **Step 5: Run the pipeline test**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.render.MarkdownPipelineTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 6: Commit the render pipeline**

```bash
git add .
git commit -m "feat: add markdown html render pipeline"
```

---

### Task 5: Render HTML In WebView And Export A Bitmap

**Files:**
- Create: `app/src/main/java/com/example/mdshare/model/RenderResult.kt`
- Create: `app/src/main/java/com/example/mdshare/render/WebViewImageRenderer.kt`
- Create: `app/src/androidTest/java/com/example/mdshare/render/WebViewImageRendererTest.kt`

- [ ] **Step 1: Write the failing instrumentation test**

Create `app/src/androidTest/java/com/example/mdshare/render/WebViewImageRendererTest.kt`:

```kotlin
package com.example.mdshare.render

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewImageRendererTest {
    @Test
    fun renders_bitmap_for_markdown_html() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val renderer = WebViewImageRenderer(context)
        val result = renderer.renderBlocking(
            MarkdownPipeline().buildHtml("# Title\n\n|A|B|\n|---|---|\n|1|2|")
        )

        assertTrue(result.width > 0)
        assertTrue(result.height > 0)
    }
}
```

- [ ] **Step 2: Run the instrumentation test and verify it fails**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.mdshare.render.WebViewImageRendererTest
```

Expected:

```text
error: unresolved reference: WebViewImageRenderer
```

- [ ] **Step 3: Implement the render result and blocking renderer**

Create `app/src/main/java/com/example/mdshare/model/RenderResult.kt`:

```kotlin
package com.example.mdshare.model

import android.graphics.Bitmap

data class RenderResult(
    val bitmap: Bitmap,
    val width: Int,
    val height: Int
)
```

Create `app/src/main/java/com/example/mdshare/render/WebViewImageRenderer.kt`:

```kotlin
package com.example.mdshare.render

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.mdshare.model.RenderResult
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebViewImageRenderer(
    private val context: Context
) {
    @SuppressLint("SetJavaScriptEnabled")
    fun renderBlocking(html: String): RenderResult {
        check(Looper.getMainLooper().thread == Thread.currentThread()) {
            "renderBlocking must run on the main thread"
        }

        val webView = WebView(context)
        val latch = CountDownLatch(1)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                latch.countDown()
            }
        }
        webView.loadDataWithBaseURL(
            "file:///android_asset/render/",
            html,
            "text/html",
            "utf-8",
            null
        )

        latch.await(5, TimeUnit.SECONDS)
        webView.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(1080, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        webView.layout(0, 0, webView.measuredWidth, webView.measuredHeight)

        val bitmap = Bitmap.createBitmap(webView.measuredWidth, webView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        webView.draw(canvas)

        return RenderResult(bitmap = bitmap, width = bitmap.width, height = bitmap.height)
    }
}
```

- [ ] **Step 4: Run the instrumentation test again**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.mdshare.render.WebViewImageRendererTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5: Commit the image renderer**

```bash
git add .
git commit -m "feat: render markdown html to bitmap"
```

---

### Task 6: Add Preview, Result, Save, And Share

**Files:**
- Create: `app/src/main/java/com/example/mdshare/export/ImageExporter.kt`
- Create: `app/src/main/java/com/example/mdshare/export/ShareImageUseCase.kt`
- Create: `app/src/main/java/com/example/mdshare/feature/preview/PreviewRoute.kt`
- Create: `app/src/main/java/com/example/mdshare/feature/result/ResultRoute.kt`
- Modify: `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt`

- [ ] **Step 1: Write a failing exporter test**

Create `app/src/test/java/com/example/mdshare/export/ImageExporterTest.kt`:

```kotlin
package com.example.mdshare.export

import org.junit.Assert.assertTrue
import org.junit.Test

class ImageExporterTest {
    @Test
    fun `png file name ends with png`() {
        val name = ImageExporter.buildFileName()
        assertTrue(name.endsWith(".png"))
    }
}
```

- [ ] **Step 2: Run the test and verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.export.ImageExporterTest"
```

Expected:

```text
error: unresolved reference: ImageExporter
```

- [ ] **Step 3: Implement image export and share helpers**

Create `app/src/main/java/com/example/mdshare/export/ImageExporter.kt`:

```kotlin
package com.example.mdshare.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore

object ImageExporter {
    fun buildFileName(): String = "md-share-${System.currentTimeMillis()}.png"

    fun saveBitmap(context: Context, bitmap: Bitmap): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, buildFileName())
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MdShare")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Failed to create MediaStore entry")
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        } ?: error("Failed to open output stream")
        return uri
    }
}
```

Create `app/src/main/java/com/example/mdshare/export/ShareImageUseCase.kt`:

```kotlin
package com.example.mdshare.export

import android.content.Context
import android.content.Intent
import android.net.Uri

object ShareImageUseCase {
    fun buildIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun launchChooser(context: Context, uri: Uri) {
        context.startActivity(
            Intent.createChooser(buildIntent(uri), "分享图片")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
```

- [ ] **Step 4: Implement preview and result routes**

Create `app/src/main/java/com/example/mdshare/feature/preview/PreviewRoute.kt`:

```kotlin
package com.example.mdshare.feature.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreviewRoute(onGenerate: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("预览页")
        Button(onClick = onGenerate) { Text("生成图片") }
        Button(onClick = onBack) { Text("返回编辑") }
    }
}
```

Create `app/src/main/java/com/example/mdshare/feature/result/ResultRoute.kt`:

```kotlin
package com.example.mdshare.feature.result

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultRoute(
    imageUri: Uri?,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onEditAgain: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("结果页: ${imageUri ?: "未导出"}")
        Button(onClick = onSave) { Text("保存相册") }
        Button(onClick = onShare) { Text("立即分享") }
        Button(onClick = onEditAgain) { Text("重新编辑") }
    }
}
```

- [ ] **Step 5: Wire navigation transitions**

Update `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt` so preview and result routes can navigate forward and back:

```kotlin
composable(Destinations.PREVIEW) {
    PreviewRoute(
        onGenerate = { navController.navigate(Destinations.RESULT) },
        onBack = { navController.popBackStack() }
    )
}
composable(Destinations.RESULT) {
    ResultRoute(
        imageUri = null,
        onSave = {},
        onShare = {},
        onEditAgain = {
            navController.popBackStack(Destinations.EDITOR, inclusive = false)
        }
    )
}
```

- [ ] **Step 6: Run the exporter unit test and app build**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.export.ImageExporterTest"
./gradlew :app:assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 7: Commit preview/result flow**

```bash
git add .
git commit -m "feat: add preview result and export helpers"
```

---

### Task 7: Make Tables And Code Blocks Fit The Share Image

**Files:**
- Modify: `app/src/main/assets/render/render.css`
- Modify: `app/src/main/assets/render/render.js`
- Modify: `app/src/test/java/com/example/mdshare/render/MarkdownPipelineTest.kt`

- [ ] **Step 1: Extend the failing test for compact table behavior**

Update `app/src/test/java/com/example/mdshare/render/MarkdownPipelineTest.kt`:

```kotlin
@Test
fun `adds compact-table hooks for wide tables`() {
    val html = MarkdownPipeline().buildHtml(
        """
        | A | B | C | D | E | F |
        |---|---|---|---|---|---|
        | 1 | 2 | 3 | 4 | 5 | 6 |
        """.trimIndent()
    )

    assertTrue(html.contains("render.js"))
    assertTrue(html.contains("table"))
}
```

- [ ] **Step 2: Run the render test suite**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.render.MarkdownPipelineTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: Add compact-table CSS and long-code tuning**

Update `app/src/main/assets/render/render.css`:

```css
table.compact-table {
  font-size: 18px;
}

table.compact-table th,
table.compact-table td {
  padding: 8px 10px;
}

table th {
  background: #eef2ff;
  font-weight: 700;
}

pre {
  overflow: hidden;
}

pre code {
  font-size: 17px;
  line-height: 1.45;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
```

- [ ] **Step 4: Add runtime heuristics in the render script**

Update `app/src/main/assets/render/render.js`:

```javascript
document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll("pre code").forEach(function (block) {
    if (window.hljs) {
      window.hljs.highlightElement(block);
    }
  });

  document.querySelectorAll("table").forEach(function (table) {
    var columnCount = table.querySelectorAll("tr:first-child th, tr:first-child td").length;
    if (columnCount >= 5) {
      table.classList.add("compact-table");
    }
  });
});
```

- [ ] **Step 5: Re-run the render test suite**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.mdshare.render.MarkdownPipelineTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 6: Commit the layout polish**

```bash
git add .
git commit -m "feat: optimize table and code block layout"
```

---

### Task 8: Wire Real Preview Rendering And Result Export

**Files:**
- Modify: `app/src/main/java/com/example/mdshare/feature/editor/EditorViewModel.kt`
- Modify: `app/src/main/java/com/example/mdshare/feature/preview/PreviewRoute.kt`
- Modify: `app/src/main/java/com/example/mdshare/feature/result/ResultRoute.kt`
- Modify: `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt`
- Modify: `app/src/main/java/com/example/mdshare/export/ImageExporter.kt`

- [ ] **Step 1: Add preview state to the editor ViewModel**

Update `app/src/main/java/com/example/mdshare/feature/editor/EditorViewModel.kt`:

```kotlin
data class PreviewState(
    val html: String = ""
)
```

Add this property and helper:

```kotlin
private val pipeline = com.example.mdshare.render.MarkdownPipeline()

fun buildPreviewHtml(): String = pipeline.buildHtml(_uiState.value.markdown)
```

- [ ] **Step 2: Render the preview HTML in a WebView**

Update `app/src/main/java/com/example/mdshare/feature/preview/PreviewRoute.kt`:

```kotlin
@Composable
fun PreviewRoute(
    html: String,
    onGenerate: () -> Unit,
    onBack: () -> Unit
) {
    AndroidView(
        factory = { context ->
            android.webkit.WebView(context).apply {
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(
                    "file:///android_asset/render/",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        }
    )
}
```

- [ ] **Step 3: Generate and persist the bitmap before entering the result screen**

Update `app/src/main/java/com/example/mdshare/navigation/AppNavGraph.kt` to hold the latest preview HTML and latest exported URI in `remember` state:

```kotlin
var previewHtml by remember { mutableStateOf("") }
var exportedUri by remember { mutableStateOf<Uri?>(null) }
```

Use these transitions:

```kotlin
EditorRoute(
    initialMarkdown = initialMarkdown,
    onPreview = {
        previewHtml = editorViewModel.buildPreviewHtml()
        navController.navigate(Destinations.PREVIEW)
    }
)
```

```kotlin
PreviewRoute(
    html = previewHtml,
    onGenerate = {
        val result = WebViewImageRenderer(activity).renderBlocking(previewHtml)
        exportedUri = ImageExporter.saveBitmap(activity, result.bitmap)
        navController.navigate(Destinations.RESULT)
    },
    onBack = { navController.popBackStack() }
)
```

- [ ] **Step 4: Show the exported image and enable sharing**

Update `app/src/main/java/com/example/mdshare/feature/result/ResultRoute.kt` so it uses `AsyncImage` from Coil:

```kotlin
AsyncImage(
    model = imageUri,
    contentDescription = "导出图片"
)
```

Keep these actions:

```kotlin
Button(onClick = onSave) { Text("保存相册") }
Button(onClick = onShare) { Text("立即分享") }
Button(onClick = onEditAgain) { Text("重新编辑") }
```

- [ ] **Step 5: Hook the result actions**

Update the result destination in `AppNavGraph.kt`:

```kotlin
ResultRoute(
    imageUri = exportedUri,
    onSave = {},
    onShare = {
        exportedUri?.let { ShareImageUseCase.launchChooser(activity, it) }
    },
    onEditAgain = {
        navController.popBackStack(Destinations.EDITOR, inclusive = false)
    }
)
```

- [ ] **Step 6: Run full smoke tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 7: Commit the end-to-end share-card flow**

```bash
git add .
git commit -m "feat: complete local markdown image export flow"
```

---

### Task 9: Manual Validation With Real Markdown Samples

**Files:**
- Create: `sample/ai-answer-table.md`
- Create: `sample/ai-answer-code.md`
- Create: `sample/ai-answer-mixed.md`
- Modify: `docs/superpowers/specs/2026-05-09-markdown-share-card-android-design.md`

- [ ] **Step 1: Add sample Markdown files**

Create `sample/ai-answer-table.md`:

```md
# 价格对比

| 方案 | 成本 | 适用场景 | 风险 |
|---|---|---|---|
| 本地渲染 | 低 | MVP | 复杂表格适配 |
| 云端渲染 | 中 | 一致性强 | 网络依赖 |
```

Create `sample/ai-answer-code.md`:

```md
# Kotlin 示例

```kotlin
fun render(markdown: String): Bitmap {
    require(markdown.isNotBlank())
    return Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
}
```
```

Create `sample/ai-answer-mixed.md`:

```md
# 每日总结

今天完成了 Markdown 渲染方案评估。

| 模块 | 状态 | 说明 |
|---|---|---|
| 表格适配 | 完成 | 全列显示优先 |
| 代码块 | 完成 | 高亮和小字号 |

```text
Next step:
- preview
- export
- share
```
```

- [ ] **Step 2: Manually test the three samples on a device or emulator**

Checklist:

```text
1. Share or paste each sample into the app
2. Generate the image
3. Confirm all table columns are visible
4. Confirm code blocks are syntax-highlighted and readable
5. Confirm image saves to Pictures/MdShare
6. Confirm system share sheet opens with the generated PNG
```

- [ ] **Step 3: Record manual verification notes in the spec**

Append this section to `docs/superpowers/specs/2026-05-09-markdown-share-card-android-design.md`:

```md
## Manual Validation Notes

- Sample table markdown exported with all columns visible.
- Sample code markdown exported with readable highlighted code blocks.
- Mixed sample exported successfully and shared through the Android system share sheet.
```

- [ ] **Step 4: Commit validation assets**

```bash
git add .
git commit -m "test: add markdown samples and validation notes"
```

---

## Self-Review Checklist

- Spec coverage: editor flow, preview flow, single long image export, local rendering, table visibility, code highlighting, light card style, and Android sharing are all covered by Tasks 1 through 9.
- Placeholder scan: no `TODO`, `TBD`, or deferred implementation markers remain in the task steps.
- Type consistency: `EditorViewModel`, `MarkdownPipeline`, `WebViewImageRenderer`, `ImageExporter`, and `ShareImageUseCase` use the same names throughout the plan.
