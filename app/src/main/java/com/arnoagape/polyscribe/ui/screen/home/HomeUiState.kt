package com.arnoagape.polyscribe.ui.screen.home

import com.arnoagape.polyscribe.domain.model.File

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val files: List<File>) : HomeUiState()
    sealed class Error : HomeUiState() {
        data class Empty(val message: String = "No files found") : Error()
        data class Generic(val message: String = "Unknown error") : Error()
    }
}