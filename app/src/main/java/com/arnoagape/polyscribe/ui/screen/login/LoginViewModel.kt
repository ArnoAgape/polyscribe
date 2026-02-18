package com.arnoagape.polyscribe.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.SessionType
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for authentication logic.
 *
 * Exposes the sign-in state, synchronizes the user with Firestore,
 * and emits one-time events for UI actions such as messages.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val _session = MutableStateFlow<SessionType?>(null)

    private val _isSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )
    val isSignedIn: StateFlow<Boolean?> = _isSignedIn

    val state: StateFlow<LoginScreenState> =
        combine(
            _session,
            _isSignedIn
        ) { session, isSignedIn ->
            LoginScreenState(
                session = when {
                    isSignedIn == true -> SessionType.Authenticated
                    session == SessionType.Guest -> SessionType.Guest
                    else -> null
                }
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LoginScreenState()
        )

    fun onSignInRequested(onAllowed: () -> Unit) {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowMessage(R.string.no_network))
                return@launch
            }

            onAllowed()
        }
    }

    fun syncUserWithFirestore() {
        viewModelScope.launch {
            userRepository.ensureUserInFirestore()
        }
    }

    fun loginAsGuest(): SessionType {
        _session.value = SessionType.Guest
        return SessionType.Guest
    }

    fun sendEvent(event: Event) {
        _events.trySend(event)
    }

}

data class LoginScreenState(
    val session: SessionType? = null
)