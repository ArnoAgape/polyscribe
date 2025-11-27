package com.arnoagape.polyscribe.domain.model

import java.io.Serializable

/**
 * Domain model representing an application user.
 */
data class User(
    val id: String = "",
    val displayName: String? = "",
    val phoneNumber: String? = "",
    val email: String? = "",
    val isProfessional: Boolean = false
) : Serializable