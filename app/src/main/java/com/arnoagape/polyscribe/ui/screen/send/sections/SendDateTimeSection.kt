package com.arnoagape.polyscribe.ui.screen.send.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.screen.send.fields.DateTimeField
import java.time.Instant

@Composable
fun SendDateTimeSection(
    dateTime: Instant,
    onDateTimeChange: (Instant) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        DateTimeField(
            modifier = Modifier.fillMaxWidth(),
            value = dateTime,
            onValueChange = onDateTimeChange,
            label = stringResource(R.string.hint_datetime)
        )
    }
}