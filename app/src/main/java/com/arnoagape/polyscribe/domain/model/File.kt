package com.arnoagape.polyscribe.domain.model

import com.arnoagape.polyscribe.data.dto.FileDto
import com.google.firebase.Timestamp
import java.time.Instant

/**
 * Domain model representing a file stored in Firebase.
 * Contains metadata, print options, author information,
 * and timestamp values.
 */
data class File(
    val id: String = "",
    val ownerId: String? = null,
    val guestId: String? = null,
    val fileUrl: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val dateTime: Instant = Instant.now(),
    val author: User? = null,
    val colored: Boolean = false,
    val doubleSided: Boolean = false,
    val numberOfCopies: Int = 1,
    val comment: String = ""
) {

    /**
     * Converts this domain model to its Firestore DTO representation.
     */
    fun toDto(): FileDto {
        return FileDto(
            id = id,
            ownerId = ownerId,
            guestId = guestId,
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
        /**
         * Converts a [FileDto] from Firestore to a domain [File] model.
         */
        fun fromDto(dto: FileDto): File {
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