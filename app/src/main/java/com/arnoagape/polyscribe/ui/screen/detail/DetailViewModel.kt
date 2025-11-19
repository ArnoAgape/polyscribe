package com.arnoagape.polyscribe.ui.screen.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val fileId: String = checkNotNull(savedStateHandle["fileId"])

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isUserSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val state: StateFlow<DetailScreenState> =
        combine(
            uiState,
            isUserSignedIn
        ) { ui, signedIn ->
            DetailScreenState(
                uiState = ui,
                isSignedIn = signedIn
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailScreenState()
        )

    init {
        Log.d("DetailViewModel", "fileId = $fileId")
        observeFile()
    }

    private fun observeFile() {
        viewModelScope.launch {
            fileRepository.getFileById(fileId)
                .onStart {
                    _uiState.value = DetailUiState.Loading
                }
                .catch { e ->
                    _uiState.value = DetailUiState.Error.Generic(
                        e.message ?: "Unknown error"
                    )
                }
                .collect { file ->
                    Log.d("DetailViewModel", "Firestore returned = $file")
                    _uiState.value = when (file) {
                        null -> DetailUiState.Error.Empty("Impossible to find the file")
                        else -> DetailUiState.Success(file)
                    }
                }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowSnackBar(R.string.no_network))
            } else {
                observeFile()
            }
        }
    }
}

data class DetailScreenState(
    val uiState: DetailUiState = DetailUiState.Idle,
    val isSignedIn: Boolean = false
)