package com.arnoagape.polyscribe.domain.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class File(
    val id: String = "",
    val fileUrl: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val date: String = "",
    val time: String = "",
    val author: User? = null,
    val isColored: Boolean = false,
    val isDoubleSided: Boolean = false,
    val numberOfCopies: Int = 1,
    val comment: String = ""
) : Serializable