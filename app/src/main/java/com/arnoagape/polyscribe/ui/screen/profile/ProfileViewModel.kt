package com.arnoagape.polyscribe.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

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

    /**
     * Ensures the authenticated user is present in Firestore.
     * This can be used to synchronize user data after login or profile updates.
     */
    fun syncUserWithFirestore() {
        viewModelScope.launch {
            userRepository.ensureUserInFirestore()
        }
    }
}