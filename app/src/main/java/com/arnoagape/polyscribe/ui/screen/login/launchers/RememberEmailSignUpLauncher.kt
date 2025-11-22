package com.arnoagape.polyscribe.ui.screen.login.launchers

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavHostController
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.navigation.Home
import com.arnoagape.polyscribe.ui.screen.login.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

@Composable
fun rememberEmailSignUpLauncher(
    navController: NavHostController,
    showMessage: (String) -> Unit,
    getString: (Int) -> String,
    loginViewModel: LoginViewModel
): () -> Unit {

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentShowMessage by rememberUpdatedState(showMessage)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = IdpResponse.fromResultIntent(result.data)

        if (result.resultCode == Activity.RESULT_OK) {
            val user = firebaseAuth.currentUser

            loginViewModel.syncUserWithFirestore()
            currentShowMessage(getString(R.string.success_sign_up))

            navController.navigate(Home) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            if (response == null) {
                currentShowMessage(getString(R.string.error_sign_up))
            } else {
                currentShowMessage("Google error: ${response.error?.errorCode}")
            }
        }
    }

    val signUpIntent = remember {
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.drawable.ic_polyscribe_logo)
            .setTheme(R.style.Theme_Polyscribe)
            .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
            .build()
    }

    return {
        launcher.launch(signUpIntent)
    }
}