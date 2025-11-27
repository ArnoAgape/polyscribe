package com.arnoagape.polyscribe.data.repository

import android.net.Uri
import com.arnoagape.polyscribe.data.service.file.FileApi
import com.arnoagape.polyscribe.domain.model.File
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that manages file-related operations.
 * Delegates data access to [FileApi] to provide
 * a clean abstraction layer for ViewModels.
 */
@Singleton
class FileRepository @Inject constructor(
    private val fileApi: FileApi
) {

    /**
     * Observes all files ordered by descending creation date for a specific user.
     */
    fun filesForUser(userId: String): Flow<List<File>> =
        fileApi.getFilesOrderByUser(userId)

    /**
     * Uploads a new file to Firebase (Storage + Firestore).
     *
     * @throws java.io.IOException when no internet connection is available
     * @throws IllegalArgumentException when file type or size is invalid
     */
    suspend fun sendFile(localUris: List<Uri>, file: File): List<String> =
        fileApi.sendFile(localUris, file)

    /**
     * Observes a single file identified by its ID and user ID.
     */
    fun getFileById(fileId: String, userId: String): Flow<File?> =
        fileApi.getFileById(fileId, userId)
}