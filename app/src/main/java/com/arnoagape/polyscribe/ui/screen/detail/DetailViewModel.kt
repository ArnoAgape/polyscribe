package com.arnoagape.polyscribe.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val fileId: String = checkNotNull(savedStateHandle["fileId"])

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isUserSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val state: StateFlow<DetailScreenState> =
        combine(
            uiState,
            isUserSignedIn,
            _isRefreshing
        ) { ui, signedIn, refresh ->
            DetailScreenState(
                uiState = ui,
                isSignedIn = signedIn,
                isRefreshing = refresh
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailScreenState()
        )

    init {
        observeFile()
    }

    private fun observeFile() {
        viewModelScope.launch {
            userRepository.observeUser()
                .filterNotNull()
                .flatMapLatest { user ->
                    fileRepository.getFileById(fileId, user.id)
                }
                .onStart {
                    _uiState.value = DetailUiState.Loading
                }
                .catch { e ->
                    _uiState.value = DetailUiState.Error.Generic(
                        e.message ?: "Unknown error"
                    )
                }
                .collect { file ->
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
                _events.trySend(Event.ShowMessage(R.string.no_network))
                return@launch
            }
        }
    }
}

/**
 * Combined UI state for the detail screen,
 * merging file loading state and authentication status.
 */
data class DetailScreenState(
    val uiState: DetailUiState = DetailUiState.Loading,
    val isSignedIn: Boolean = false,
    val isRefreshing: Boolean = false,
)