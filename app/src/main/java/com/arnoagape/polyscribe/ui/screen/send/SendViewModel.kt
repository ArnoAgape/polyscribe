package com.arnoagape.polyscribe.ui.screen.send

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<SendUiState>(SendUiState.Idle)
    val uiState: StateFlow<SendUiState> = _uiState.asStateFlow()
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    private val _events = Channel<Event>()
    val eventsFlow = _events.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _file = MutableStateFlow(
        File(
            id = UUID.randomUUID().toString(),
            fileUrl = null,
            photoUrl = null,
            createdAt = Timestamp.now(),
            date = LocalDate.now(),
            time = LocalTime.now(),
            author = null,
            isColored = false,
            isDoubleSided = false,
            numberOfCopies = 1,
            comment = "",
        )
    )

    /**
     * Public state flow representing the current post being edited.
     * This is immutable for consumers.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    val file: StateFlow<File> = _file.asStateFlow()

    /**
     * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    val isFileValid = file.map { currentFile ->
        currentFile.photoUrl != null && currentFile.fileUrl != null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun onAction(formEvent: FormEvent) {
        when (formEvent) {
            is FormEvent.DateChanged -> {
                _file.update { it.copy(date = formEvent.date) }
            }

            is FormEvent.TimeChanged -> {
                _file.update { it.copy(time = formEvent.time) }
            }

            is FormEvent.ColorChanged -> {
                _file.update { it.copy(isColored = formEvent.isColored) }
            }

            is FormEvent.DoubleSidedChanged -> {
                _file.update { it.copy(isDoubleSided = formEvent.isDoubleSided) }
            }

            is FormEvent.NumberOfCopiesChanged -> {
                _file.update { file ->
                    file.copy(
                        numberOfCopies = (file.numberOfCopies + formEvent.delta).coerceAtLeast(1)
                    )
                }
            }

            is FormEvent.PhotoChanged -> {
                _file.update { it.copy(photoUrl = formEvent.photoUrl.toString()) }
            }

            is FormEvent.FileChanged -> {
                _file.update { it.copy(fileUrl = formEvent.fileUrl.toString()) }
            }

            is FormEvent.CommentChanged -> {
                _file.update { it.copy(comment = formEvent.comment) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addFile() {
        viewModelScope.launch {
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowSnackBar(R.string.no_network))
                return@launch
            }

            _uiState.value = SendUiState.Loading

            try {
                val fileToSave = _file.value.copy(author = _user.value)

                fileRepository.sendFile(fileToSave)

                _uiState.value = SendUiState.Success(fileToSave)
                _events.trySend(Event.ShowSnackBar(R.string.file_success))

            } catch (e: Exception) {
                when (e) {
                    is IllegalStateException -> {
                        _uiState.value = SendUiState.Error.NoAccount()
                    }

                    is IOException -> {
                        _uiState.value = SendUiState.Error.Generic("Network error: ${e.message}")
                        _events.trySend(Event.ShowSnackBar(R.string.no_network))
                    }

                    else -> {
                        _uiState.value = SendUiState.Error.Generic()
                        _events.trySend(Event.ShowSnackBar(R.string.error_generic))
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSaveClicked() {
        val file = _file.value
        if (file.fileUrl == null || file.photoUrl == null) {
            _events.trySend(Event.ShowSnackBar(R.string.error_no_file))
        } else {
            addFile()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleColoration() {
        _file.update { file ->
            file.copy(isColored = !file.isColored)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleDoubleSided() {
        _file.update { file ->
            file.copy(isDoubleSided = !file.isDoubleSided)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeNumberOfCopies(delta: Int) {
        _file.update { file ->
            val current = file.numberOfCopies
            val newValue = (current + delta).coerceAtLeast(1)
            file.copy(numberOfCopies = newValue)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateDate() {
        _file.update { file ->
            file.copy(date = file.date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTime() {
        _file.update { file ->
            file.copy(time = file.time)
        }
    }
}