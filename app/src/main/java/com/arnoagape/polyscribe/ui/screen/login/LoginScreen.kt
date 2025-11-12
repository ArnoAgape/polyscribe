package com.arnoagape.polyscribe.ui.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {}
    }
}

@Composable
private fun LoginContent(
    onGoogleSignInClick: () -> Unit,
    onEmailSignInClick: (String, String) -> Unit,
    showMessage: (String) -> Unit
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 🔹 Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_polyscribe_logo), // ton logo ici
                    contentDescription = "Logo Polyscribe",
                    modifier = Modifier
                        .size(160.dp)
                        .padding(bottom = 16.dp)
                )

                // 🔹 Champ email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

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

                // 🔹 Email Button
                Button(
                    onClick = { onEmailSignInClick(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(stringResource(R.string.sign_in))
                }

                // 🔹 Separator
                Text(stringResource(R.string.or), style = MaterialTheme.typography.labelMedium)

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
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.sign_in_google))
                }

                // 🔹 Option inscription
                TextButton(
                    onClick = {
                        showMessage("Fonctionnalité d’inscription à venir")
                    }
                ) {
                    Text(stringResource(R.string.sign_up))
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
            showMessage = { },
        )
    }
}