package com.arnoagape.polyscribe.domain.model

import com.arnoagape.polyscribe.data.dto.AuthorSnapshot
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
    val collectDate: Instant = Instant.now(),
    val author: AuthorSnapshot? = null,
    val colored: Boolean = false,
    val doubleSided: Boolean = false,
    val numberOfCopies: Int = 1,
    val comment: String = "",
    val guestName: String = ""
) {

    /**
     * Converts this domain model to its Firestore DTO representation.
     */
    fun toDto(): FileDto {
        return FileDto(
            id = id,
            fileUrl = fileUrl,
            createdAt = createdAt,
            collectDate = Timestamp(collectDate.epochSecond, collectDate.nano),
            author = author,
            colored = colored,
            doubleSided = doubleSided,
            copies = numberOfCopies,
            comment = comment,
            ownerId = ownerId,
            guestId = guestId,
            guestName = guestName
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
                collectDate = dto.collectDate.toDate().toInstant(),
                author = dto.author,
                colored = dto.colored,
                doubleSided = dto.doubleSided,
                numberOfCopies = dto.copies,
                comment = dto.comment,
                guestName = dto.guestName
            )
        }
    }
}