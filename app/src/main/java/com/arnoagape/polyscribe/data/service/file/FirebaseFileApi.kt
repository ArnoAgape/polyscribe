package com.arnoagape.polyscribe.data.service.file

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import com.arnoagape.polyscribe.data.dto.FileDto
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Firebase implementation of [FileApi].
 * Handles file uploads to Firebase Storage and metadata persistence in Firestore.
 */
class FirebaseFileApi @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils
) : FileApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val filesCollection = firestore.collection("files")
    private val guestCollection = firestore.collection("guest")

    /**
     * Retrieves all files ordered by creation date (descending) from a specific user
     * and maps Firestore DTOs to domain models.
     */
    override fun getFilesOrderByUser(userId: String): Flow<List<File>> {
        return filesCollection
            .whereEqualTo("author.id", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<FileDto>()
            .map { list -> list.map { File.fromDto(it) } }
            .catch { emit(emptyList()) }
    }

    /**
     * Uploads multiple URIs, stores their URLs in Firestore,
     * and returns the list of generated URLs.
     *
     * @throws IOException when the device is offline
     */
    override suspend fun sendFile(localUris: List<Uri>, file: File): List<String> {
        if (!networkUtils.isNetworkAvailable()) {
            throw IOException("No internet connection")
        }
        try {
            val uploadedFiles = localUris.mapNotNull { uri ->
                uploadDocumentToFirebase(uri)
            }

            val updated = file.copy(fileUrl = uploadedFiles)
            filesCollection.document(updated.id).set(updated.toDto()).await()
            return uploadedFiles

        } catch (e: Exception) {
            Log.e("FirebaseFileApi", "Error while adding document", e)
            throw e
        }
    }

    override suspend fun sendFileAsGuest(localUris: List<Uri>, file: File): List<String> {

        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }

        if (!networkUtils.isNetworkAvailable()) {
            throw IOException("No internet connection")
        }

        val uploadedFiles = localUris.map { uri ->
            uploadDocumentAsGuest(uri)
        }

        val updated = file.copy(fileUrl = uploadedFiles)
        guestCollection.document(updated.id)
            .set(updated.guestToDto())
            .await()

        return uploadedFiles
    }

    /**
     * Observes a single file by ID and userId.
     */
    override fun getFileById(fileId: String, userId: String): Flow<File?> {
        return filesCollection
            .document(fileId)
            .dataObjects<FileDto>()
            .map { dto ->
                if (dto?.author?.id == userId) File.fromDto(dto) else null
            }
    }

    /**
     * Uploads a document to Firebase Storage.
     * Validates MIME type and returns the public download URL.
     */
    override suspend fun uploadDocumentToFirebase(uri: Uri): String? {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            var pfd: ParcelFileDescriptor? = null

            try {
                val mimeType = context.contentResolver.getType(uri)
                pfd = context.contentResolver.openFileDescriptor(uri, "r")

                val allowedMimeTypes = listOf(
                    "image/jpeg", "image/png", "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.oasis.opendocument.text",
                    "text/plain"
                )

                if (mimeType !in allowedMimeTypes) {
                    throw IllegalArgumentException("Unsupported file type.")
                }

                val extension =
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                val fileName = "${System.currentTimeMillis()}.$extension"

                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("files/$fileName")

                storageRef.putFile(uri).await()
                return@withContext storageRef.downloadUrl.await().toString()

            } catch (e: Exception) {
                Log.e("FirebaseUpload", "Error while uploading", e)
                null
            } finally {
                pfd?.close()
            }
        }
    }

    override suspend fun uploadDocumentAsGuest(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null

            try {
                val mimeType = context.contentResolver.getType(uri)
                pfd = context.contentResolver.openFileDescriptor(uri, "r")

                val allowedMimeTypes = listOf(
                    "image/jpeg", "image/png", "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.oasis.opendocument.text",
                    "text/plain"
                )

                if (mimeType !in allowedMimeTypes) {
                    throw IllegalArgumentException("Unsupported file type: $mimeType")
                }

                val extension =
                    MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

                val fileName = "${System.currentTimeMillis()}.$extension"

                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("guest/$fileName")

                storageRef.putFile(uri).await()
                return@withContext storageRef.downloadUrl.await().toString()

            } catch (e: Exception) {
                Log.e("FirebaseGuestUpload", "Error while uploading", e)
                throw e
            } finally {
                pfd?.close()
            }
        }
    }
}