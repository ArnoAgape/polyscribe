package com.arnoagape.polyscribe.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel responsible for loading a single file and exposing UI state
 * for the detail screen.
 *
 * It observes the file in Firestore, handles loading/error states,
 * and emits one-time events such as network warnings.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    val fileRepository: FileRepository,
    val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val fileId: String = checkNotNull(savedStateHandle["fileId"])

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    val isUserSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    val fileState: StateFlow<DetailUiState> =
        fileRepository.observeFile(fileId)
            .map { file ->
                when (file) {
                    null -> DetailUiState.Error.Empty("Impossible to find the file")
                    else -> DetailUiState.Success(file)
                }
            }
            .onStart { emit(DetailUiState.Loading) }
            .catch { e ->
                emit(
                    DetailUiState.Error.Generic(
                        e.message ?: "Unknown error"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DetailUiState.Loading
            )

    val state: StateFlow<DetailScreenState> =
        combine(
            fileState,
            isUserSignedIn
        ) { ui, signedIn ->
            DetailScreenState(
                uiState = ui,
                isSignedIn = signedIn == true
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailScreenState()
        )
}

data class DetailScreenState(
    val uiState: DetailUiState = DetailUiState.Loading,
    val isSignedIn: Boolean = false
)