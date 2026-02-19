package com.arnoagape.polyscribe.ui.screen.login

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.domain.model.SessionType
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.screen.send.SendViewModel
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Displays the login screen with multiple sign-in options:
 * email, Google, and guest access.
 *
 * @param viewModel ViewModel providing authentication state and events.
 * @param onLoginSuccess Callback executed after successful sign-in.
 * @param onGoogleSignInClick Launches the Google sign-in flow.
 * @param onEmailSignInClick Launches the email sign-in flow.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    onEmailSignInClick: () -> Unit,
    onLoginSuccess: (SessionType) -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val sendViewModel: SendViewModel = hiltViewModel()

    val state by sendViewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val resources = LocalResources.current

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                Toast.makeText(
                    context,
                    resources.getString(event.message),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> Unit
        }
    }

    val notificationsPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else null

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LoginContent(
                guestName = state.file.guestName,
                onGuestNameChanged = { sendViewModel.onAction(FormEvent.GuestNameChanged(it)) },
                isGuestNameValid = state.file.guestName.isNotBlank(),
                onGoogleSignInClick = { viewModel.onSignInRequested { onGoogleSignInClick() } },
                onGuestSignInClick = {
                    viewModel.onSignInRequested {

                        if (notificationsPermissionState != null &&
                            !notificationsPermissionState.status.isGranted
                        ) {
                            notificationsPermissionState.launchPermissionRequest()
                        }

                        onLoginSuccess(viewModel.loginAsGuest())
                    }
                },
                onEmailSignInClick = { viewModel.onSignInRequested { onEmailSignInClick() } }
            )
        }
    }
}

@Composable
fun LoginContent(
    guestName: String,
    onGuestNameChanged: (String) -> Unit,
    isGuestNameValid: Boolean,
    onGoogleSignInClick: () -> Unit,
    onGuestSignInClick: () -> Unit,
    onEmailSignInClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /** ---------- LOGO POLYSCRIBE ---------- **/
                Image(
                    painter = painterResource(id = R.drawable.ic_polyscribe_logo),
                    contentDescription = "Logo Polyscribe",
                    modifier = Modifier
                        .size(160.dp)
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    stringResource(R.string.sign_in_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                /** ---------- EMAIL BUTTON ---------- **/
                /*Button(
                    onClick = { onEmailSignInClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag(stringResource(R.string.sign_in_email)),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.sign_in_email))
                }

                Spacer(modifier = Modifier.height(10.dp))

                /** ---------- GOOGLE BUTTON ---------- **/
                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag(stringResource(R.string.sign_in_google)),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.sign_in_google))
                }

                Spacer(modifier = Modifier.height(14.dp))

                /** ---------- SEPARATOR ---------- **/
                OrSeparator()

                Spacer(modifier = Modifier.height(14.dp))*/

                /** ---------- NAME FIELD ---------- **/
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = guestName,
                    onValueChange = onGuestNameChanged,
                    label = { Text(stringResource(R.string.hint_first_last_name)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                /** ---------- GUEST BUTTON ---------- **/
                Button(
                    onClick = onGuestSignInClick,
                    enabled = isGuestNameValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .height(50.dp),
                ) {
                    Text(stringResource(R.string.sign_in_guest))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    PolyscribeTheme {
        LoginContent(
            guestName = "John Doe",
            onGuestNameChanged = {},
            isGuestNameValid = false,
            onEmailSignInClick = { },
            onGoogleSignInClick = { },
            onGuestSignInClick = { }
        )
    }
}