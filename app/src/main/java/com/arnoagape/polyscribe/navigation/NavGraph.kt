package com.arnoagape.polyscribe.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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

    val loginViewModel: LoginViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val sendViewModel: SendViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val context = LocalContext.current
    val showMessage: (String) -> Unit = { msg ->
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    val getString: (Int) -> String = { resId ->
        context.getString(resId)
    }
    val emailSignUpLauncher = rememberEmailSignUpLauncher(
        navController = navController,
        showMessage = showMessage,
        loginViewModel = loginViewModel,
        getString = getString
    )
    val googleSignUpLauncher = rememberGoogleSignUpLauncher(
        navController = navController,
        showMessage = showMessage,
        loginViewModel = loginViewModel,
        getString = getString
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
                viewModel = homeViewModel,
                loginViewModel = loginViewModel,
                onFABClick = { navController.navigate(Send) },
                onFileClick = { file -> navController.navigate(Detail(file.id)) }
            )
        }

        composable<Login> {
            LoginScreen(
                onEmailSignInClick = { emailSignUpLauncher() },
                onGoogleSignInClick = { googleSignUpLauncher() },
                onGuestSignInClick = { navController.navigate(Home) }
            )
        }

        composable<Profile> {
            ProfileScreen(
                viewModel = profileViewModel,
                onLoginScreen = { navController.navigate(Login) },
                onSaveClick = { navController.navigateUp() }
            )
        }

        composable<Send> {
            SendScreen(
                viewModel = sendViewModel,
                onBackClick = { navController.navigateUp() },
                onSaveClick = { navController.navigateUp() }
            )
        }

        composable<Settings> {
            SettingsScreen(
                viewModel = settingsViewModel
            )
        }
    }
}
