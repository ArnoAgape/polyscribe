package com.arnoagape.polyscribe.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.arnoagape.polyscribe.R

@Composable
fun ImageFilePreview(
    modifier: Modifier = Modifier,
    documentUrl: String?,
    baseUrl: String,
    isImage: Boolean,
    isDetailScreen: Boolean,
    onClick: (() -> Unit)? = null
) {
    var selectedImage by remember { mutableStateOf<String?>(null) }

    val effectiveModifier = if (!isDetailScreen && onClick != null) {
        modifier.clickable { onClick() }
    } else modifier

    when {
        isImage -> {
            AsyncImage(
                modifier = effectiveModifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .aspectRatio(16 / 9f)
                    .let {
                        if (isDetailScreen) it.clickable { selectedImage = documentUrl }
                        else it
                    },
                model = documentUrl,
                placeholder = ColorPainter(Color.DarkGray),
                contentDescription = stringResource(R.string.contentDescription_file_preview),
                contentScale = ContentScale.Crop
            )
            if (isDetailScreen && selectedImage != null) {
                Dialog(
                    onDismissRequest = { selectedImage = null }, // closes when click
                    properties = DialogProperties(usePlatformDefaultWidth = false) // full screen
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clickable { selectedImage = null }, // closes when click
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedImage,
                            contentDescription = stringResource(R.string.contentDescription_file_preview),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        baseUrl.endsWith(".pdf", true) -> {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "PDF",
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }

        else -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = "Document",
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}