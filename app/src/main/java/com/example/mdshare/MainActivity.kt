package com.example.mdshare

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mdshare.navigation.AppNavGraph
import com.example.mdshare.ui.theme.MdShareTheme

class MainActivity : ComponentActivity() {
    private var currentIntent: Intent? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent = intent
        setContent {
            MdShareTheme {
                AppNavGraph(activity = this, initialIntent = currentIntent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
    }
}
