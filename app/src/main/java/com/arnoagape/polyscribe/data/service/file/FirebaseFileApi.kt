package com.arnoagape.polyscribe.data.service.file

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class FirebaseFileApi @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils
) : FileApi {

    private val firestore = FirebaseFirestore.getInstance()
    private val filesCollection = firestore.collection("files")

    override fun getFilesOrderByCreationDateDesc(): Flow<List<File>> {
        return filesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .dataObjects()
    }

    override suspend fun sendFile(file: File) {
        if (!networkUtils.isNetworkAvailable()) {
            throw IOException("No internet connection")
        }
        try {
            val uploadedFiles = file.fileUrl.mapNotNull { uriString ->
                val uri = uriString.toUri()
                uploadDocumentToFirebase(uri)
            }

            val uploadedPhotos = file.pictureUrl.mapNotNull { uriString ->
                val uri = uriString.toUri()
                uploadDocumentToFirebase(uri)
            }

            val updated = file.copy(
                fileUrl = uploadedFiles,
                pictureUrl = uploadedPhotos
            )

            filesCollection.document(updated.id).set(updated).await()

        } catch (e: Exception) {
            Log.e("FirebaseFileApi", "Error while adding document", e)
            throw e
        }
    }

    override fun getFileById(fileId: String): Flow<File?> {
        return filesCollection
            .whereEqualTo("id", fileId)
            .limit(1)
            .dataObjects<File>()
            .map { it.firstOrNull() }
    }

    override suspend fun uploadDocumentToFirebase(uri: Uri): String? {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            var pfd: ParcelFileDescriptor? = null
            try {
                val storage = FirebaseStorage.getInstance().reference

                // --- 1️⃣ Detects the MIME type of the file ---
                val mimeType = context.contentResolver.getType(uri)
                pfd = context.contentResolver.openFileDescriptor(uri, "r")

                // --- 2️⃣ Checks the maximum file size ---
                val fileSize = pfd?.statSize ?: 0L
                val maxSizeBytes = 20 * 1024 * 1024 // 20 Mb

                if (fileSize > maxSizeBytes) {
                    Log.w("FirebaseUpload", "File too large: ${fileSize / 1024 / 1024} Mo")
                    throw IllegalArgumentException("File exceeds the maximum size of 20 Mb")
                }

                // --- 3️⃣ Checks the allowed MIME type ---
                val allowedMimeTypes = listOf(
                    "image/jpeg",
                    "image/png",
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.oasis.opendocument.text"
                )

                if (mimeType !in allowedMimeTypes) {
                    Log.w("FirebaseUpload", "Unsupported MIME type: $mimeType")
                    throw IllegalArgumentException("Unsupported file type.")
                }

                // --- 4️⃣ Infers the file extension ---
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                val fileName = "${System.currentTimeMillis()}.$extension"
                val fileRef = storage.child("uploads/$fileName")

                // --- 5️⃣ File upload ---
                fileRef.putFile(uri).await()

                // --- 6️⃣ Collects the download URL ---
                fileRef.downloadUrl.await().toString()

            } catch (e: IllegalArgumentException) {
                Log.w("FirebaseUpload", "Upload aborted: ${e.message}")
                throw e
            } catch (e: Exception) {
                Log.e("FirebaseUpload", "Error while uploading", e)
                null
            } finally {
                pfd?.close()
            }
        }
    }
}