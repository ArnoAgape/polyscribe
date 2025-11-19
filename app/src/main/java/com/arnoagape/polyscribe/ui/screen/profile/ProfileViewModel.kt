package com.arnoagape.polyscribe.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Backing state for the current user profile. */
    private val _user = MutableStateFlow<User?>(null)

    /** Exposed immutable flow representing the currently signed-in user. */
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    val isUserFieldsValid = user
        .map { currentUser ->
            currentUser?.displayName?.isNotBlank() == true && (currentUser.email?.isNotBlank() == true)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Observes the current user from [UserRepository] and updates the [_user] state.
     * Called automatically when the ViewModel is initialized.
     */
    init {
        viewModelScope.launch {
            userRepository.observeUser()
                .collect { user ->
                    _user.value = user
                }
        }
    }

    /**
     * Ensures the authenticated user is present in Firestore.
     * This can be used to synchronize user data after login or profile updates.
     */
    fun syncUserWithFirestore() {
        viewModelScope.launch {
            userRepository.ensureUserInFirestore()
        }
    }

    /**
     * Observes whether a user is currently signed in.
     * Exposed as a [StateFlow] for reactive UI updates.
     */
    val isSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    fun onAction(formEvent: FormEvent) {
        when (formEvent) {
            is FormEvent.DisplayNameChanged -> {
                _user.update { it?.copy(displayName = formEvent.displayName) }
            }

            is FormEvent.EmailChanged -> {
                _user.update { it?.copy(email = formEvent.email) }
            }

            else -> Unit
        }
    }

    fun saveUser() {
        viewModelScope.launch {

            // 1. Network checking
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowSnackBar(R.string.no_network))
                return@launch
            }

            // 2. If user logged in checking
            val currentUser = _user.value
            if (currentUser == null) {
                _uiState.value = ProfileUiState.Error.NoAccount()
                _events.trySend(Event.ShowSnackBar(R.string.error_no_account_profile))
                return@launch
            }

            _uiState.value = ProfileUiState.Loading

            try {
                // 3. Creation of file with user
                val userToSave = _user.value?.copy(
                    displayName = currentUser.displayName,
                    email = currentUser.email
                )

                if (userToSave != null) {
                    // 4. Updates User on Firebase
                    userRepository.updateUser(userToSave)

                    // 5. Success UI
                    _uiState.value = ProfileUiState.Success(userToSave)
                    _events.trySend(Event.ShowSnackBar(R.string.success_user_updated))
                }

            } catch (e: IOException) {
                // 6. Network error (impossible upload)
                _uiState.value = ProfileUiState.Error.Generic("Network error: ${e.message}")
                _events.trySend(Event.ShowSnackBar(R.string.no_network))

            } catch (_: Exception) {
                // 7. Generic error (Firebase Storage, Firestore, etc.)
                _uiState.value = ProfileUiState.Error.Generic()
                _events.trySend(Event.ShowSnackBar(R.string.error_generic))
            }
        }
    }

    /**
     * Signs the user out using [UserRepository.signOut].
     * If successful, the local [_user] state is reset to null.
     */
    fun signOut() {
        viewModelScope.launch {
            val result = userRepository.signOut()
            if (result.isSuccess) {
                _user.value = null
            }
        }
    }

    /**
     * Deletes the currently authenticated user's account and associated Firestore data.
     * If successful, clears the local user state.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            val result = userRepository.deleteUser()
            if (result.isSuccess) {
                _user.value = null
            }
        }
    }
}
