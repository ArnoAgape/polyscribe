package com.arnoagape.polyscribe.data.repository

import com.arnoagape.polyscribe.data.service.file.FileApi
import com.arnoagape.polyscribe.domain.model.File
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for managing file-related operations.
 * It delegates data access to the [FileApi] (FirebaseFileApi implementation),
 * providing a clean abstraction layer for ViewModels.
 */
@Singleton
class FileRepository @Inject constructor(
    private val fileApi: FileApi
) {

    /**
     * Observes the list of all files ordered by descending creation date.
     */
    val files: Flow<List<File>> = fileApi.getFilesOrderByCreationDateDesc()

    /**
     * Uploads a new file to Firebase (Storage + Firestore).
     * @throws java.io.IOException if there is no internet connection
     * @throws IllegalArgumentException if the file type or size is invalid
     */
    suspend fun sendFile(file: File) = fileApi.sendFile(file)

    /**
     * Observes a single file by its unique ID.
     */
    fun getFileById(fileId: String): Flow<File?> =
        fileApi.getFileById(fileId)
}