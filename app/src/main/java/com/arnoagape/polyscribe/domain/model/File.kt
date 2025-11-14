package com.arnoagape.polyscribe.domain.model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime

data class File(
    val id: String,
    val fileUrl: String?,
    val createdAt: Timestamp,
    val date: LocalDate,
    val time: LocalTime,
    val author: User?,
    val isColored: Boolean,
    val isDoubleSided: Boolean,
    val numberOfCopies: Int,
    val comments: String
) : Serializable