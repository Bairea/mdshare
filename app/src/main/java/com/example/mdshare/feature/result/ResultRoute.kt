package com.example.mdshare.feature.result

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun ResultRoute(
    bitmap: Bitmap?,
    hasSavedImage: Boolean,
    errorMessage: String?,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onEditAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        bitmap?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "导出图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )
        } ?: Text(
            text = "暂无生成结果",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (hasSavedImage) "已保存" else "保存相册")
            }
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Text("立即分享")
            }
            Button(
                onClick = onEditAgain,
                modifier = Modifier.weight(1f)
            ) {
                Text("重新编辑")
            }
        }
    }
}
