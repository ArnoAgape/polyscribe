package com.arnoagape.polyscribe.ui.screen.send.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R

@Composable
fun SendActionsSection(
    onAddFileClick: () -> Unit,
    onAddPictureClick: () -> Unit,
) {
    /** ---------- CONTENT_DESCRIPTION ---------- **/

    val contentDescriptionAddFile = stringResource(R.string.contentDescription_add_file)
    val contentDescriptionAddPicture = stringResource(R.string.contentDescription_add_picture)

    /** ---------- CONTENT_DESCRIPTION ---------- **/
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = Modifier
                .weight(1f)
                .testTag(stringResource(R.string.add_file)),
            onClick = onAddFileClick
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_file),
                maxLines = 1,
                modifier = Modifier.semantics {
                    contentDescription = contentDescriptionAddFile
                }
            )
        }

        Button(
            modifier = Modifier
                .weight(1f)
                .testTag(stringResource(R.string.add_picture)),
            onClick = onAddPictureClick
        ) {
            Icon(Icons.Default.Photo, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.add_picture),
                maxLines = 1,
                modifier = Modifier.semantics {
                    contentDescription = contentDescriptionAddPicture
                }
            )
        }
    }
}