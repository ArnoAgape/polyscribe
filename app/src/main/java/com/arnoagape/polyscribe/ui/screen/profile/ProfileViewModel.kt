package com.arnoagape.polyscribe.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.UserSession
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.utils.AndroidEmailValidator
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing user profile data.
 *
 * It observes the authenticated user, validates input fields,
 * persists profile updates, emits UI events, and handles sign-out or deletion.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils,
    private val emailValidator: AndroidEmailValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val editedUser = MutableStateFlow<User?>(null)

    val user: StateFlow<User?> =
        combine(
            userRepository.observeUser(),
            editedUser
        ) { repoUser, edited ->
            edited ?: repoUser
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    val isUserFieldsValid: StateFlow<Boolean> =
        user.map { currentUser ->
            val displayName = currentUser?.displayName.orEmpty()
            displayName.isNotBlank() &&
                    emailValidator.validate(currentUser?.email)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    val state: StateFlow<ProfileScreenState> =
        combine(
            uiState,
            user,
            isUserFieldsValid
        ) { ui, u, valid ->
            ProfileScreenState(
                uiState = ui,
                user = u,
                isValid = valid
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ProfileScreenState()
        )

    val session: StateFlow<UserSession> =
        userRepository.observeUserSession()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                UserSession(userId = null, isGuest = false)
            )

    fun onAction(formEvent: FormEvent) {
        editedUser.update { current ->
            val base = current ?: user.value ?: return
            when (formEvent) {
                is FormEvent.DisplayNameChanged ->
                    base.copy(displayName = formEvent.displayName)

                is FormEvent.EmailChanged ->
                    base.copy(email = formEvent.email)

                else -> base
            }
        }
    }

    fun saveUser() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowMessage(R.string.no_network))
                return@launch
            }

            val currentUser = editedUser.value ?: user.value
            if (currentUser == null) {
                _uiState.value = ProfileUiState.Error.NoAccount()
                _events.trySend(Event.ShowMessage(R.string.error_no_account_profile))
                return@launch
            }

            _uiState.value = ProfileUiState.Loading

            runCatching {
                userRepository.updateUser(currentUser)
            }.onSuccess {
                editedUser.value = null
                _uiState.value = ProfileUiState.Success(currentUser)
                _events.trySend(Event.ShowSuccessMessage(R.string.success_user_updated))
            }.onFailure {
                _uiState.value = ProfileUiState.Error.Generic()
                _events.trySend(Event.ShowMessage(R.string.error_generic))
            }
        }
    }

    fun signOut() = viewModelScope.launch {
        userRepository.signOut().onSuccess {
            _events.trySend(Event.ShowSuccessMessage(R.string.success_sign_out))
        }
    }

    /**
     * Deletes the current user's account and associated Firestore data.
     */
    fun deleteAccount() = viewModelScope.launch {
        userRepository.deleteUser().onSuccess {
            _events.trySend(Event.ShowSuccessMessage(R.string.success_deleted_account))
        }
    }
}

data class ProfileScreenState(
    val uiState: ProfileUiState = ProfileUiState.Idle,
    val user: User? = null,
    val isValid: Boolean = false
)