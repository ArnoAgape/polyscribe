package com.arnoagape.polyscribe.ui.screen.home

import com.arnoagape.polyscribe.domain.model.File

/**
 * Represents the UI state for the home screen.
 */
sealed class HomeUiState {
    /** Initial idle state. */
    object Idle : HomeUiState()

    /** Indicates that files are loading. */
    object Loading : HomeUiState()

    /** State emitted when files are successfully loaded. */
    data class Success(val files: List<File>) : HomeUiState()

    /** Error states for the home screen. */
    sealed class Error : HomeUiState() {
        /** No files available. */
        data class Empty(val message: String = "No files found") : Error()

        /** A generic error occurred. */
        data class Generic(val message: String = "Unknown error") : Error()
    }
}