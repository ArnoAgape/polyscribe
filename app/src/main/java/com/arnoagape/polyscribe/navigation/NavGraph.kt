package com.arnoagape.polyscribe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arnoagape.polyscribe.domain.model.SessionType
import com.arnoagape.polyscribe.ui.screen.detail.DetailScreen
import com.arnoagape.polyscribe.ui.screen.detail.DetailViewModel
import com.arnoagape.polyscribe.ui.screen.home.HomeScreen
import com.arnoagape.polyscribe.ui.screen.home.HomeViewModel
import com.arnoagape.polyscribe.ui.screen.login.LoginScreen
import com.arnoagape.polyscribe.ui.screen.login.LoginViewModel
import com.arnoagape.polyscribe.ui.screen.login.launchers.rememberEmailSignUpLauncher
import com.arnoagape.polyscribe.ui.screen.login.launchers.rememberGoogleSignUpLauncher
import com.arnoagape.polyscribe.ui.screen.profile.ProfileScreen
import com.arnoagape.polyscribe.ui.screen.profile.ProfileViewModel
import com.arnoagape.polyscribe.ui.screen.send.SendScreen
import com.arnoagape.polyscribe.ui.screen.send.SendViewModel
import com.arnoagape.polyscribe.ui.screen.settings.SettingsScreen
import com.arnoagape.polyscribe.ui.screen.settings.SettingsViewModel

/**
 * Defines the navigation graph for the application.
 * Each destination binds its screen with the appropriate ViewModel.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: Any
) {

    // Sign-in launchers
    val emailSignUpLauncher = rememberEmailSignUpLauncher(hiltViewModel<LoginViewModel>())
    val googleSignUpLauncher = rememberGoogleSignUpLauncher(hiltViewModel<LoginViewModel>())

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable<Detail> {
            DetailScreen(
                viewModel = hiltViewModel<DetailViewModel>(),
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Home> {
            HomeScreen(
                viewModel = hiltViewModel<HomeViewModel>(),
                onFABClick = {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("sessionType", SessionType.Authenticated)
                    navController.navigate(Send)
                },
                onFileClick = { file -> navController.navigate(Detail(file.id)) }
            )
        }

        composable<Login> {
            LoginScreen(
                onGoogleSignInClick = { googleSignUpLauncher() },
                onEmailSignInClick = { emailSignUpLauncher() },
                onLoginSuccess = { session ->
                    when (session) {
                        SessionType.Guest -> {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("sessionType", SessionType.Guest)

                            navController.navigate(Send)
                        }

                        SessionType.Authenticated ->
                            navController.navigate(Home) {
                                popUpTo(Login) { inclusive = true }
                            }
                    }
                }
            )
        }

        composable<Profile> {
            val viewModel = hiltViewModel<ProfileViewModel>()
            val session by viewModel.session.collectAsStateWithLifecycle()

            LaunchedEffect(session.isGuest) {
                if (session.isGuest) {
                    navController.navigate(Login) {
                        popUpTo(Profile) { inclusive = true }
                    }
                }
            }

            if (!session.isGuest) {
                ProfileScreen(viewModel = viewModel)
            }
        }

        composable<Send> {
            SendScreen(
                viewModel = hiltViewModel<SendViewModel>(),
                onBackClick = { navController.popBackStack() },
                onSaveClick = { session ->
                    when (session) {
                        SessionType.Guest ->
                            navController.navigate(Login) {
                                popUpTo(0) { inclusive = true }
                            }

                        SessionType.Authenticated ->
                            navController.navigate(Home) {
                                popUpTo(Send) { inclusive = true }
                            }
                    }
                }
            )
        }

        composable<Settings> {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>()
            )
        }
    }
}