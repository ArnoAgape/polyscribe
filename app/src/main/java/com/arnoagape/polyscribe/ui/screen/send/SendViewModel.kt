package com.arnoagape.polyscribe.ui.screen.send

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
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
    private val userRepository: UserRepository,
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
            fileUrl = emptyList(),
            createdAt = Timestamp.now(),
            date = LocalDate.now().toString(),
            time = LocalTime.now().toString(),
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
    val isFileValid = file
        .map { currentFile ->
            currentFile.fileUrl.isNotEmpty()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        viewModelScope.launch {
            _user.value = userRepository.getCurrentUser()
        }
    }

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

            is FormEvent.NumberOfCopiesSet -> {
                _file.update { file ->
                    file.copy(numberOfCopies = formEvent.value.coerceAtLeast(1))
                }
            }

            is FormEvent.AddFile ->
                _file.update { file ->
                    file.copy(
                        fileUrl = file.fileUrl.plus(formEvent.uri.toString())
                    )
                }

            is FormEvent.RemoveFile ->
                _file.update { file ->
                    file.copy(
                        fileUrl = file.fileUrl.minus(formEvent.uri.toString())
                    )
                }

            is FormEvent.CommentChanged -> {
                _file.update { it.copy(comment = formEvent.comment) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendFile() {
        viewModelScope.launch {

            // 🔹 1. Vérification réseau
            if (!networkUtils.isNetworkAvailable()) {
                _events.trySend(Event.ShowSnackBar(R.string.no_network))
                return@launch
            }

            // 🔹 2. Vérification utilisateur connecté
            val currentUser = _user.value
            if (currentUser == null) {
                _uiState.value = SendUiState.Error.NoAccount()
                _events.trySend(Event.ShowSnackBar(R.string.error_no_account_file))
                return@launch
            }

            _uiState.value = SendUiState.Loading

            try {
                // 🔹 3. Création d’un file complet avec l’auteur
                val fileToSave = _file.value.copy(author = currentUser)

                // 🔹 4. Upload Storage + Firestore
                fileRepository.sendFile(fileToSave)

                // 🔹 5. Succès UI
                _uiState.value = SendUiState.Success(fileToSave)
                _events.trySend(Event.ShowSnackBar(R.string.success_file))

            } catch (e: IOException) {
                // 🔹 6. Erreur réseau (upload impossible)
                _uiState.value = SendUiState.Error.Generic("Network error: ${e.message}")
                _events.trySend(Event.ShowSnackBar(R.string.no_network))

            } catch (e: Exception) {
                // 🔹 7. Erreur générique (Firebase Storage, Firestore, etc.)
                _uiState.value = SendUiState.Error.Generic()
                _events.trySend(Event.ShowSnackBar(R.string.error_generic))
            }
        }
    }
}