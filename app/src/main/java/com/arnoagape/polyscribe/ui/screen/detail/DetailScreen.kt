package com.arnoagape.polyscribe.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.dto.AuthorSnapshot
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.common.components.FilePreviewList
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.arnoagape.polyscribe.ui.utils.Format
import java.time.Instant

/**
 * Displays the detail view of a file.
 *
 * @param viewModel The ViewModel providing file data and state.
 * @param onBackClick Callback invoked when the back button is pressed.
 */
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowMessage -> {
                snackbarHostState.showSnackbar(
                    message = resources.getString(event.message),
                    duration = SnackbarDuration.Short
                )
            }

            else -> Unit
        }
    }

    DetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    state: DetailScreenState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClick: () -> Unit
) {

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_fragment_label)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
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
            when (val ui = state.uiState) {

                is DetailUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        item {
                            DetailItem(
                                file = ui.file,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }

                is DetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is DetailUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.error_generic))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    modifier: Modifier = Modifier,
    file: File
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        val (date, time) = Format.getLocalizedDateParts(file.collectDate)
        val doubleSidedText =
            if (file.doubleSided) stringResource(R.string.yes) else stringResource(R.string.no)
        val coloredText =
            if (file.colored) stringResource(R.string.yes) else stringResource(R.string.no)

        DetailCard(title = stringResource(R.string.collect_date_title)) {
            Text(stringResource(R.string.detail_collect_date, date, time))
        }

        DetailCard(title = stringResource(R.string.print_options)) {
            DetailRow(stringResource(R.string.hint_color), coloredText)
            DetailRow(stringResource(R.string.hint_double_sided), doubleSidedText)
            DetailRow(
                stringResource(R.string.hint_number_of_copies),
                file.numberOfCopies.toString()
            )
        }

        if (file.comment.isNotBlank()) {
            DetailCard(title = stringResource(R.string.detail_comments)) {
                Text(file.comment)
            }
        }

        DetailCard(title = stringResource(R.string.preview_file)) {
            FilePreviewList(
                fileUrls = file.fileUrl,
                isDetailScreen = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun DetailScreenPreview() {
    PolyscribeTheme {
        val fakeFile =
            File(
                collectDate = Instant.now(),
                author = AuthorSnapshot(
                    displayName = "John Doe",
                    phoneNumber = "06 01 02 03 04",
                    email = "jdoe@mail.com"
                ),
                colored = false,
                doubleSided = false,
                numberOfCopies = 9,
                comment = "Format A4"
            )

        val previewState = DetailScreenState(
            uiState = DetailUiState.Success(fakeFile)
        )

        DetailContent(
            state = previewState,
            onBackClick = {}
        )
    }
}