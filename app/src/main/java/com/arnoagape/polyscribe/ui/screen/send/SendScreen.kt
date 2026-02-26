package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.domain.model.SessionType
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.utils.SharedFilesHolder
import com.arnoagape.polyscribe.ui.screen.send.sections.SendActionsSection
import com.arnoagape.polyscribe.ui.screen.send.sections.SendCommentsSection
import com.arnoagape.polyscribe.ui.screen.send.sections.SendDateTimeSection
import com.arnoagape.polyscribe.ui.screen.send.sections.SendFilesSection
import com.arnoagape.polyscribe.ui.screen.send.sections.SendPrintOptionsSection
import com.arnoagape.polyscribe.ui.screen.send.sections.SendSubmitSection
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import java.time.Instant

/**
 * Displays the screen used for uploading and configuring a new file.
 *
 * @param viewModel ViewModel providing the file state and actions.
 * @param onBackClick Callback invoked when the back button is pressed.
 * @param onSaveClick Callback invoked after a successful upload.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    viewModel: SendViewModel,
    onBackClick: () -> Unit,
    onSaveClick: (SessionType) -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    val pictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.onAction(FormEvent.AddFile(uri))
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.onAction(FormEvent.AddFile(uri))
        }
    }

    LaunchedEffect(Unit) {
        val sharedUris = SharedFilesHolder.consume()
        sharedUris.forEach { uri ->
            viewModel.onAction(FormEvent.AddFile(uri))
        }
    }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    actionLabel = resources.getString(R.string.try_again),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onSaveButton {
                        viewModel.sendFile()
                    }
                }
            }

            is Event.ErrorUploadFiles -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
            }

            is Event.FileSentSuccessfully -> {
                Toast.makeText(
                    context,
                    R.string.success_file,
                    Toast.LENGTH_SHORT
                ).show()
                onSaveClick(event.sessionType)
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.send_fragment_label))
                },
                navigationIcon = {
                    if (state.isSignedIn == true) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(
                                    id = R.string.contentDescription_go_back
                                )
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            SendSubmitSection(
                isFileValid = state.isValid,
                isLoading = state.uiState is SendUiState.Loading,
                onSaveClicked = {
                    viewModel.onSaveButton {
                        viewModel.sendFile()
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when (state.uiState) {
                is SendUiState.Idle, is SendUiState.Success -> {
                    val fileToDisplay =
                        if (state.uiState is SendUiState.Success) (state.uiState as SendUiState.Success).file
                        else state.file

                    SendContent(
                        localUris = state.localUris,
                        onAddFileClick = {
                            fileLauncher.launch(arrayOf("*/*"))
                        },
                        onAddPictureClick = { pictureLauncher.launch("image/*") },
                        onRemoveFile = { viewModel.onAction(FormEvent.RemoveFile(it)) },
                        dateTime = fileToDisplay.collectDate,
                        onDateTimeChange = { viewModel.onAction(FormEvent.DateTimeChanged(it)) },
                        colored = fileToDisplay.colored,
                        onColorationChange = { viewModel.onAction(FormEvent.ColorChanged(it)) },
                        doubleSided = fileToDisplay.doubleSided,
                        onDoubleSidedChange = { viewModel.onAction(FormEvent.DoubleSidedChanged(it)) },
                        numberOfCopies = fileToDisplay.numberOfCopies,
                        onNumberOfCopiesChange = { newValue ->
                            viewModel.onAction(FormEvent.NumberOfCopiesSet(newValue))
                        },
                        comments = fileToDisplay.comment,
                        onCommentsChanged = { viewModel.onAction(FormEvent.CommentChanged(it)) }
                    )
                }

                is SendUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.sending),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is SendUiState.Error -> {
                    val errorState = state.uiState as SendUiState.Error
                    val message = when (errorState) {
                        is SendUiState.Error.NoAccount -> (state.uiState as SendUiState.Error.NoAccount).message
                        is SendUiState.Error.Generic -> (state.uiState as SendUiState.Error.Generic).message
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun SendContent(
    localUris: List<Uri>,
    dateTime: Instant,
    colored: Boolean,
    doubleSided: Boolean,
    numberOfCopies: Int,
    comments: String,
    onDateTimeChange: (Instant) -> Unit,
    onColorationChange: (Boolean) -> Unit,
    onDoubleSidedChange: (Boolean) -> Unit,
    onNumberOfCopiesChange: (Int) -> Unit,
    onCommentsChanged: (String) -> Unit,
    onAddFileClick: () -> Unit,
    onAddPictureClick: () -> Unit,
    onRemoveFile: (Uri) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(bottom = SendDimens.BottomCtaHeight)
            .padding(SendDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SendDimens.SectionSpacing)
    ) {

        SendDateTimeSection(dateTime, onDateTimeChange)

        SendPrintOptionsSection(
            colored = colored,
            onColorationChange = onColorationChange,
            doubleSided = doubleSided,
            onDoubleSidedChange = onDoubleSidedChange,
            numberOfCopies = numberOfCopies,
            onNumberOfCopiesChange = onNumberOfCopiesChange
        )

        SendCommentsSection(
            comments = comments,
            onCommentsChanged = onCommentsChanged
        )

        SendActionsSection(
            onAddFileClick = onAddFileClick,
            onAddPictureClick = onAddPictureClick
        )

        SendFilesSection(
            uris = localUris,
            onRemoveFile = onRemoveFile
        )
    }
}

@PreviewLightDark
@Composable
private fun SendContentPreview() {
    PolyscribeTheme {
        SendContent(
            localUris = listOf("content://com.example.provider/document/resume.pdf").map { it.toUri() },
            onAddFileClick = {},
            onAddPictureClick = {},
            onRemoveFile = {},
            dateTime = Instant.EPOCH,
            onDateTimeChange = {},
            colored = false,
            onColorationChange = {},
            doubleSided = false,
            onDoubleSidedChange = {},
            numberOfCopies = 1,
            onNumberOfCopiesChange = {},
            comments = "I love Polyscribe!",
            onCommentsChanged = {}
        )
    }
}