package com.arnoagape.polyscribe.data.repository

import android.net.Uri
import android.util.Log
import com.arnoagape.polyscribe.data.service.file.FileApi
import com.arnoagape.polyscribe.domain.model.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that manages file-related operations.
 * Delegates data access to [FileApi] to provide
 * a clean abstraction layer for ViewModels.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class FileRepository @Inject constructor(
    private val fileApi: FileApi,
    private val userRepository: UserRepository
) {

    fun observeFiles(): Flow<List<File>> =
        userRepository.observeUserSession()
            .onEach { session ->
                Log.d(
                    "DEBUG_SESSION",
                    "session = userId=${session.userId}, isGuest=${session.isGuest}"
                )
            }
            .flatMapLatest { session ->
                fileApi.getFilesForUser(
                    userId = session.userId,
                    isAnonymous = session.isGuest
                )
            }

    fun observeFile(fileId: String): Flow<File?> =
        userRepository.observeUserSession()
            .flatMapLatest { session ->
                fileApi.observeFileById(
                    fileId,
                    session.userId,
                    session.isGuest
                )
            }

    suspend fun sendFile(localUris: List<Uri>, file: File): List<String> {
        val session = userRepository.getCurrentSession()
        return fileApi.sendFile(
            localUris,
            file,
            session.userId,
            session.isGuest
        )
    }
}