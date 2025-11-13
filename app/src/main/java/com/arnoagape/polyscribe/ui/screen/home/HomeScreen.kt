package com.arnoagape.polyscribe.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
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
import coil.imageLoader
import coil.util.DebugLogger
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
                onClick = {
                    if (isSignedIn) {
                        onFABClick()
                    } else {
                        Toast.makeText(
                            context, context.getString(R.string.error_no_account_file),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.description_button_add)
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
                    is HomeUiState.Success ->
                        HomeContent(
                            files = (uiState as HomeUiState.Success).files,
                            onFileClick = onFileClick
                        )

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
                    Text(
                        text = stringResource(
                            R.string.created_at,
                            file.date ?: "", file.time ?: ""
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    if (file.fileUrl != null) {
                        AsyncImage(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .aspectRatio(ratio = 16 / 9f),
                            model = file.fileUrl,
                            imageLoader = LocalContext.current.imageLoader.newBuilder()
                                .logger(DebugLogger())
                                .build(),
                            placeholder = ColorPainter(Color.DarkGray),
                            contentDescription = "image",
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeContentPreview() {
    PolyscribeTheme {
        HomeContent(
            files = listOf(
                File(
                    id = "1",
                    fileUrl = "https://picsum.photos/id/85/1080/",
                    createdAt = Timestamp(0, 0),
                    date = "13/11/2025",
                    time = "10:50",
                    author = User(
                        id = "1",
                        displayName = "John Doe",
                        phoneNumber = "06 01 02 03 04",
                        email = "jdoe@mail.com",
                        isProfessional = true
                    )
                ),
                File(
                    id = "2",
                    fileUrl = "https://picsum.photos/id/85/1080/",
                    createdAt = Timestamp(0, 0),
                    date = "08/01/2025",
                    time = "21:38",
                    author = User(
                        id = "2",
                        displayName = "Harry Ter",
                        phoneNumber = "06 12 23 34 45",
                        email = "hter@mail.com",
                        isProfessional = true
                    )
                ),
                File(
                    id = "3",
                    fileUrl = "https://picsum.photos/id/85/1080/",
                    createdAt = Timestamp(0, 0),
                    date = "13/07/2025",
                    time = "19:57",
                    author = User(
                        id = "3",
                        displayName = "Emma Watt",
                        phoneNumber = "06 02 03 04 05",
                        email = "ewatt@mail.com",
                        isProfessional = false
                    )
                )
            ),
            onFileClick = {}
        )
    }
}