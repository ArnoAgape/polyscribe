package com.arnoagape.polyscribe.domain.model

import com.arnoagape.polyscribe.data.dto.FileDto
import com.arnoagape.polyscribe.data.dto.UserDto

/**
 * Domain model representing an application user.
 */
data class User(
    val id: String = "",
    val displayName: String? = "",
    val phoneNumber: String? = "",
    val email: String? = "",
    val professional: Boolean = false
) {
    /**
     * Converts this domain model to its Firestore DTO representation.
     */
    fun toDto(): UserDto {
        return UserDto(
            id = id,
            displayName = displayName,
            phoneNumber = phoneNumber,
            email = email,
            professional = professional
        )
    }

    companion object {
        /**
         * Converts a [FileDto] from Firestore to a domain [File] model.
         */
        fun fromDto(dto: UserDto): User {
            return User(
                id = dto.id,
                displayName = dto.displayName,
                phoneNumber = dto.phoneNumber,
                email = dto.email,
                professional = dto.professional
            )
        }
    }
}