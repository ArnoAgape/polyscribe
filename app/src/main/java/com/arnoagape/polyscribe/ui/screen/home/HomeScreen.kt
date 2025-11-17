package com.arnoagape.polyscribe.ui.screen.home

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onFileClick: (File) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFABClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isUserSignedIn.collectAsStateWithLifecycle()
    val refreshState = rememberPullToRefreshState()

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowSnackBar -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onFABClick() },
                /*if (isSignedIn) {
                    onFABClick()
                } else {
                    Toast.makeText(
                        context, context.getString(R.string.error_no_account_file),
                        Toast.LENGTH_SHORT
                    ).show()
                }*/
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.contentDescription_button_add)
                )
            }
        }
    ) { contentPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            state = refreshState,
            isRefreshing = uiState is HomeUiState.Loading,
            onRefresh = { viewModel.refreshPosts() }
        ) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState) {
                    is HomeUiState.Idle, is HomeUiState.Success ->
                        HomeContent(
                            files = (uiState as HomeUiState.Success).files,
                            onFileClick = onFileClick
                        )

                    is HomeUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is HomeUiState.Error.Empty -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.no_files),
                                modifier = Modifier.fillMaxSize(),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    files: List<File>,
    onFileClick: (File) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(files) { file ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onFileClick(file) }
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {

                    // ---- TITRE DATE/HEURE ----
                    Text(
                        text = stringResource(
                            R.string.created_at,
                            file.date,
                            file.time
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    // ---- VIGNETTE DU DOCUMENT ----
                    val documentUrl = file.fileUrl.firstOrNull()

                    if (documentUrl != null) {

                        val isImage = documentUrl.endsWith(".jpg", true) ||
                                documentUrl.endsWith(".jpeg", true) ||
                                documentUrl.endsWith(".png", true)

                        when {
                            isImage -> {
                                AsyncImage(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .aspectRatio(16 / 9f),
                                    model = documentUrl,
                                    placeholder = ColorPainter(Color.DarkGray),
                                    contentDescription = "Image preview",
                                    contentScale = ContentScale.Crop
                                )
                            }

                            documentUrl.endsWith(".pdf", true) -> {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF",
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .fillMaxWidth()
                                        .height(60.dp)
                                )
                            }

                            else -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = "Document",
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .fillMaxWidth()
                                        .height(60.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@PreviewLightDark
@Composable
private fun HomeContentPreview() {
    PolyscribeTheme {
        HomeContent(
            files = listOf(
                File(
                    id = "1",
                    fileUrl = emptyList(),
                    createdAt = Timestamp(0, 0),
                    date = "10/11/2025",
                    time = "11:04",
                    author = User(
                        id = "1",
                        displayName = "John Doe",
                        phoneNumber = "06 01 02 03 04",
                        email = "jdoe@mail.com",
                        isProfessional = true
                    ),
                    isColored = false,
                    isDoubleSided = false,
                    numberOfCopies = 1,
                    comment = ""
                ),
                File(
                    id = "2",
                    fileUrl = emptyList(),
                    createdAt = Timestamp(0, 0),
                    date = "23/04/1993",
                    time = "22:44",
                    author = User(
                        id = "2",
                        displayName = "Harry Ter",
                        phoneNumber = "06 12 23 34 45",
                        email = "hter@mail.com",
                        isProfessional = true
                    ),
                    isColored = false,
                    isDoubleSided = false,
                    numberOfCopies = 1,
                    comment = "null"
                ),
                File(
                    id = "3",
                    fileUrl = emptyList(),
                    createdAt = Timestamp(0, 0),
                    date = "01/05/1968",
                    time = "14:00",
                    author = User(
                        id = "3",
                        displayName = "Emma Watt",
                        phoneNumber = "06 02 03 04 05",
                        email = "ewatt@mail.com",
                        isProfessional = false
                    ),
                    isColored = false,
                    isDoubleSided = false,
                    numberOfCopies = 1,
                    comment = "null"
                )
            ),
            onFileClick = {}
        )
    }
}