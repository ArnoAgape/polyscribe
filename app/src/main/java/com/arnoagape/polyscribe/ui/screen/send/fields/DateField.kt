package com.arnoagape.polyscribe.ui.screen.send.fields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    modifier: Modifier,
    value: Instant,
    onValueChange: (Instant) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val currentLocalDate = remember(value) {
        value.atZone(ZoneId.systemDefault()).toLocalDate()
    }

    PickerField(
        modifier = modifier,
        label = label,
        value = currentLocalDate.format(formatter),
        icon = Icons.Default.DateRange,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        val initialMillis = remember(currentLocalDate) {
            currentLocalDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        }

        val state = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->

                            // Convert millis -> LocalDate
                            val pickedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()

                            // Convert LocalDate -> Instant (00:00)
                            val pickedInstant = pickedLocalDate
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()

                            onValueChange(pickedInstant)
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}