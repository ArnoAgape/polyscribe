package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    viewModel: SendViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val post by viewModel.file.collectAsStateWithLifecycle()
    val isFileValid by viewModel.isFileValid.collectAsStateWithLifecycle()
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

        when (uiState) {

            is SendUiState.Idle, is SendUiState.Success -> {
                val fileToDisplay =
                    if (uiState is SendUiState.Success) (uiState as SendUiState.Success).file else post

                CreateFile(
                    contentPadding = contentPadding,
                    fileUrl = fileToDisplay.fileUrl,
                    onFileSelected = { viewModel.onAction(FormEvent.FileChanged(it)) },
                    photoUrl = fileToDisplay.photoUrl,
                    onPhotoSelected = { viewModel.onAction(FormEvent.PhotoChanged(it)) },
                    date = fileToDisplay.date,
                    onDateChange = { viewModel.onAction(FormEvent.DateChanged(it)) },
                    time = fileToDisplay.time,
                    onTimeChange = { viewModel.onAction(FormEvent.TimeChanged(it)) },
                    colored = fileToDisplay.isColored,
                    onColorationChange = { viewModel.onAction(FormEvent.ColorChanged(it)) },
                    doubleSided = fileToDisplay.isDoubleSided,
                    onDoubleSidedChange = { viewModel.onAction(FormEvent.DoubleSidedChanged(it)) },
                    numberOfCopies = fileToDisplay.numberOfCopies,
                    onNumberOfCopiesChange = { delta -> viewModel.onAction(FormEvent.NumberOfCopiesChanged(delta)) },
                    comments = fileToDisplay.comment,
                    onCommentsChanged = { viewModel.onAction(FormEvent.CommentChanged(it)) },
                    onSaveClicked = { viewModel.onSaveClicked() },
                    isFileValid = isFileValid,
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
                val errorState = uiState as SendUiState.Error
                val message = when (errorState) {
                    is SendUiState.Error.NoAccount -> (uiState as SendUiState.Error.NoAccount).message
                    is SendUiState.Error.Generic -> (uiState as SendUiState.Error.Generic).message
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
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateField(
    modifier: Modifier,
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val displayText = value?.format(formatter) ?: ""

    OutlinedTextField(
        label = { Text(label) },
        value = displayText,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.DateRange, null) },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { showDialog = true }
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        onValueChange(date)
                    }
                    showDialog = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeField(
    modifier: Modifier,
    value: LocalTime?,
    onValueChange: (LocalTime?) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val displayText = value?.format(formatter) ?: ""

    OutlinedTextField(
        label = { Text(label) },
        value = displayText,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.AccessTime, null) },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { showDialog = true }
    )

    if (showDialog) {
        val state = rememberTimePickerState()

        TimePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val picked = LocalTime.of(state.hour, state.minute)
                    onValueChange(picked)
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            },
            title = { Text(stringResource(R.string.select_time)) }
        ) {
            TimePicker(state = state)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CreateFile(
    contentPadding: PaddingValues = PaddingValues(),
    fileUrl: String?,
    onFileSelected: (Uri?) -> Unit,
    photoUrl: String?,
    onPhotoSelected: (Uri?) -> Unit,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    time: LocalTime?,
    onTimeChange: (LocalTime) -> Unit,
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
    val scrollState = rememberScrollState()
    val selectedFileUri = fileUrl?.toUri()
    val selectedPhotoUri = photoUrl?.toUri()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onFileSelected(uri)
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
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .verticalScroll(scrollState)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateField(
                        modifier = Modifier.weight(1f),
                        value = date,
                        onValueChange = { onDateChange },
                        label = stringResource(id = R.string.hint_date)
                    )

                    TimeField(
                        modifier = Modifier.weight(1f),
                        value = time,
                        onValueChange = { onTimeChange },
                        label = stringResource(id = R.string.hint_time)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Coloration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(id = R.string.hint_color))
                    Switch(
                        checked = colored,
                        onCheckedChange = onColorationChange
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Double sided
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(id = R.string.hint_double_sided))
                    Switch(
                        checked = doubleSided,
                        onCheckedChange = onDoubleSidedChange
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Number of copies
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(id = R.string.hint_number_of_copies))
                    IconButton(
                        onClick = { onNumberOfCopiesChange(-1) },
                        enabled = numberOfCopies > 1
                    ) {
                        Text("-", style = MaterialTheme.typography.headlineSmall)
                    }

                    Text(
                        text = numberOfCopies.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(onClick = { onNumberOfCopiesChange(+1) }) {
                        Text("+", style = MaterialTheme.typography.headlineSmall)
                    }
                }

                // Comments
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    value = comments,
                    onValueChange = { onCommentsChanged(it) },
                    label = { Text(stringResource(id = R.string.hint_comments)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                Spacer(Modifier.height(16.dp))

                // 🖼️ Photo & File picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // File picker
                    if (selectedFileUri != null) {
                        AsyncImage(
                            model = selectedFileUri,
                            contentDescription = stringResource(R.string.preview_file),
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (selectedFileUri == null)
                                stringResource(R.string.select_file)
                            else
                                stringResource(R.string.change_file),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Photo picker
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription = stringResource(R.string.preview_image),
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (selectedPhotoUri == null)
                                stringResource(R.string.select_image)
                            else
                                stringResource(R.string.change_image),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Button(
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

@RequiresApi(Build.VERSION_CODES.O)
@PreviewLightDark
@Composable
private fun CreateFilePreview() {
    PolyscribeTheme {
        CreateFile(
            fileUrl = null,
            onFileSelected = {},
            photoUrl = null,
            onPhotoSelected = {},
            date = null,
            onDateChange = {},
            time = null,
            onTimeChange = {},
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