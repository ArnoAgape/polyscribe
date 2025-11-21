package com.arnoagape.polyscribe.ui.screen.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.EventsEffect
import com.arnoagape.polyscribe.ui.common.components.FilePreviewList
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme
import com.arnoagape.polyscribe.ui.utils.Format
import com.google.firebase.Timestamp
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val refreshState = rememberPullToRefreshState()

    EventsEffect(viewModel.eventsFlow) { event ->
        when (event) {
            is Event.ShowSnackBar -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            state = refreshState,
            isRefreshing = state.uiState is DetailUiState.Loading,
            onRefresh = { viewModel.refreshData() }
        ) {
            if (state.uiState is DetailUiState.Success) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    item {
                        DetailContent(
                            file = (state.uiState as DetailUiState.Success).file,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    modifier: Modifier,
    file: File
) {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ---- AUTHOR ----
            file.author?.displayName?.let {
                Text(
                    text = stringResource(
                        R.string.by,
                        file.author.displayName
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier.height(12.dp))

            // ---- COLLECT DATE/TIME ----
            val (date, time) = Format.getLocalizedDateParts(file.dateTime)

            Text(stringResource(R.string.detail_collect_date, date, time))

            // ---- COLOR DETAILS ----
            val coloredText = if (file.colored) {
                stringResource(R.string.yes)
            } else {
                stringResource(R.string.no)
            }
            Text(
                text = stringResource(
                    R.string.detail_color,
                    coloredText
                )
            )

            // ---- DOUBLE SIDED DETAILS ----
            val doubleSidedText = if (file.doubleSided) {
                stringResource(R.string.yes)
            } else {
                stringResource(R.string.no)
            }
            Text(
                text = stringResource(
                    R.string.detail_double_sided,
                    doubleSidedText
                )
            )

            // ---- NUMBER OF COPIES DETAILS ----
            Text(
                text = stringResource(
                    R.string.detail_number_of_copies,
                    file.numberOfCopies
                )
            )

            // ---- COMMENTS DETAILS ----
            Text(
                text = stringResource(
                    R.string.detail_comments,
                    file.comment
                )
            )

            // ---- FILE PREVIEW ----
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
private fun PostScreenPreview() {
    PolyscribeTheme {
        DetailContent(
            file = File(
                id = "1",
                fileUrl = emptyList(),
                createdAt = Timestamp(1233356000, 212120),
                dateTime = Instant.now(),
                author = User(
                    id = "1",
                    displayName = "John Doe",
                    phoneNumber = "06 01 02 03 04",
                    email = "jdoe@mail.com",
                    isProfessional = true
                ),
                colored = false,
                doubleSided = false,
                numberOfCopies = 1,
                comment = ""
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}