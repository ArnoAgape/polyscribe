package com.arnoagape.polyscribe.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.arnoagape.polyscribe.ui.screen.detail.DetailScreen
import com.arnoagape.polyscribe.ui.screen.detail.DetailViewModel
import com.arnoagape.polyscribe.ui.screen.home.HomeScreen
import com.arnoagape.polyscribe.ui.screen.home.HomeViewModel
import com.arnoagape.polyscribe.ui.screen.login.LoginScreen
import com.arnoagape.polyscribe.ui.screen.login.LoginViewModel
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

    NavHost(
        navController = navController,
        startDestination = Login
    ) {
        composable<Detail> { backStackEntry ->
            val args = backStackEntry.toRoute<Detail>()
            DetailScreen(
                viewModel = hiltViewModel<DetailViewModel>(),
                onBackClick = { navController.navigateUp() },
                fileId = args.fileId
            )
        }

        composable<Home> {
            HomeScreen(
                viewModel = hiltViewModel<HomeViewModel>(),
                onFABClick = { navController.navigate(Send) },
                onProfileClick = { navController.navigate(Profile) },
                onFileClick = { navController.navigate(Detail) },
                onHomeClick = { navController.navigate(Home) }
            )
        }

        composable<Login> {
            LoginScreen(
                viewModel = hiltViewModel<LoginViewModel>(),
                onEmailSignInClick = { email, password ->
                    navController.navigate(Home)
                },
                onGoogleSignInClick = { navController.navigate(Home) },
                onGuestSignInClick = { navController.navigate(Home) },
            )
        }

        composable<Profile> {
            ProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
                onBackClick = { navController.navigateUp() }
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
                viewModel = hiltViewModel<SettingsViewModel>()
            )
        }
    }
}
