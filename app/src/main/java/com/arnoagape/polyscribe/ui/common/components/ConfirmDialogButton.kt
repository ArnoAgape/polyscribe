package com.arnoagape.polyscribe.ui.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R

@Composable
fun ConfirmDialogButton(
    buttonColor: ButtonColors,
    onConfirmButton: () -> Unit,
    actionButton: Int,
    confirmButtonTitle: Int,
    confirmButtonMessage: Int
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = buttonColor
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(actionButton)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text(text = stringResource(confirmButtonTitle))
            },

            text = {
                Text(text = stringResource(confirmButtonMessage))
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onConfirmButton()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("OK")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
