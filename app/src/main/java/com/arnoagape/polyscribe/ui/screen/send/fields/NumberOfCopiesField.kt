package com.arnoagape.polyscribe.ui.screen.send.fields

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R

@Composable
fun NumberOfCopiesField(
    numberOfCopies: Int,
    onNumberOfCopiesChange: (Int) -> Unit
) {
    var isDialogOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current

    /** ---------- SEMANTICS ---------- **/

    LaunchedEffect(numberOfCopies) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE)
                as android.view.accessibility.AccessibilityManager

        if (am.isEnabled) {
            val event = android.view.accessibility.AccessibilityEvent
                .obtain(android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT).apply {
                    text.add("$numberOfCopies ${context.getString(R.string.copies)}")
                }
            am.sendAccessibilityEvent(event)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(5.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(id = R.string.hint_number_of_copies))

        IconButton(
            onClick = {
                val newValue = numberOfCopies - 1
                onNumberOfCopiesChange(newValue)
            },
            enabled = numberOfCopies > 1,
            modifier = Modifier
                .semantics {
                    contentDescription = context.getString(R.string.contentDescription_remove_copy)
                }
        ) {
            Text(
                text = "-", style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.clearAndSetSemantics { })
        }

        Text(
            numberOfCopies.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clickable { isDialogOpen = true }
                .clearAndSetSemantics { }
        )

        IconButton(
            onClick = { onNumberOfCopiesChange(numberOfCopies + 1) },
            modifier = Modifier
                .semantics {
                    contentDescription = context.getString(R.string.contentDescription_add_copy)
                }
        ) {
            Text(
                "+", style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.clearAndSetSemantics { })
        }
    }

    if (isDialogOpen) {
        NumberPickerDialog(
            initialValue = numberOfCopies,
            onConfirm = { typedValue ->
                onNumberOfCopiesChange(typedValue)
                isDialogOpen = false
            },
            onDismiss = { isDialogOpen = false }
        )
    }
}

@Composable
fun NumberPickerDialog(
    initialValue: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(initialValue.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.hint_number_of_copies)) },
        text = {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { value ->
                    inputValue = value.filter { it.isDigit() }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalValue = inputValue.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onConfirm(finalValue)
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
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}