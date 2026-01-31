package com.arnoagape.polyscribe.data.dto

import com.google.firebase.Timestamp

data class GuestFileDto(
    val id: String = "",
    val fileUrl: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val dateTime: Timestamp = Timestamp.now(),
    val colored: Boolean = false,
    val doubleSided: Boolean = false,
    val numberOfCopies: Int = 1,
    val comment: String = ""
)