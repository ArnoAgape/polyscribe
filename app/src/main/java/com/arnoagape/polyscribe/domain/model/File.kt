package com.arnoagape.polyscribe.domain.model

import android.util.Log
import com.arnoagape.polyscribe.data.dto.FileDto
import com.google.firebase.Timestamp
import java.time.Instant

data class File(
    val id: String = "",
    val fileUrl: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val dateTime: Instant = Instant.now(),
    val author: User? = null,
    val colored: Boolean = false,
    val doubleSided: Boolean = false,
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
            colored = colored,
            doubleSided = doubleSided,
            numberOfCopies = numberOfCopies,
            comment = comment
        )
    }

    companion object {
        fun fromDto(dto: FileDto): File {
            Log.e("FIRESTORE_READ", "DTO lu = $dto")
            return File(
                id = dto.id,
                fileUrl = dto.fileUrl,
                createdAt = dto.createdAt,
                dateTime = dto.dateTime.toDate().toInstant(),
                author = dto.author,
                colored = dto.colored,
                doubleSided = dto.doubleSided,
                numberOfCopies = dto.numberOfCopies,
                comment = dto.comment
            )
        }
    }
}