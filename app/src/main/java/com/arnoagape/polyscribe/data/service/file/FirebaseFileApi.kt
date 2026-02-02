package com.arnoagape.polyscribe.data.service.file

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import com.arnoagape.polyscribe.data.dto.AuthorSnapshot
import com.arnoagape.polyscribe.data.dto.FileDto
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Firebase implementation of [FileApi].
 * Handles file uploads to Firebase Storage and metadata persistence in Firestore.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseFileApi @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils
) : FileApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val filesCollection = firestore.collection("files")

    /**
     * Retrieves all files ordered by creation date (descending) from a specific user
     * and maps Firestore DTOs to domain models.
     */

    override fun getFilesForUser(userId: String?, isAnonymous: Boolean): Flow<List<File>> {

        Log.d(
            "DEBUG_FILES",
            "getFilesForUser uid=$userId anon=$isAnonymous"
        )

        if (userId == null) return flowOf(emptyList())

        val query =
            if (isAnonymous) {
                filesCollection.whereEqualTo("guestId", userId)
            } else {
                filesCollection.whereEqualTo("ownerId", userId)
            }

        return query
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<FileDto>()
            .map { it.map(File::fromDto) }
            .catch { emit(emptyList()) }
    }

    override fun observeFileById(
        fileId: String,
        userId: String?,
        isAnonymous: Boolean
    ): Flow<File?> {

        if (userId == null) return flowOf(null)

        val query =
            if (isAnonymous) {
                filesCollection.whereEqualTo("guestId", userId)
            } else {
                filesCollection.whereEqualTo("ownerId", userId)
            }

        return query
            .whereEqualTo("id", fileId)
            .limit(1)
            .dataObjects<FileDto>()
            .map { list -> list.firstOrNull()?.let(File::fromDto) }
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

        val firebaseUser =
            auth.currentUser
                ?: auth.signInAnonymously().await().user
                ?: throw IllegalStateException("Unable to get Firebase user")

        val uploadedFiles = localUris.mapNotNull { uri ->
            uploadDocumentToFirebase(uri, firebaseUser)
        }

        val authorSnapshot =
            if (!firebaseUser.isAnonymous) {
                AuthorSnapshot(
                    displayName = firebaseUser.displayName,
                    email = firebaseUser.email,
                    phoneNumber = firebaseUser.phoneNumber
                )
            } else null

        val updated = file.copy(
            fileUrl = uploadedFiles,
            ownerId = if (!firebaseUser.isAnonymous) firebaseUser.uid else null,
            guestId = if (firebaseUser.isAnonymous) firebaseUser.uid else null,
            author = authorSnapshot
        )

        filesCollection
            .document(updated.id)
            .set(updated.toDto())
            .await()

        return uploadedFiles
    }

    /**
     * Uploads a document to Firebase Storage.
     * Validates MIME type and returns the public download URL.
     */
    private suspend fun uploadDocumentToFirebase(uri: Uri, firebaseUser: FirebaseUser): String? {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            var pfd: ParcelFileDescriptor? = null

            val rawName =
                firebaseUser.displayName
                    ?.takeIf { it.isNotBlank() }
                    ?: firebaseUser.email?.takeIf { it.isNotBlank() }
                    ?: "invite"
            val safeUserName = rawName.safeFileName()
            val timestamp = System.currentTimeMillis()

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
                val fileName = "${safeUserName}_${timestamp}.$extension"

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

    private suspend fun uploadDocumentAsGuest(uri: Uri): String {
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

    private fun String.safeFileName(): String =
        lowercase()
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-z0-9_-]".toRegex(), "")
}