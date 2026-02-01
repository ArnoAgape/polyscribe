package com.arnoagape.polyscribe.data.dto

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
    val collectDate: Timestamp = Timestamp.now(),
    val author: AuthorSnapshot? = null,
    val colored: Boolean = false,
    val doubleSided: Boolean = false,
    val copies: Int = 1,
    val comment: String = "",
    val ownerId: String? = null,
    val guestId: String? = null,
) : Serializable