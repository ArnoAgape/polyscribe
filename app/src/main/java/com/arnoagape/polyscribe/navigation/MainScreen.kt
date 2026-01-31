package com.arnoagape.polyscribe.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.screen.login.LoginViewModel
import kotlinx.coroutines.flow.map

/**
 * Root composable of the app.
 * Hosts navigation, bottom bar visibility logic
 * and injects required ViewModels.
 */
@Composable
fun MainScreen() {

    val loginViewModel: LoginViewModel = hiltViewModel()
    val isSignedIn by loginViewModel.state
        .map { it.isSignedIn }
        .collectAsStateWithLifecycle(initialValue = null)

    if (isSignedIn == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination =
        if (isSignedIn == true) Home else Login

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar =
            currentRoute == Home::class.qualifiedName ||
                    currentRoute == Profile::class.qualifiedName ||
                    currentRoute == Settings::class.qualifiedName

        if (showBottomBar) {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestinations.entries.forEach { destination ->
                        item(
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.label()
                                )
                            },
                            label = { Text(destination.label()) },
                            selected = currentRoute == destination.routeName,
                            onClick = {
                                if (currentRoute == destination.routeName) return@item
                                navController.navigate(destination.screenObject) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            ) {
                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        } else {
            AppNavGraph(
                navController = navController,
                startDestination = startDestination
            )
        }
}

/**
 * Enum listing the main screens of the app,
 * along with their icons, labels, and navigation routes.
 */
enum class AppDestinations(
    val icon: ImageVector,
    private val labelRes: Int,
    val screenObject: Any
) {
    HOME(Icons.Default.Home, R.string.home, Home),
    PROFILE(Icons.Default.AccountBox, R.string.profile, Profile),
    SETTINGS(Icons.Default.Settings, R.string.settings, Settings);

    /** Returns the localized label of the destination. */
    @Composable
    fun label(): String = stringResource(id = labelRes)

    /** Class name used as navigation route. */
    val routeName: String get() = screenObject::class.qualifiedName!!
}