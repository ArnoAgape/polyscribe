package com.arnoagape.polyscribe.data.dto

import com.arnoagape.polyscribe.domain.model.User
import com.google.firebase.Timestamp
import java.io.Serializable

/**
 * Data model representing a file stored in Firestore/Storage.
 * Contains metadata, print options, author information
 * and creation timestamps.
 */
data class FileDto(
    val id: String = "",
    val fileUrl: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val dateTime: Timestamp = Timestamp.now(),
    val author: User? = null,
    val colored: Boolean = false,
    val doubleSided: Boolean = false,
    val numberOfCopies: Int = 1,
    val comment: String = ""
) : Serializable