package com.arnoagape.polyscribe.ui.screen.home

import app.cash.turbine.test
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var fileRepo: FileRepository
    private lateinit var userRepo: UserRepository

    private lateinit var fakeNetwork: NetworkUtils
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        fileRepo = mockk(relaxed = true)
        userRepo = mockk(relaxed = true)
        fakeNetwork = mockk(relaxed = true)

        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1")
        every { fakeNetwork.checkNetwork(any(), any()) } returns Unit

    }

    @Test
    fun `uiState is Success when repository returns files`() = runTest {
        val fakeFiles = listOf(TestUtils.fakeFile("1"))
        every { fileRepo.files } returns flowOf(fakeFiles)

        viewModel = HomeViewModel(fileRepo, fakeNetwork)

        viewModel.uiState.test {
            val latest = expectMostRecentItem()
            assertTrue("Expected Success but was $latest", latest is HomeUiState.Success)
            assertEquals(fakeFiles, (latest as HomeUiState.Success).files)
        }
    }

    @Test
    fun `uiState is Error_Empty when repository returns empty list`() = runTest {
        fileRepo = mockkClass(FileRepository::class)
        every { fileRepo.files } returns flowOf(emptyList())

        viewModel = HomeViewModel(fileRepo, fakeNetwork)

        viewModel.uiState.test {
            val latest = expectMostRecentItem()
            assertTrue("Expected Error.Empty but was $latest", latest is HomeUiState.Error.Empty)
        }
    }

    @Test
    fun `uiState becomes Error_Generic when repository flow throws exception`() = runTest {
        // Arrange
        val errorFlow = flow<List<File>> { throw Exception("Database failed") }

        fileRepo = mockk(relaxed = true)
        every { fileRepo.files } returns errorFlow
        every { userRepo.isUserSignedIn() } returns flowOf(true)
        every { fakeNetwork.isNetworkAvailable() } returns true

        // Act
        viewModel = HomeViewModel(fileRepo, fakeNetwork)

        // Assert
        viewModel.uiState.test {
            val latest = expectMostRecentItem()
            assertTrue(
                "Expected Error.Generic but was $latest",
                latest is HomeUiState.Error.Generic
            )
            assertEquals("Database failed", (latest as HomeUiState.Error.Generic).message)
        }
    }

    @Test
    fun `refreshFiles sends ShowMessage event when no network`() = runTest {

        every { fakeNetwork.checkNetwork(any(), any()) } answers {
            val events = secondArg<Channel<Event>>()
            events.trySend(Event.ShowMessage(R.string.no_network))
        }

        every { fileRepo.files } returns flowOf(emptyList())

        viewModel = HomeViewModel(fileRepo, fakeNetwork)

        viewModel.eventsFlow.test {
            awaitItem()
            viewModel.refreshFiles()
            val event = awaitItem()
            assertTrue(event is Event.ShowMessage)
            assertEquals(R.string.no_network, (event as Event.ShowMessage).message)
        }
    }

}