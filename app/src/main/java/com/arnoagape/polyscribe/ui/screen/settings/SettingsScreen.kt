package com.arnoagape.polyscribe.ui.screen.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.components.TextRowItem
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    val notificationsPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else null

    LaunchedEffect(notificationsPermissionState?.status?.isGranted) {
        if (notificationsPermissionState?.status?.isGranted == true &&
            !notificationsEnabled
        ) {
            viewModel.toggleNotifications(true)
        }
    }

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
        SettingsContent(
            contentPadding = contentPadding,
            notificationsEnabled = notificationsEnabled,
            onNotificationEnabledClicked = { enabled ->

                if (notificationsPermissionState != null) {

                    if (notificationsPermissionState.status.isGranted) {
                        viewModel.toggleNotifications(enabled)

                    } else {
                        notificationsPermissionState.launchPermissionRequest()
                    }

                } else {
                    // Android < 13
                    viewModel.toggleNotifications(enabled)
                }
            }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SettingsContent(
    contentPadding: PaddingValues = PaddingValues(),
    notificationsEnabled: Boolean,
    onNotificationEnabledClicked: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /** ---------- SCROLLABLE FORM CONTENT ---------- **/
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /** ---------- NOTIFICATIONS ---------- **/
                TextRowItem(
                    textRes = R.string.notifications,
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationEnabledClicked
                        )
                    }
                )
            }
        }
    }
}


@PreviewLightDark
@Composable
private fun SettingsPreview() {
    PolyscribeTheme {
        SettingsContent(
            onNotificationEnabledClicked = { },
            notificationsEnabled = false
        )
    }
}