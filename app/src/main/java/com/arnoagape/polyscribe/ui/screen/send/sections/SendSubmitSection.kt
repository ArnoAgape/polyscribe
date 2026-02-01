package com.arnoagape.polyscribe.ui.screen.send.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R

@Composable
fun SendSubmitSection(
    isLoading: Boolean,
    onSaveClicked: () -> Unit,
    isFileValid: Boolean
) {
    Surface(
        tonalElevation = 4.dp
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .navigationBarsPadding()
                .testTag(stringResource(R.string.action_send)),
            onClick = onSaveClicked,
            enabled = isFileValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(id = R.string.action_send))
            }
        }
    }
}