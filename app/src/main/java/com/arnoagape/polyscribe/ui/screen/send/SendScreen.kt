package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.common.components.FileRowItem
import com.arnoagape.polyscribe.ui.common.components.TextRowItem
import com.arnoagape.polyscribe.ui.screen.send.fields.DateTimeField
import com.arnoagape.polyscribe.ui.screen.send.fields.NumberOfCopiesField
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.arnoagape.polyscribe.ui.utils.getFileName
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    viewModel: SendViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowSnackBar -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                onSaveClick()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.send_fragment_label)) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->

        when (state.uiState) {
            is SendUiState.Idle, is SendUiState.Success -> {
                val fileToDisplay =
                    if (state.uiState is SendUiState.Success) (state.uiState as SendUiState.Success).file
                    else state.file

                CreateFile(
                    contentPadding = contentPadding,
                    fileUrls = fileToDisplay.fileUrl,
                    onAddFile = { viewModel.onAction(FormEvent.AddFile(it)) },
                    onRemoveFile = { viewModel.onAction(FormEvent.RemoveFile(it)) },
                    pictureUrls = emptyList(),
                    onAddPicture = { viewModel.onAction(FormEvent.AddFile(it)) },
                    onRemovePicture = {},
                    dateTime = fileToDisplay.dateTime,
                    onDateTimeChange = { viewModel.onAction(FormEvent.DateTimeChanged(it)) },
                    colored = fileToDisplay.isColored,
                    onColorationChange = { viewModel.onAction(FormEvent.ColorChanged(it)) },
                    doubleSided = fileToDisplay.isDoubleSided,
                    onDoubleSidedChange = { viewModel.onAction(FormEvent.DoubleSidedChanged(it)) },
                    numberOfCopies = fileToDisplay.numberOfCopies,
                    onNumberOfCopiesChange = { newValue ->
                        viewModel.onAction(FormEvent.NumberOfCopiesSet(newValue))
                    },
                    comments = fileToDisplay.comment,
                    onCommentsChanged = { viewModel.onAction(FormEvent.CommentChanged(it)) },
                    onSaveClicked = { viewModel.sendFile() },
                    isFileValid = state.isValid,
                    isLoading = false
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
                            text = stringResource(R.string.publishing),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateFile(
    contentPadding: PaddingValues = PaddingValues(),
    fileUrls: List<String>,
    onAddFile: (Uri) -> Unit,
    onRemoveFile: (Uri) -> Unit,
    pictureUrls: List<String>,
    onAddPicture: (Uri) -> Unit,
    onRemovePicture: (Uri) -> Unit,
    dateTime: Instant,
    onDateTimeChange: (Instant) -> Unit,
    colored: Boolean,
    onColorationChange: (Boolean) -> Unit,
    doubleSided: Boolean,
    onDoubleSidedChange: (Boolean) -> Unit,
    numberOfCopies: Int,
    onNumberOfCopiesChange: (Int) -> Unit,
    comments: String,
    onCommentsChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    isFileValid: Boolean,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val selectedFileUris = fileUrls.map { it.toUri() }

    val pictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { onAddFile(it) }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { onAddFile(it) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /** ---------- SCROLLABLE FORM CONTENT ---------- **/
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /** ---------- DATE & TIME ---------- **/
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DateTimeField(
                        modifier = Modifier.fillMaxWidth(),
                        value = dateTime,
                        onValueChange = onDateTimeChange,
                        label = stringResource(R.string.hint_datetime)
                    )
                }

                /** ---------- COLORATION ---------- **/
                TextRowItem(
                    textRes = R.string.hint_color,
                    trailingContent = {
                        Switch(
                            checked = colored,
                            onCheckedChange = onColorationChange
                        )
                    }
                )

                /** ---------- DOUBLE SIDED ---------- **/
                TextRowItem(
                    textRes = R.string.hint_double_sided,
                    trailingContent = {
                        Switch(
                            checked = doubleSided,
                            onCheckedChange = onDoubleSidedChange
                        )
                    }
                )

                /** ---------- NUMBER OF COPIES ---------- **/

                NumberOfCopiesField(
                    numberOfCopies = numberOfCopies,
                    onNumberOfCopiesChange = onNumberOfCopiesChange
                )

                /** ---------- COMMENTS ---------- **/
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = comments,
                    onValueChange = onCommentsChanged,
                    label = { Text(stringResource(id = R.string.hint_comments)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }

            /** ---------- ADD BUTTONS FILE ---------- **/
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        fileLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.oasis.opendocument.text",
                                "text/plain"
                            )
                        )
                    }
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.add_file),
                        maxLines = 1
                    )
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { pictureLauncher.launch("image/*") }
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.add_picture),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            /** ---------- FILES LISTED ONE AFTER ANOTHER ---------- **/
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedFileUris.forEach { uri ->
                    FileRowItem(
                        fileName = context.getFileName(uri),
                        icon = Icons.Default.AttachFile,
                        onRemove = { onRemoveFile(uri) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))


            /** ---------- SEND BUTTON ---------- **/
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSaveClicked,
                enabled = isFileValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(id = R.string.action_send))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CreateFilePreview() {
    PolyscribeTheme {
        CreateFile(
            fileUrls = listOf("content://com.example.provider/document/resume.pdf"),
            onAddFile = {},
            onRemoveFile = {},
            pictureUrls = listOf(
                "content://com.example.provider/images/photo.jpg"
            ),
            onAddPicture = {},
            onRemovePicture = {},
            dateTime = Instant.EPOCH,
            onDateTimeChange = {},
            colored = false,
            onColorationChange = {},
            doubleSided = false,
            onDoubleSidedChange = {},
            numberOfCopies = 1,
            onNumberOfCopiesChange = {},
            comments = "I love Polyscribe!",
            onCommentsChanged = {},
            onSaveClicked = {},
            isFileValid = true,
            isLoading = false
        )
    }
}