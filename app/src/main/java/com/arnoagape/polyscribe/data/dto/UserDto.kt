package com.arnoagape.polyscribe.data.dto

import java.io.Serializable

/**
 * Data model representing a user stored in Firestore/Storage.
 */
data class UserDto(
    val id: String = "",
    val displayName: String? = "",
    val phoneNumber: String? = "",
    val email: String? = "",
    val professional: Boolean = false
) : Serializable

data class AuthorSnapshot(
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
)