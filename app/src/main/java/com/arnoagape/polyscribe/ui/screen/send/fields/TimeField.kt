package com.arnoagape.polyscribe.ui.screen.send.fields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.components.PickerField
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    modifier: Modifier,
    value: Instant,
    onValueChange: (Instant) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val currentLocalTime: LocalTime = remember(value) {
        value.atZone(ZoneId.systemDefault()).toLocalTime()
    }

    val state = rememberTimePickerState(
        initialHour = currentLocalTime.hour,
        initialMinute = currentLocalTime.minute
    )

    PickerField(
        modifier = modifier,
        label = label,
        value = currentLocalTime.format(formatter),
        icon = Icons.Default.AccessTime,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        TimePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = LocalTime.of(state.hour, state.minute)
                        val pickedInstant = picked
                            .atDate(LocalDate.now())
                            .atZone(ZoneId.systemDefault())
                            .toInstant()

                        onValueChange(pickedInstant)
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { }
        ) {
            TimePicker(state = state)
        }
    }
}