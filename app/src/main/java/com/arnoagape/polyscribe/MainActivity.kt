package com.arnoagape.polyscribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arnoagape.polyscribe.navigation.MainScreen
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity that sets up the app theme and hosts the root composable.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolyscribeTheme {
                MainScreen()
            }
        }
    }
}