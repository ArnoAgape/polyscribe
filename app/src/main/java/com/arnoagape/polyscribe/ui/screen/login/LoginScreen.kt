package com.arnoagape.polyscribe.ui.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.Event.ShowSnackBar
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onGoogleSignInClick: () -> Unit,
    onGuestSignInClick: () -> Unit,
    onEmailSignInClick: (String, String) -> Unit
) {

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LoginContent(
                onGoogleSignInClick = onGoogleSignInClick,
                onGuestSignInClick = onGuestSignInClick,
                onEmailSignInClick = onEmailSignInClick
            )
        }
    }
}

@Composable
private fun LoginContent(
    onGoogleSignInClick: () -> Unit,
    onGuestSignInClick: () -> Unit,
    onEmailSignInClick: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_polyscribe_logo), // ton logo ici
                    contentDescription = "Logo Polyscribe",
                    modifier = Modifier
                        .size(160.dp)
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 🔹 Champ email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 🔹 Champ mot de passe
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Email Button
                Button(
                    onClick = { onEmailSignInClick(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(stringResource(R.string.sign_in))
                }

                Spacer(modifier = Modifier.height(50.dp))

                // 🔹 Google Button
                OutlinedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Separator
                Text(stringResource(R.string.or), style = MaterialTheme.typography.labelMedium)

                Spacer(modifier = Modifier.height(14.dp))

                // 🔹 Option inscription
                TextButton(
                    onClick = {
                        ShowSnackBar(R.string.feature_coming_soon)
                    }
                ) {
                    OutlinedButton(
                        onClick = onGuestSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text(stringResource(R.string.sign_in_guest))
                    }
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
            onEmailSignInClick = { _, _ -> },
            onGoogleSignInClick = { },
            onGuestSignInClick = { },
        )
    }
}