package com.arnoagape.polyscribe.data.service.file

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import com.arnoagape.polyscribe.data.dto.FileDto
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
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .dataObjects<FileDto>()
            .map { list -> list.map { File.fromDto(it) } }
    }

    override suspend fun sendFile(localUris: List<Uri>, file: File): List<String> {
        if (!networkUtils.isNetworkAvailable()) {
            throw IOException("No internet connection")
        }
        try {
            val uploadedFiles = localUris.mapNotNull { uri ->
                uploadDocumentToFirebase(uri)
            }

            val updated = file.copy(
                fileUrl = uploadedFiles
            )
            filesCollection.document(updated.id).set(updated.toDto()).await()

            return uploadedFiles

        } catch (e: Exception) {
            Log.e("FirebaseFileApi", "Error while adding document", e)
            throw e
        }
    }

    override fun getFileById(fileId: String): Flow<File?> {
        return filesCollection
            .whereEqualTo("id", fileId)
            .limit(1)
            .dataObjects<FileDto>()
            .map { File.fromDto(it.first()) }
    }

    override suspend fun uploadDocumentToFirebase(uri: Uri): String? {
        return withContext(Dispatchers.IO + SupervisorJob()) {
            var pfd: ParcelFileDescriptor? = null

            try {
                // 1. MIME type + extension
                val mimeType = context.contentResolver.getType(uri)
                pfd = context.contentResolver.openFileDescriptor(uri, "r")

                val allowedMimeTypes = listOf(
                    "image/jpeg",
                    "image/png",
                    "application/pdf",
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

                // 2. USE EXACT SAME API AS HEXAGONALGAMES
                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("files/$fileName")

                // 3. Upload
                storageRef.putFile(uri).await()

                // 4. URL fournie directement par Firebase (à NE PAS modifier)
                return@withContext storageRef.downloadUrl.await().toString()

            } catch (e: Exception) {
                Log.e("FirebaseUpload", "Error while uploading", e)
                null
            } finally {
                pfd?.close()
            }
        }
    }
}