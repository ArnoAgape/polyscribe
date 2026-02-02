package com.arnoagape.polyscribe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arnoagape.polyscribe.navigation.MainScreen
import com.arnoagape.polyscribe.ui.utils.SharedFilesHolder
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.arnoagape.polyscribe.ui.utils.parcelableArrayListExtraCompat
import com.arnoagape.polyscribe.ui.utils.parcelableExtra
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity that sets up the app theme and hosts the root composable.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)
        enableEdgeToEdge()
        setContent {
            PolyscribeTheme {
                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.parcelableExtra<Uri>(Intent.EXTRA_STREAM)
                uri?.let {
                    SharedFilesHolder.add(it)
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                val uris =
                    intent.parcelableArrayListExtraCompat<Uri>(Intent.EXTRA_STREAM)
                uris?.forEach {
                    SharedFilesHolder.add(it)
                }
            }
        }
    }
}