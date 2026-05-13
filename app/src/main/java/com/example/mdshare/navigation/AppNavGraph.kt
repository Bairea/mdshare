package com.example.mdshare.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mdshare.feature.editor.EditorRoute
import com.example.mdshare.feature.preview.PreviewRoute
import com.example.mdshare.feature.result.ResultRoute
import com.example.mdshare.export.ImageExporter
import com.example.mdshare.export.ShareImageUseCase
import com.example.mdshare.render.MarkdownPipeline
import com.example.mdshare.render.WebViewImageRenderer
import com.example.mdshare.share.ShareIntentParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavGraph(activity: Activity, initialIntent: Intent?) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val initialMarkdown = ShareIntentParser.extractText(initialIntent).orEmpty()
    var previewHtml by remember { mutableStateOf("") }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var exportedBitmapSaved by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = Destinations.EDITOR
    ) {
        composable(Destinations.EDITOR) {
            EditorRoute(
                initialMarkdown = initialMarkdown,
                onPreview = { payload ->
                    previewHtml = MarkdownPipeline().buildHtml(payload.markdown)
                    errorMessage = null
                    navController.navigate(Destinations.PREVIEW)
                }
            )
        }
        composable(Destinations.PREVIEW) {
            var isGenerating by remember { mutableStateOf(false) }

            PreviewRoute(
                html = previewHtml,
                isGenerating = isGenerating,
                errorMessage = errorMessage,
                onGenerate = { previewWebView ->
                    coroutineScope.launch {
                        isGenerating = true
                        errorMessage = null
                        try {
                            renderedBitmap = withContext(Dispatchers.IO) {
                                WebViewImageRenderer(activity)
                                    .captureExistingWebViewBlocking(previewWebView)
                                    .bitmap
                            }
                            exportedBitmapSaved = false
                            navController.navigate(Destinations.RESULT)
                        } catch (throwable: Throwable) {
                            errorMessage = throwable.message ?: "生成图片失败"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Destinations.RESULT) {
            ResultRoute(
                bitmap = renderedBitmap,
                hasSavedImage = exportedBitmapSaved,
                errorMessage = errorMessage,
                onSave = {
                    renderedBitmap?.let { bitmap ->
                        coroutineScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    ImageExporter.saveBitmap(activity, bitmap)
                                }
                                exportedBitmapSaved = true
                                errorMessage = null
                            } catch (throwable: Throwable) {
                                errorMessage = throwable.message ?: "保存图片失败"
                            }
                        }
                    }
                },
                onShare = {
                    renderedBitmap?.let { bitmap ->
                        coroutineScope.launch {
                            try {
                                val uri = withContext(Dispatchers.IO) {
                                    ImageExporter.saveBitmap(activity, bitmap)
                                }
                                exportedBitmapSaved = true
                                errorMessage = null
                                ShareImageUseCase.launchChooser(activity, uri)
                            } catch (throwable: Throwable) {
                                errorMessage = throwable.message ?: "分享图片失败"
                            }
                        }
                    }
                },
                onEditAgain = {
                    navController.popBackStack(Destinations.EDITOR, inclusive = false)
                }
            )
        }
    }
}
