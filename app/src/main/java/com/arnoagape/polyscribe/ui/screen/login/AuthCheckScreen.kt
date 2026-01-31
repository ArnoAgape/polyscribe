package com.arnoagape.polyscribe.ui.screen.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        when (state.isSignedIn) {
            true -> {
                navController.navigate(Home) {
                    popUpTo(AuthCheck) { inclusive = true }
                }
            }
            false -> {
                navController.navigate(Login) {
                    popUpTo(AuthCheck) { inclusive = true }
                }
            }
            null -> Unit
        }
    }

    if (state.isSignedIn == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}