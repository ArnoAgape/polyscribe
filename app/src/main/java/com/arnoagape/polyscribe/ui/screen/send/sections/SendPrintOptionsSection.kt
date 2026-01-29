package com.arnoagape.polyscribe.ui.screen.send.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.components.TextRowItem
import com.arnoagape.polyscribe.ui.screen.send.SendDimens
import com.arnoagape.polyscribe.ui.screen.send.fields.NumberOfCopiesField

@Composable
fun SendPrintOptionsSection(
    colored: Boolean,
    onColorationChange: (Boolean) -> Unit,
    doubleSided: Boolean,
    onDoubleSidedChange: (Boolean) -> Unit,
    numberOfCopies: Int,
    onNumberOfCopiesChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SendDimens.ItemSpacing)) {

        TextRowItem(
            text = stringResource(R.string.hint_color),
            trailingContent = {
                Switch(
                    checked = colored,
                    onCheckedChange = onColorationChange
                )
            }
        )

        TextRowItem(
            text = stringResource(R.string.hint_double_sided),
            trailingContent = {
                Switch(
                    checked = doubleSided,
                    onCheckedChange = onDoubleSidedChange
                )
            }
        )

        NumberOfCopiesField(
            numberOfCopies = numberOfCopies,
            onNumberOfCopiesChange = onNumberOfCopiesChange
        )
    }
}
