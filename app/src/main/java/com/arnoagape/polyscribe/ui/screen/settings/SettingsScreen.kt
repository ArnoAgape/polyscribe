package com.arnoagape.polyscribe.ui.screen.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.settings))
                }
            )
        }
    ) { contentPadding ->
        Settings(
            modifier = Modifier.padding(contentPadding),
            notificationsEnabled = notificationsEnabled,
            onNotificationEnabledClicked = {
                viewModel.toggleNotifications(!notificationsEnabled)
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Settings(
    modifier: Modifier = Modifier,
    notificationsEnabled: Boolean,
    onNotificationEnabledClicked: () -> Unit
) {
    val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (notificationsPermissionState?.status?.isGranted == false) {
                        notificationsPermissionState.launchPermissionRequest()
                        return@Button
                    }
                }
                onNotificationEnabledClicked()
            }
        ) {
            if (notificationsEnabled) {
                Text(text = stringResource(id = R.string.no_network))
            }
            else {
                Text(text = stringResource(id = R.string.no_network))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsPreview() {
    PolyscribeTheme {
        Settings(
            onNotificationEnabledClicked = { },
            notificationsEnabled = false
        )
    }
}