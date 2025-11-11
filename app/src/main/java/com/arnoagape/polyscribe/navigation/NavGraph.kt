package com.arnoagape.polyscribe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.arnoagape.polyscribe.ui.screen.detail.DetailScreen
import com.arnoagape.polyscribe.ui.screen.detail.DetailViewModel
import com.arnoagape.polyscribe.ui.screen.home.HomeScreen
import com.arnoagape.polyscribe.ui.screen.home.HomeViewModel
import com.arnoagape.polyscribe.ui.screen.login.LoginPlaceholderScreen
import com.arnoagape.polyscribe.ui.screen.login.rememberSignInLauncher
import com.arnoagape.polyscribe.ui.screen.profile.ProfileScreen
import com.arnoagape.polyscribe.ui.screen.profile.ProfileViewModel
import com.arnoagape.polyscribe.ui.screen.send.SendScreen
import com.arnoagape.polyscribe.ui.screen.send.SendViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    showMessage: (String) -> Unit
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()

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
            val signInLauncher = rememberSignInLauncher(
                navController = navController,
                showMessage = showMessage,
                profileViewModel = profileViewModel
            )
            LaunchedEffect(Unit) {
                signInLauncher()
            }
            LoginPlaceholderScreen()
        }

        composable<Profile> {
            ProfileScreen(
                viewModel = hiltViewModel<ProfileViewModel>(),
            )
        }

        composable<Send> {
            SendScreen(
                viewModel = hiltViewModel<SendViewModel>(),
                onBackClick = { navController.navigateUp() },
                onSaveClick = { navController.navigateUp() }
            )
        }
    }
}
