package com.arnoagape.polyscribe.ui.screen.home

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    /** Holds the current state of the home feed UI (loading, success, or error). */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    /** Publicly exposed immutable flow for observing post-related UI states. */
    val uiState: StateFlow<HomeUiState> = _uiState

    /** Channel used for one-time UI events such as displaying toasts. */
    private val _events = Channel<Event>()

    /** Flow that emits UI events (e.g., toast messages). */
    val eventsFlow = _events.receiveAsFlow()

    /** Observes whether a user is currently signed in. */
    val isUserSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        getAllFiles()
    }

    private fun getAllFiles() {
        viewModelScope.launch {
            fileRepository.getFilesOrderByCreationDateDesc()
                .onStart { _uiState.value = HomeUiState.Loading }
                .catch { e ->
                    _uiState.value = HomeUiState.Error.Generic(e.message ?: "Unknown error")
                }
                .collect { posts ->
                    _uiState.value = if (posts.isEmpty()) {
                        HomeUiState.Error.Empty()
                    } else {
                        HomeUiState.Success(posts)
                    }
                }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowSnackBar(R.string.no_network))
                return@launch
            }
            getAllFiles()
        }
    }
}