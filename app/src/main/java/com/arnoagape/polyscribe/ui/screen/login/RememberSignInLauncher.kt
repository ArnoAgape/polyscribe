package com.arnoagape.polyscribe.ui.screen.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavHostController
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.navigation.Home
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.arnoagape.polyscribe.ui.screen.profile.ProfileViewModel

/**
 * Remembers and returns a lambda function that launches the Firebase Authentication
 * sign-in flow using FirebaseUI.
 */
@Composable
fun rememberSignInLauncher(
    navController: NavHostController,
    showMessage: (String) -> Unit,
    profileViewModel: ProfileViewModel
): () -> Unit {

    fun handleSignInFailure(
        response: IdpResponse?,
        showMessage: (String) -> Unit
    ) {
        if (response == null) {
            Log.w("Auth", "Connection canceled by user.")
            showMessage("Connection canceled.")
        } else {
            val errorCode = response.error?.errorCode
            Log.w("Auth", "Connection failed. Error code: $errorCode", response.error)
            showMessage("Connection failed. Please try again.")
        }
    }

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentShowMessage by rememberUpdatedState(showMessage)

    val signInLauncher: ActivityResultLauncher<Intent> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()

    ) { result ->
        val response = IdpResponse.fromResultIntent(result.data)

        if (result.resultCode == Activity.RESULT_OK) {
            val user = firebaseAuth.currentUser
            Log.d("Auth", "Successfully connected: ${user?.email ?: "Unknown user"}")

            profileViewModel.syncUserWithFirestore()
            currentShowMessage("Connection successful")

            navController.navigate(Home) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else {
            handleSignInFailure(response, showMessage)
        }
    }

    val providers = remember {
        listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
    }

    val signInIntent = remember(providers) {
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.drawable.ic_polyscribe_logo)
            .setTheme(R.style.Theme_Polyscribe)
            .setAvailableProviders(providers)
            .build()
    }

    return {
        signInLauncher.launch(signInIntent)
    }
}