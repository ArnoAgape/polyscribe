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

    fun filesForUser(userId: String): Flow<List<File>> = fileApi.getFilesOrderByUser(userId)

    suspend fun sendFile(localUris: List<Uri>, file: File): List<String> =
        fileApi.sendFile(localUris, file)

    fun getFileById(fileId: String, userId: String): Flow<File?> =
        fileApi.getFileById(fileId, userId)
}