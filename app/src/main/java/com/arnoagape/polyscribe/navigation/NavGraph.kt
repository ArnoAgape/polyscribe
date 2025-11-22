package com.arnoagape.polyscribe.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arnoagape.polyscribe.ui.screen.detail.DetailScreen
import com.arnoagape.polyscribe.ui.screen.detail.DetailViewModel
import com.arnoagape.polyscribe.ui.screen.home.HomeScreen
import com.arnoagape.polyscribe.ui.screen.home.HomeViewModel
import com.arnoagape.polyscribe.ui.screen.login.AuthCheckScreen
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController
) {

    val emailSignUpLauncher = rememberEmailSignUpLauncher(
        navController = navController,
        loginViewModel = hiltViewModel<LoginViewModel>(),
    )
    val googleSignUpLauncher = rememberGoogleSignUpLauncher(
        navController = navController,
        loginViewModel = hiltViewModel<LoginViewModel>(),
    )

    NavHost(
        navController = navController,
        startDestination = AuthCheck
    ) {

        composable<AuthCheck> {
            AuthCheckScreen(navController)
        }

        composable<Detail> {
            DetailScreen(
                viewModel = hiltViewModel<DetailViewModel>(),
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<Home> {
            HomeScreen(
                viewModel = hiltViewModel<HomeViewModel>(),
                loginViewModel = hiltViewModel<LoginViewModel>(),
                onFABClick = { navController.navigate(Send) },
                onFileClick = { file -> navController.navigate(Detail(file.id)) }
            )
        }

        composable<Login> {
            LoginScreen(
                viewModel = hiltViewModel<LoginViewModel>(),
                navController = navController,
                onGoogleSignInClick = { googleSignUpLauncher() },
                onEmailSignInClick = { emailSignUpLauncher() },
                onGuestSignInClick = { navController.navigate(Home) }
            )
        }

        composable<Profile> {
            ProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
                onLoginScreen = { navController.navigate(Login) }
            )
        }

        composable<Send> {
            SendScreen(
                viewModel = hiltViewModel<SendViewModel>(),
                onBackClick = { navController.navigateUp() },
                onSaveClick = { navController.navigateUp() }
            )
        }

        composable<Settings> {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
            )
        }
    }
}
