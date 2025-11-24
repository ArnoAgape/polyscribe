package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.io.IOException
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<SendUiState>(SendUiState.Idle)
    val uiState: StateFlow<SendUiState> = _uiState.asStateFlow()
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    private val _localUris = MutableStateFlow<List<Uri>>(emptyList())
    val localUris = _localUris.asStateFlow()


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
     * Public state flow representing the current post being edited.
     * This is immutable for consumers.
     */
    val file: StateFlow<File> = _file.asStateFlow()

    /**
     * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
     */
    val isFileValid = localUris
        .map { uris -> uris.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val state: StateFlow<SendScreenState> =
        combine(
            uiState,
            file,
            isFileValid,
            localUris
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

    fun sendFile() {
        viewModelScope.launch {

            // 1. Network checking
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowMessage(R.string.no_network))
                return@launch
            }

            // 2. If user logged in checking
            val currentUser = _user.value
            if (currentUser == null) {
                _uiState.value = SendUiState.Error.NoAccount()
                _events.trySend(Event.ShowMessage(R.string.error_no_account_file))
                return@launch
            }

            _uiState.value = SendUiState.Loading

            try {
                // 3. Creation of file with user
                val fileToSave = _file.value.copy(author = currentUser)

                // 4. Upload Storage + Firestore
                val uploadedFiles = fileRepository.sendFile(_localUris.value, fileToSave)

                _file.update {
                    it.copy(fileUrl = uploadedFiles)
                }

                // 5. Success UI
                _uiState.value = SendUiState.Success(fileToSave)
                _events.trySend(Event.ShowSuccessMessage(R.string.success_file))

            } catch (e: IOException) {
                // 6. Network error (impossible upload)
                _uiState.value = SendUiState.Error.Generic("Network error: ${e.message}")
                _events.trySend(Event.ShowMessage(R.string.no_network))
                return@launch

            } catch (_: Exception) {
                // 7. Generic error (Firebase Storage, Firestore, etc.)
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