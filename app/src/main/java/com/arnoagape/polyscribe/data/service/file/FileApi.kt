package com.arnoagape.polyscribe.data.service.file

import android.net.Uri
import com.arnoagape.polyscribe.domain.model.File
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining file-related operations.
 * Implementations handle storage, upload and retrieval logic.
 */
interface FileApi {

    /** Returns all files ordered by creation date from a specific user. */
    fun getFilesOrderByUser(userId: String): Flow<List<File>>

    /** Uploads a file and returns the list of uploaded URLs. */
    suspend fun sendFile(localUris: List<Uri>, file: File): List<String>

    /** Observes a file by its ID and user ID. */
    fun getFileById(fileId: String, userId: String): Flow<File?>

    /** Uploads a single document to Firebase Storage and returns its URL. */
    suspend fun uploadDocumentToFirebase(uri: Uri): String?
}