package com.arnoagape.polyscribe.ui.screen.login

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onGoogleSignInClick: () -> Unit,
    onGuestSignInClick: () -> Unit,
    onEmailSignInClick: () -> Unit
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
    onEmailSignInClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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
                // 🔹 Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_polyscribe_logo), // ton logo ici
                    contentDescription = "Logo Polyscribe",
                    modifier = Modifier
                        .size(160.dp)
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    stringResource(R.string.sign_in_title),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Email Button
                Button(
                    onClick = { onEmailSignInClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
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

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Separator
                OrSeparator()

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Continue as guest
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

@PreviewLightDark
@Composable
private fun LoginScreenPreview() {
    PolyscribeTheme {
        LoginContent(
            onEmailSignInClick = { },
            onGoogleSignInClick = { },
            onGuestSignInClick = { }
        )
    }
}