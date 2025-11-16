package com.arnoagape.polyscribe.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

@Composable
fun FileRowItem(
    fileName: String,
    icon: ImageVector,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(fileName)
        }

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = null)
        }
    }
}

@Preview
@Composable
fun FileRowItemPreview() {
    PolyscribeTheme {
        FileRowItem(
            fileName = "example.pdf",
            icon = Icons.Default.AttachFile,
            onRemove = {}
        )
    }
}
