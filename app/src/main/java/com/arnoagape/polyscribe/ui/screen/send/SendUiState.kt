package com.arnoagape.polyscribe.ui.screen.send

import com.arnoagape.polyscribe.domain.model.File

/**
 * Represents the UI state for the send screen.
 */
sealed class SendUiState {

    /** Initial idle state. */
    object Idle : SendUiState()

    /** Indicates that the file upload is in progress. */
    object Loading : SendUiState()

    /** Successfully uploaded file. */
    data class Success(val file: File) : SendUiState()

    /** Error states for the send screen. */
    sealed class Error : SendUiState() {
        /** No user account was found. */
        data class NoAccount(val message: String = "No account found") : Error()

        /** A generic error occurred. */
        data class Generic(val message: String = "Unknown error") : Error()
    }
}