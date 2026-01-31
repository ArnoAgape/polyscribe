package com.arnoagape.polyscribe.data.service.file

import android.net.Uri
import com.arnoagape.polyscribe.domain.model.File
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining file-related operations.
 * Implementations handle storage, upload and retrieval logic.
 */
interface FileApi {

    fun getFilesForUser(userId: String?, isAnonymous: Boolean): Flow<List<File>>

    fun observeFileById(fileId: String, userId: String?, isAnonymous: Boolean): Flow<File?>

    suspend fun sendFile(
        localUris: List<Uri>,
        file: File,
        userId: String?,
        isAnonymous: Boolean
    ): List<String>
}