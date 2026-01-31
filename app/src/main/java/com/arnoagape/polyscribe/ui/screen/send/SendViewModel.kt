package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel responsible for preparing and uploading a file.
 *
 * Manages local URIs, file metadata, validation logic,
 * network checks, and one-time UI events.
 */
@HiltViewModel
class SendViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SendUiState>(SendUiState.Idle)
    private val _user = MutableStateFlow<User?>(null)
    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    private val _localUris = MutableStateFlow<List<Uri>>(emptyList())

    val isGuest: Boolean
        get() = _user.value == null

    private val _file = MutableStateFlow(
        File(
            id = UUID.randomUUID().toString(),
            fileUrl = emptyList(),
            createdAt = Timestamp.now(),
            dateTime = Instant.now(),
            author = null,
            colored = false,
            doubleSided = false,
            numberOfCopies = 1,
            comment = "",
        )
    )

    /**
     * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
     */
    private val _isFileValid = _localUris
        .map { uris -> uris.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val state: StateFlow<SendScreenState> =
        combine(
            _uiState,
            _file,
            _isFileValid,
            _localUris
        ) { ui, f, valid, local ->
            SendScreenState(
                uiState = ui,
                file = f,
                isValid = valid,
                localUris = local
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SendScreenState()
        )

    init {
        viewModelScope.launch {
            _user.value = userRepository.getCurrentUser()
        }
    }

    /**
     * Handles user actions modifying the file or selected URIs.
     */
    fun onAction(formEvent: FormEvent) {
        when (formEvent) {
            is FormEvent.DateTimeChanged -> {
                _file.update { it.copy(dateTime = formEvent.dateTime) }
            }

            is FormEvent.ColorChanged -> {
                _file.update { it.copy(colored = formEvent.colored) }
            }

            is FormEvent.DoubleSidedChanged -> {
                _file.update { it.copy(doubleSided = formEvent.doubleSided) }
            }

            is FormEvent.NumberOfCopiesSet -> {
                _file.update { file ->
                    file.copy(numberOfCopies = formEvent.value.coerceAtLeast(1))
                }
            }

            is FormEvent.AddFile -> {
                _localUris.update { it + formEvent.uri }
            }

            is FormEvent.RemoveFile -> {
                _localUris.update { it - formEvent.uri }
            }

            is FormEvent.CommentChanged -> {
                _file.update { it.copy(comment = formEvent.comment) }
            }

            else -> Unit
        }
    }

    /**
     * Uploads the selected files to Firebase Storage and Firestore.
     * Performs network and authentication checks before uploading.
     */
    fun sendFile() {
        viewModelScope.launch {
            _uiState.value = SendUiState.Loading

            val currentUser = _user.value
            val fileToSave = _file.value.copy(author = currentUser)

            val result = fileRepository.sendFile(
                localUris = _localUris.value,
                file = fileToSave
            )

            result
                .onSuccess {
                    _uiState.value = SendUiState.Success(fileToSave)

                    _events.trySend(
                        Event.ShowSuccessMessage(R.string.success_file)
                    )
                }
                .onFailure { throwable ->
                    Log.e("SendViewModel", "sendFile failed", throwable)
                    _uiState.value = SendUiState.Error.Generic()
                    _events.trySend(Event.ShowMessage(R.string.error_generic))
                }

        }
    }
}

/**
 * Combined UI state for the send screen,
 * containing the file, selection, and validation status.
 */
data class SendScreenState(
    val uiState: SendUiState = SendUiState.Idle,
    val file: File = File(),
    val isValid: Boolean = false,
    val localUris: List<Uri> = emptyList()
)