package com.arnoagape.polyscribe.ui.screen.send

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.dto.AuthorSnapshot
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.SessionType
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val userRepository: UserRepository,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        private const val MAX_FILES = 10
        private const val MAX_TOTAL_SIZE = 50 * 1024 * 1024L // 50 MB
    }

    private val _uiState = MutableStateFlow<SendUiState>(SendUiState.Idle)
    private val _events = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = _events.receiveAsFlow()

    private val _localUris = MutableStateFlow<List<Uri>>(emptyList())

    private val _isSignedIn =
        userRepository.isUserSignedIn()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private val _formState = MutableStateFlow(
        SendFormState(
            colored = false,
            doubleSided = false,
            numberOfCopies = 1,
            comment = "",
            dateTime = Instant.now(),
            guestName = ""
        )
    )

    val state: StateFlow<SendScreenState> =
        combine(
            _uiState,
            _formState,
            _localUris,
            _isSignedIn
        ) { ui, f, uris, s ->
            SendScreenState(
                uiState = ui,
                file = f.toFile(),
                localUris = uris,
                isValid = uris.isNotEmpty(),
                isSignedIn = s
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
                    form.copy(
                        numberOfCopies = event.value
                            .coerceAtLeast(1)
                            .coerceAtMost(999)
                    )

                is FormEvent.CommentChanged -> form.copy(comment = event.comment)
                is FormEvent.GuestNameChanged -> form.copy(guestName = event.guestName) // LoginScreen field
                else -> form
            }
        }

        when (event) {
            is FormEvent.AddFile -> {
                _localUris.update { current ->

                    val newList = current + event.uri

                    val totalSize = newList.sumOf { uri ->
                        getFileSize(uri)
                    }

                    if (newList.size > 10) {
                        viewModelScope.launch {
                            _events.send(Event.ErrorUploadFiles(R.string.error_max_files))
                        }
                        current
                    } else if (totalSize > MAX_TOTAL_SIZE) {
                        viewModelScope.launch {
                            _events.send(Event.ErrorUploadFiles(R.string.error_max_size))
                        }
                        current
                    } else {
                        newList
                    }
                }
            }

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

            val totalSize = _localUris.value.sumOf { getFileSize(it) }

            if (_localUris.value.size > MAX_FILES) {
                _uiState.value = SendUiState.Error.Generic()
                _events.trySend(Event.ErrorUploadFiles(R.string.error_max_files))
                return@launch
            }

            if (totalSize > MAX_TOTAL_SIZE) {
                _uiState.value = SendUiState.Error.Generic()
                _events.trySend(Event.ErrorUploadFiles(R.string.error_max_size))
                return@launch
            }

            val file = _formState.value.toFile(
                id = UUID.randomUUID().toString()
            )

            val sessionType =
                if (userRepository.getCurrentUser() == null)
                    SessionType.Guest
                else
                    SessionType.Authenticated

            //Log.d("SEND_DEBUG", "GuestName sent: ${_formState.value.guestName}")

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

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver
                .openFileDescriptor(uri, "r")
                ?.use { it.statSize }
                ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

data class SendScreenState(
    val uiState: SendUiState = SendUiState.Idle,
    val file: File = File(),
    val isValid: Boolean = false,
    val localUris: List<Uri> = emptyList(),
    val isSignedIn: Boolean? = null
)

data class SendFormState(
    val colored: Boolean,
    val doubleSided: Boolean,
    val numberOfCopies: Int,
    val comment: String,
    val dateTime: Instant,
    val guestName: String // LoginScreen field
) {
    fun toFile(
        id: String = "",
        author: AuthorSnapshot? = null
    ) = File(
        id = id,
        colored = colored,
        doubleSided = doubleSided,
        numberOfCopies = numberOfCopies,
        comment = comment,
        collectDate = dateTime,
        author = author,
        guestName = guestName // LoginScreen field
    )
}