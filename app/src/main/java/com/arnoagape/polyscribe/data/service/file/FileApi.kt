package com.arnoagape.polyscribe.data.service.file

import android.net.Uri
import com.arnoagape.polyscribe.domain.model.File
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining file-related operations.
 * Implementations handle storage, upload and retrieval logic.
 */
interface FileApi {

    fun getFilesOrderByUser(userId: String): Flow<List<File>>
    suspend fun sendFile(localUris: List<Uri>, file: File): List<String>
    suspend fun sendFileAsGuest(localUris: List<Uri>, file: File): List<String>
    fun getFileById(fileId: String, userId: String): Flow<File?>
    suspend fun uploadDocumentToFirebase(uri: Uri): String?
    suspend fun uploadDocumentAsGuest(uri: Uri): String?
}