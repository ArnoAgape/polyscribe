import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeField(
    modifier: Modifier = Modifier,
    value: Instant,
    onValueChange: (Instant) -> Unit,
    label: String
) {
    val zone = ZoneId.systemDefault()

    // Convert Instant -> LocalDate + LocalTime
    val currentDate = value.atZone(zone).toLocalDate()
    val currentTime = value.atZone(zone).toLocalTime()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var tempPickedDate by remember { mutableStateOf<LocalDate?>(null) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val display = value.atZone(zone).format(formatter)

    PickerField(
        modifier = modifier,
        label = label,
        value = display,
        icon = Icons.Default.DateRange,
        onClick = { showDatePicker = true }
    )

    // --- DatePicker ---
    if (showDatePicker) {
        val todayUtcMillis = LocalDate.now()
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        val initialMillis = currentDate
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayUtcMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val utcMillis = dateState.selectedDateMillis
                        if (utcMillis != null) {

                            tempPickedDate = Instant.ofEpochMilli(utcMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            showDatePicker = false
                            showTimePicker = true
                        } else showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    // --- TimePicker ---
    if (showTimePicker) {

        val timeState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute
        )

        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pickedDate = tempPickedDate ?: currentDate
                        val pickedTime = LocalTime.of(timeState.hour, timeState.minute)

                        val combinedInstant = ZonedDateTime.of(
                            pickedDate,
                            pickedTime,
                            zone
                        ).toInstant()

                        onValueChange(combinedInstant)
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {}
        ) {
            TimePicker(state = timeState)
        }
    }
}