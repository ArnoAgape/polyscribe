package com.arnoagape.polyscribe.ui.screen.detail

import com.arnoagape.polyscribe.domain.model.File

sealed class DetailUiState {
    object Idle : DetailUiState()
    object Loading : DetailUiState()
    data class Success(val file: File) : DetailUiState()
    sealed class Error : DetailUiState() {
        data class Empty(val message: String = "No file found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}