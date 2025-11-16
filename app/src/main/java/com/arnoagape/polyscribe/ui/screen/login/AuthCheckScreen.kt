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

@Composable
fun AuthCheckScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val isSignedIn by loginViewModel.isSignedIn.collectAsStateWithLifecycle()

    if (isSignedIn == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    LaunchedEffect(isSignedIn) {
        if (isSignedIn == true) {
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
