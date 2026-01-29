package com.arnoagape.polyscribe.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.screen.login.LoginViewModel

/**
 * Root composable of the app.
 * Hosts navigation, bottom bar visibility logic
 * and injects required ViewModels.
 */
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val loginViewModel: LoginViewModel = hiltViewModel()
    val isSignedIn by loginViewModel.isSignedIn.collectAsStateWithLifecycle()

    LaunchedEffect(isSignedIn) {
        if (isSignedIn == false) {
            navController.navigate(Login) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Determines when the bottom bar should be displayed
    val isBottomBarDestination =
        currentRoute == Home::class.qualifiedName ||
                currentRoute == Profile::class.qualifiedName ||
                currentRoute == Settings::class.qualifiedName

    if (isBottomBarDestination) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            Icon(destination.icon, contentDescription = destination.label())
                        },
                        label = { Text(destination.label()) },
                        selected = currentRoute == destination.routeName,
                        onClick = {
                            if (currentRoute == destination.routeName) return@item
                            if (destination == AppDestinations.PROFILE) {
                                if (loginViewModel.isSignedIn.value == true) {
                                    navController.navigate(Profile)
                                } else {
                                    navController.navigate(AuthCheck)
                                }
                            } else {
                                navController.navigate(destination.screenObject) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) {
            AppNavGraph(navController = navController)
        }
    } else {
        AppNavGraph(navController = navController)
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