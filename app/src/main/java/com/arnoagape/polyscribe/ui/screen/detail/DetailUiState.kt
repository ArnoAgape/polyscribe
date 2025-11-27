package com.arnoagape.polyscribe.ui.screen.detail

import com.arnoagape.polyscribe.domain.model.File

/**
 * Represents the UI state for the detail screen.
 */
sealed class DetailUiState {
    /** Initial idle state. */
    object Idle : DetailUiState()

    /** Indicates that data is being loaded. */
    object Loading : DetailUiState()

    /** State emitted when the file is successfully loaded. */
    data class Success(val file: File) : DetailUiState()

    /** Error states for the detail screen. */
    sealed class Error : DetailUiState() {
        /** No file was found. */
        data class Empty(val message: String = "No file found") : Error()

        /** A generic error occurred. */
        data class Generic(val message: String = "Unknown error") : Error()
    }
}