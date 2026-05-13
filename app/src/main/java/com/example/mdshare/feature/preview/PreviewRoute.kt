package com.example.mdshare.feature.preview

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewRoute(
    html: String,
    isGenerating: Boolean,
    errorMessage: String?,
    onGenerate: (WebView) -> Unit,
    onBack: () -> Unit
) {
    var previewWebView by remember { mutableStateOf<WebView?>(null) }
    var isPreviewReady by remember(html) { mutableStateOf(false) }
    var loadedHtml by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "预览",
            style = MaterialTheme.typography.headlineSmall
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                    isVerticalScrollBarEnabled = true
                    setBackgroundColor(android.graphics.Color.WHITE)
                    clearCache(true)
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isPreviewReady = true
                        }
                    }
                    previewWebView = this
                    loadedHtml = html
                    loadDataWithBaseURL(
                        "file:///android_asset/render/",
                        html,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            },
            update = { webView ->
                previewWebView = webView
                if (loadedHtml != html) {
                    isPreviewReady = false
                    loadedHtml = html
                    webView.clearCache(true)
                    webView.loadDataWithBaseURL(
                        "file:///android_asset/render/",
                        html,
                        "text/html",
                        "utf-8",
                        null
                    )
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = !isGenerating
            ) {
                Text("返回编辑")
            }
            Button(
                onClick = {
                    previewWebView?.let(onGenerate)
                },
                enabled = !isGenerating && html.isNotBlank() && isPreviewReady && previewWebView != null
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(if (isGenerating) "生成中" else "生成图片")
            }
        }
    }
}
