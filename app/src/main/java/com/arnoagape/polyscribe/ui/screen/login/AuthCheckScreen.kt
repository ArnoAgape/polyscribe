package com.arnoagape.polyscribe.ui.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.arnoagape.polyscribe.navigation.AuthCheck
import com.arnoagape.polyscribe.navigation.Home
import com.arnoagape.polyscribe.navigation.Login

/**
 * Checks whether the user is signed in and navigates accordingly.
 * Redirects signed-in users to [Home] and others to [Login].
 *
 * @param navController Navigation controller used for redirection.
 * @param viewModel Injected ViewModel providing sign-in state.
 */
@Composable
fun AuthCheckScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn == null) {
            navController.navigate(Home) {
                popUpTo(AuthCheck) { inclusive = true }
            }
        } else {
            navController.navigate(Login) {
                popUpTo(AuthCheck) { inclusive = true }
            }
        }
    }
}
