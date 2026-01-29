package com.arnoagape.polyscribe.ui.screen.send

import com.arnoagape.polyscribe.domain.model.File

/**
 * Represents the UI state for the send screen.
 */
sealed class SendUiState {

    object Idle : SendUiState()
    object Loading : SendUiState()

    data class Success(val file: File) : SendUiState()

    sealed class Error : SendUiState() {
        data class NoAccount(val message: String = "No account found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}