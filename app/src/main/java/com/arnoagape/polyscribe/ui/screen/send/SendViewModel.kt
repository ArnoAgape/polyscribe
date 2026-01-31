package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.SessionType
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    private val _localUris = MutableStateFlow<List<Uri>>(emptyList())

    private val _formState = MutableStateFlow(
        SendFormState(
            colored = false,
            doubleSided = false,
            numberOfCopies = 1,
            comment = "",
            dateTime = Instant.now()
        )
    )

    val state: StateFlow<SendScreenState> =
        combine(
            _uiState,
            _formState,
            _localUris
        ) { ui, f, uris ->
            SendScreenState(
                uiState = ui,
                file = f.toFile(),
                localUris = uris,
                isValid = uris.isNotEmpty()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SendScreenState()
        )

    /**
     * Handles user actions modifying the file or selected URIs.
     */
    fun onAction(event: FormEvent) {
        _formState.update { form ->
            when (event) {
                is FormEvent.DateTimeChanged -> form.copy(dateTime = event.dateTime)
                is FormEvent.ColorChanged -> form.copy(colored = event.colored)
                is FormEvent.DoubleSidedChanged -> form.copy(doubleSided = event.doubleSided)
                is FormEvent.NumberOfCopiesSet ->
                    form.copy(numberOfCopies = event.value.coerceAtLeast(1))

                is FormEvent.CommentChanged -> form.copy(comment = event.comment)
                else -> form
            }
        }

        when (event) {
            is FormEvent.AddFile -> _localUris.update { it + event.uri }
            is FormEvent.RemoveFile -> _localUris.update { it - event.uri }
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

            val file = _formState.value.toFile(
                id = UUID.randomUUID().toString()
            )

            val sessionType =
                if (userRepository.getCurrentUser() == null)
                    SessionType.Guest
                else
                    SessionType.Authenticated

            runCatching {
                fileRepository.sendFile(
                    localUris = _localUris.value,
                    file = file
                )
            }.onSuccess {
                _uiState.value = SendUiState.Success(file)
                _events.trySend(Event.FileSentSuccessfully(sessionType))
            }.onFailure {
                _uiState.value = SendUiState.Error.Generic()
                _events.trySend(Event.ShowMessage(R.string.error_generic))
            }
        }
    }
}

data class SendScreenState(
    val uiState: SendUiState = SendUiState.Idle,
    val file: File = File(),
    val isValid: Boolean = false,
    val localUris: List<Uri> = emptyList()
)

data class SendFormState(
    val colored: Boolean,
    val doubleSided: Boolean,
    val numberOfCopies: Int,
    val comment: String,
    val dateTime: Instant
) {
    fun toFile(
        id: String = "",
        author: User? = null
    ) = File(
        id = id,
        colored = colored,
        doubleSided = doubleSided,
        numberOfCopies = numberOfCopies,
        comment = comment,
        dateTime = dateTime,
        author = author
    )
}