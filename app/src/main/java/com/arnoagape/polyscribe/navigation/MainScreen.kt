package com.arnoagape.polyscribe.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arnoagape.polyscribe.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {

    val context = LocalContext.current
    val showMessage: (String) -> Unit = { msg ->
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val isBottomBarDestination = currentRoute == Home::class.qualifiedName ||
            currentRoute == Profile::class.qualifiedName || currentRoute == Settings::class.qualifiedName

    if (isBottomBarDestination) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            Icon(destination.icon, contentDescription = destination.label())
                        },
                        label = { Text(destination.label()) },
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        )
        {
            AppNavGraph(navController = navController, showMessage = showMessage)
        }
    } else {
        AppNavGraph(navController = navController, showMessage = showMessage)
    }
}

enum class AppDestinations(
    val icon: ImageVector,
    private val labelRes: Int,
    val route: Any
) {
    HOME(Icons.Default.Home, R.string.home, Home),
    PROFILE(Icons.Default.AccountBox, R.string.profile, Profile),
    SETTINGS(Icons.Default.Settings, R.string.settings, Settings);

    @Composable
    fun label(): String = stringResource(id = labelRes)
}