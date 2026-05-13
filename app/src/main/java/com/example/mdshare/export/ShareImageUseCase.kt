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
