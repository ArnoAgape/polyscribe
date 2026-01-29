package com.arnoagape.polyscribe.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.common.components.FilePreviewList
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.arnoagape.polyscribe.ui.utils.Format
import java.time.Instant

/**
 * Displays the home screen showing the list of uploaded files.
 *
 * @param viewModel ViewModel providing file data and UI state.
 * @param onFileClick Callback invoked when a file is selected.
 * @param onFABClick Callback invoked when the FAB is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onFileClick: (File) -> Unit,
    onFABClick: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

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
                    viewModel.refreshFiles()
                }
            }

            is Event.ShowSuccessMessage -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message)
                )
            }
        }
    }

    HomeContent(
        state = state,
        isSignedIn = isSignedIn,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.refreshFiles() },
        onFABClick = onFABClick,
        onFileClick = onFileClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeScreenState,
    isSignedIn: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRefresh: () -> Unit,
    onFABClick: () -> Unit,
    onFileClick: (File) -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    val resources = LocalResources.current

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.home))
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isSignedIn) onFABClick()
                    else Toast.makeText(
                        context,
                        resources.getString(R.string.error_no_account_file),
                        Toast.LENGTH_SHORT
                    ).show()
                },
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
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh
        ) {
            when (val ui = state.uiState) {
                is HomeUiState.Success ->
                    HomeFile(
                        files = ui.files,
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun HomeFile(
    modifier: Modifier = Modifier,
    files: List<File>,
    onFileClick: (File) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
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

                    /** ---------- DATE/TIME ---------- **/
                    val (date, time) = Format.getLocalizedDateParts(file.createdAt)

                    Text(stringResource(R.string.sent_at, date, time))

                    Spacer(Modifier.height(8.dp))

                    /** ---------- FILE PREVIEW ---------- **/
                    FilePreviewList(
                        fileUrls = file.fileUrl,
                        isDetailScreen = false,
                        onClick = { onFileClick(file) }
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeContentPreview() {
    PolyscribeTheme {
        val fakeFiles = listOf(
            File(dateTime = Instant.now()),
            File(dateTime = Instant.now()),
            File(dateTime = Instant.now())
        )

        val previewState = HomeScreenState(
            uiState = HomeUiState.Success(fakeFiles),
            isRefreshing = false
        )
        HomeContent(
            state = previewState,
            onFileClick = {},
            isSignedIn = true,
            onRefresh = {},
            onFABClick = {}
        )
    }
}