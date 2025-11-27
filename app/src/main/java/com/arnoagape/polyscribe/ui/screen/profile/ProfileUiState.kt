package com.arnoagape.polyscribe.ui.screen.profile

import com.arnoagape.polyscribe.domain.model.User

/**
 * Represents the UI state for the profile screen.
 */
sealed class ProfileUiState {

    /** Initial idle state. */
    object Idle : ProfileUiState()

    /** Indicates that profile data is being processed. */
    object Loading : ProfileUiState()

    /** Successfully loaded or updated user profile. */
    data class Success(val user: User) : ProfileUiState()

    /** Error states for profile operations. */
    sealed class Error : ProfileUiState() {
        /** No user account found. */
        data class NoAccount(val message: String = "No account found") : Error()

        /** A generic error occurred. */
        data class Generic(val message: String = "Unknown error") : Error()
    }
}