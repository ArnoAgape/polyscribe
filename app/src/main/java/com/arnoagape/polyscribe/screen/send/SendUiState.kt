package com.arnoagape.polyscribe.screen.send

import com.arnoagape.polyscribe.domain.model.File

sealed class SendUiState {
    object Idle : SendUiState()
    object Loading : SendUiState()
    data class Success(val file: File) : SendUiState()
    sealed class Error : SendUiState() {
        data class NoAccount(val message: String = "No account found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}