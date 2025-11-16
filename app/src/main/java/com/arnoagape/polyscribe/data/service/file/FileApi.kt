package com.arnoagape.polyscribe.data.service.file

import android.net.Uri
import com.arnoagape.polyscribe.domain.model.File
import kotlinx.coroutines.flow.Flow

interface FileApi {

    fun getFilesOrderByCreationDateDesc(): Flow<List<File>>

    suspend fun sendFile(file: File)

    fun getFileById(fileId: String): Flow<File?>

    suspend fun uploadDocumentToFirebase(uri: Uri): String?

    suspend fun uploadImageToFirebase(uri: Uri): String?
}