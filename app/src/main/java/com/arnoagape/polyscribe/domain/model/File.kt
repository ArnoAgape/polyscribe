package com.arnoagape.polyscribe.domain.model

import com.arnoagape.polyscribe.data.dto.FileDto
import com.google.firebase.Timestamp
import java.time.Instant

data class File(
    val id: String = "",
    val fileUrl: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val date: Instant = Instant.now(),
    val time: Instant = Instant.now(),
    val author: User? = null,
    val isColored: Boolean = false,
    val isDoubleSided: Boolean = false,
    val numberOfCopies: Int = 1,
    val comment: String = ""
) {

    fun toDto(): FileDto {
        return FileDto(
            id = id,
            fileUrl = fileUrl,
            createdAt = createdAt,
            date = date.toString(),
            time = time.toString(),
            author = author,
            isColored = isColored,
            isDoubleSided = isDoubleSided,
            numberOfCopies = numberOfCopies,
            comment = comment
        )
    }
}