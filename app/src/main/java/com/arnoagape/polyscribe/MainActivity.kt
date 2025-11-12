package com.arnoagape.polyscribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arnoagape.polyscribe.navigation.AppNavGraph
import com.arnoagape.polyscribe.navigation.Home
import com.arnoagape.polyscribe.navigation.Profile
import com.arnoagape.polyscribe.ui.components.BottomBar
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolyscribeTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry.value?.destination
                val showMessage: (String) -> Unit = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
                val bottomBarRoutes = listOf(
                    Home::class.simpleName,
                    Profile::class.simpleName
                )

                Scaffold(
                    bottomBar = {
                        if (currentDestination?.route in bottomBarRoutes) {
                            BottomBar(navController)
                        }
                    }
                ) { contentPadding ->
                    Box(Modifier.padding(contentPadding)) {
                        AppNavGraph(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}