package com.arnoagape.polyscribe.data.service.file

import android.net.Uri
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class FirebaseFileApi @Inject constructor(
    private val networkUtils: NetworkUtils
) : FileApi {

    private val firestore = FirebaseFirestore.getInstance()
    private val filesCollection = firestore.collection("files")

    override fun getFilesOrderByCreationDateDesc(): Flow<List<File>> {
        TODO("Not yet implemented")
    }

    override suspend fun sendFile(file: File) {
        TODO("Not yet implemented")
    }

    override fun getFileById(fileId: String): Flow<File?> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadDocumentToFirebase(uri: Uri): String? {
        TODO("Not yet implemented")
    }
}