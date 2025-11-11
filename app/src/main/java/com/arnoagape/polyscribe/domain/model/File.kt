package com.arnoagape.polyscribe.domain.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class File(
    val id: String = "",
    val url: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val date: String? = null,
    val time: String? = null,
    val author: User? = null,
    val isColored: Boolean = false,
    val isDoubleSided: Boolean = false,
    val numberOfCopies: Int = 0,
    val comments: String? = null
) : Serializable