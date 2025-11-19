package com.arnoagape.polyscribe.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    fileRepository: FileRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    val uiState =
        fileRepository.files
            .map { files ->
                if (files.isEmpty()) HomeUiState.Error.Empty()
                else HomeUiState.Success(files)
            }
            .catch { e ->
                emit(HomeUiState.Error.Generic(e.message ?: "Unknown error"))
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                HomeUiState.Loading
            )

    fun refreshPosts() {
        if (!networkUtils.isNetworkAvailable()) {
            _events.trySend(Event.ShowSnackBar(R.string.no_network))
        }
    }
}