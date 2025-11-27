package com.arnoagape.polyscribe.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for loading and refreshing the list of files.
 *
 * It exposes UI state, manages refresh actions, and emits one-time
 * events such as network warnings.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    fileRepository: FileRepository,
    userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<HomeUiState> =
        userRepository.observeUser()
            .filterNotNull()
            .flatMapLatest { user ->
                fileRepository.filesForUser(user.id)
            }
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

    val state: StateFlow<HomeScreenState> =
        combine(
            uiState,
            isRefreshing
        ) { ui, isRef ->
            HomeScreenState(
                uiState = ui,
                isRefreshing = isRef
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeScreenState(
                uiState = HomeUiState.Loading,
                isRefreshing = false
            )
        )

    init {
        networkUtils.checkNetwork(networkUtils, _events)
    }

    fun refreshFiles() {
        viewModelScope.launch {
            networkUtils.checkNetwork(networkUtils, _events)
            _isRefreshing.value = true

            kotlinx.coroutines.delay(700)

            _isRefreshing.value = false
        }
    }
}

/**
 * Combined UI state for the home screen,
 * including loading state and refresh status.
 */
data class HomeScreenState(
    val uiState: HomeUiState = HomeUiState.Idle,
    val isRefreshing: Boolean = false
)