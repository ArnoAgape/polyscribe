package com.arnoagape.polyscribe.ui.screen.send.sections

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.ui.common.components.FileRowItem
import com.arnoagape.polyscribe.ui.utils.getFileName

@Composable
fun SendFilesSection(
    uris: List<Uri>,
    onRemoveFile: (Uri) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uris.forEach { uri ->
            FileRowItem(
                fileName = context.getFileName(uri),
                icon = Icons.Default.AttachFile,
                onRemove = { onRemoveFile(uri) }
            )
        }
    }
}