package com.arnoagape.polyscribe.ui.screen.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPlaceholderScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.loading), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoginPreview() {
    PolyscribeTheme {
        LoginPlaceholderScreen()
    }
}