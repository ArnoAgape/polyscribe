package com.arnoagape.polyscribe.domain.model

import com.arnoagape.polyscribe.data.dto.FileDto
import com.google.firebase.Timestamp
import java.time.Instant

data class File(
    val id: String = "",
    val fileUrl: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val dateTime: Instant = Instant.now(),
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
            dateTime = Timestamp(dateTime.epochSecond, dateTime.nano),
            author = author,
            isColored = isColored,
            isDoubleSided = isDoubleSided,
            numberOfCopies = numberOfCopies,
            comment = comment
        )
    }

    companion object {
        fun fromDto(dto: FileDto): File {
            return File(
                id = dto.id,
                fileUrl = dto.fileUrl,
                createdAt = dto.createdAt,
                dateTime = dto.dateTime.toDate().toInstant(),
                author = dto.author,
                isColored = dto.isColored,
                isDoubleSided = dto.isDoubleSided,
                numberOfCopies = dto.numberOfCopies,
                comment = dto.comment
            )
        }
    }
}