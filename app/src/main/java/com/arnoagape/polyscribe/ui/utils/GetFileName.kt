package com.arnoagape.polyscribe.ui.utils

import android.content.Context
import android.net.Uri

fun Context.getFileName(uri: Uri?): String {
    if (uri != null) {
        val cursor = contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )
        cursor?.use {
            val nameIndex = cursor.getColumnIndex("_display_name")
            if (nameIndex != -1 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return uri?.lastPathSegment ?: "Fichier"
}
