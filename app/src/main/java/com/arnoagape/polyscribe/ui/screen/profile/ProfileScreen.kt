package com.arnoagape.polyscribe.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.common.components.ConfirmDialogButton
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

/**
 * Displays the user profile screen with editable account information.
 *
 * @param viewModel ViewModel providing profile data and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    actionLabel = resources.getString(R.string.try_again),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.saveUser()
                }
            }

            is Event.ShowSuccessMessage -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.profile))
                }
            )
        }
    ) { contentPadding ->

        when (state.user) {
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                ProfileContent(
                    contentPadding = contentPadding,
                    userName = state.user?.displayName ?: "",
                    onNameChanged = { viewModel.onAction(FormEvent.DisplayNameChanged(it)) },
                    emailAddress = state.user?.email ?: "",
                    onEmailChanged = { viewModel.onAction(FormEvent.EmailChanged(it)) },
                    onSaveClick = { viewModel.saveUser() },
                    onSignOutClick = { viewModel.signOut() },
                    onDeleteAccountClick = { viewModel.deleteAccount() },
                    isUserFieldsValid = state.isValid,
                    isLoading = false
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    contentPadding: PaddingValues = PaddingValues(),
    userName: String,
    onNameChanged: (String) -> Unit,
    emailAddress: String,
    onEmailChanged: (String) -> Unit,
    onSignOutClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    isUserFieldsValid: Boolean,
    isLoading: Boolean
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        /** ---------- SCROLLABLE FORM CONTENT ---------- **/
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /** ---------- PROFILE IMAGE ---------- **/
            Image(
                painter = painterResource(R.drawable.img_profile_default),
                contentDescription = stringResource(R.string.profile_picture),
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )

            /** ---------- NAME FIELD ---------- **/
            OutlinedTextField(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                value = userName,
                onValueChange = onNameChanged,
                label = { Text(stringResource(id = R.string.user_name)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )

            /** ---------- EMAIL FIELD ---------- **/
            OutlinedTextField(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                value = emailAddress,
                onValueChange = onEmailChanged,
                label = { Text(stringResource(id = R.string.user_email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            /** ---------- SAVE BUTTON ---------- **/
            Button(
                modifier = Modifier.testTag(stringResource(R.string.action_save)),
                onClick = {
                    keyboardController?.hide()
                    onSaveClick()
                },
                enabled = isUserFieldsValid && !isLoading,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(id = R.string.action_save))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            /** ---------- SIGN OUT BUTTON ---------- **/
            ConfirmDialogButton(
                buttonColor = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                onConfirmButton = onSignOutClick,
                actionButton = stringResource(R.string.action_sign_out),
                confirmButtonTitle = stringResource(R.string.action_sign_out),
                confirmButtonMessage = stringResource(R.string.confirm_sign_out_message)
            )

            Spacer(modifier = Modifier.height(16.dp))

            /** ---------- DELETE ACCOUNT BUTTON ---------- **/
            ConfirmDialogButton(
                buttonColor = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                onConfirmButton = onDeleteAccountClick,
                actionButton = stringResource(R.string.action_delete_account),
                confirmButtonTitle = stringResource(R.string.action_delete_account),
                confirmButtonMessage = stringResource(R.string.confirm_delete_account_message)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun ProfileScreenPreview() {
    PolyscribeTheme {
        ProfileContent(
            userName = "Aretha Franklin",
            onNameChanged = { },
            emailAddress = "aretha.franklin@mail.com",
            onEmailChanged = { },
            onSaveClick = { },
            onSignOutClick = { },
            onDeleteAccountClick = { },
            isUserFieldsValid = true,
            isLoading = false
        )
    }
}