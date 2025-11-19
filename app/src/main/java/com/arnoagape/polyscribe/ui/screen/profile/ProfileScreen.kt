package com.arnoagape.polyscribe.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.components.ConfirmDialogButton
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoginScreen: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (user) {
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
                        modifier = Modifier.fillMaxWidth(),
                        userName = user?.displayName ?: "",
                        onNameChanged = { },
                        emailAddress = user?.email ?: "",
                        onEmailChanged = { },
                        onSignOutClick = {
                            viewModel.signOut()
                            onLoginScreen()
                        },
                        onDeleteAccountClick = { viewModel.deleteAccount() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    userName: String,
    onNameChanged: (String) -> Unit,
    emailAddress: String,
    onEmailChanged: (String) -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.img_profile_default),
                contentDescription = stringResource(R.string.profile_picture),
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    value = userName,
                    onValueChange = { onNameChanged(it) },
                    label = { Text(stringResource(id = R.string.user_name)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    value = emailAddress,
                    onValueChange = { onEmailChanged(it) },
                    label = { Text(stringResource(id = R.string.user_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
            }

            /** ---------- SIGN OUT BUTTON ---------- **/
            ConfirmDialogButton(
                buttonColor = ButtonDefaults.buttonColors(),
                onConfirmButton = onSignOutClick,
                actionButton = R.string.action_sign_out,
                confirmButtonTitle = R.string.action_sign_out,
                confirmButtonMessage = R.string.action_sign_out,
                okButtonMessage = R.string.action_continue
            )

            Spacer(modifier = Modifier.height(16.dp))

            /** ---------- DELETE ACCOUNT BUTTON ---------- **/
            ConfirmDialogButton(
                buttonColor = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onConfirmButton = onDeleteAccountClick,
                actionButton = R.string.action_delete_account,
                confirmButtonTitle = R.string.action_delete_account,
                confirmButtonMessage = R.string.action_delete_account,
                okButtonMessage = R.string.action_continue
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
            onSignOutClick = { },
            onDeleteAccountClick = { }
        )
    }
}