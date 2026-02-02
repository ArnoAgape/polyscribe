package com.arnoagape.polyscribe.ui.screen.home

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val fileRepo: FileRepository = mockk()
    private val userRepo: UserRepository = mockk()
    private val networkUtils: NetworkUtils = mockk()

    private fun createViewModel(): HomeViewModel {
        every { networkUtils.checkNetwork(networkUtils, any()) } returns Unit
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        return HomeViewModel(
            fileRepository = fileRepo,
            userRepository = userRepo,
            networkUtils = networkUtils,
        )
    }

    @Test
    fun `uiState is Success when repository returns files`() = runTest {
        val fakeFiles = listOf(TestUtils.fakeFile("1"))
        every { fileRepo.observeFiles() } returns flowOf(fakeFiles)

        val viewModel = createViewModel()

        viewModel.state.test {
            val success = awaitItem()
            val files = (success.uiState as HomeUiState.Success).files

            assertThat(files)
        }
    }

    @Test
    fun `uiState is Error_Empty when repository returns empty list`() = runTest {
        every { fileRepo.observeFiles() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.state.test {
            val empty = awaitItem()
            assertThat(empty.uiState).isEqualTo(HomeUiState.Error.Empty())
        }
    }

    @Test
    fun `uiState becomes Error_Generic when repository flow throws exception`() = runTest {
        coEvery { fileRepo.observeFiles() } returns flow {
            delay(1)
            throw RuntimeException("boom")
        }

        val viewModel = createViewModel()

        viewModel.state.test {
            val loading = awaitItem()
            assertThat(loading.uiState).isEqualTo(HomeUiState.Loading)

            val error = awaitItem()
            assertThat(error.uiState)
                .isEqualTo(HomeUiState.Error.Generic("boom"))
        }
    }

    @Test
    fun `refreshFiles emits no network message when offline`() = runTest {
        val viewModel = createViewModel()

        every { networkUtils.checkNetwork(any(), any()) } answers {
            val events = secondArg<Channel<Event>>()
            events.trySend(Event.ShowMessage(R.string.no_network))
        }

        viewModel.eventsFlow.test {
            viewModel.refreshFiles()

            val event = awaitItem()
            assertThat(event).isInstanceOf(Event.ShowMessage::class.java)

            val offlineEvent = event as Event.ShowMessage
            assertThat(offlineEvent.message).isEqualTo(R.string.no_network)
        }
    }

}