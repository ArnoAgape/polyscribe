package com.arnoagape.polyscribe.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
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
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var fileRepo: FileRepository
    private lateinit var userRepo: UserRepository

    private lateinit var fakeNetwork: NetworkUtils
    private lateinit var viewModel: DetailViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        fileRepo = mockk()
        userRepo = mockk(relaxed = true)
        fakeNetwork = mockk()

        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1")
        every { fakeNetwork.checkNetwork(any(), any()) } returns Unit

        savedStateHandle = SavedStateHandle().apply {
            set("fileId", "123")
        }
        every { userRepo.isUserSignedIn() } returns flowOf(true)

    }

    fun createViewModel() {
        viewModel = DetailViewModel(
            fileRepository = fileRepo,
            userRepository = userRepo,
            savedStateHandle = savedStateHandle,
            networkUtils = fakeNetwork
        )
    }

    @Test
    fun `observeFile sets Loading first`() = runTest {

        coEvery { fileRepo.getFileById("123") } returns flow {
            delay(1)
            emit(TestUtils.fakeFile(id = "123"))
        }

        createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is DetailUiState.Loading)
            assertTrue(awaitItem() is DetailUiState.Success)
        }
    }

    @Test
    fun `observeFile emits Error_Empty when file is null`() = runTest {

        coEvery { fileRepo.getFileById("123") } returns flow {
            delay(1)
            emit(null)
        }

        createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is DetailUiState.Loading)
            val second = awaitItem()
            assertTrue(second is DetailUiState.Error.Empty)
        }
    }

    @Test
    fun `observeFile emits Generic error when exception thrown`() = runTest {

        coEvery { fileRepo.getFileById("123") } returns flow {
            delay(1)
            throw RuntimeException("boom")
        }

        createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is DetailUiState.Loading)
            val second = awaitItem()
            assertTrue(second is DetailUiState.Error.Generic)
        }
    }

    @Test
    fun `refreshData sends no_network event when offline`() = runTest {

        coEvery { fileRepo.getFileById("123") } returns flow {
            delay(1)
            emit(TestUtils.fakeFile(id = "1"))
        }
        every { fakeNetwork.isNetworkAvailable() } returns false

        createViewModel()

        viewModel.eventsFlow.test {
            viewModel.refreshData()
            val event = awaitItem()
            assertTrue(event is Event.ShowMessage)
            assertEquals(R.string.no_network, (event as Event.ShowMessage).message)
        }
    }

    @Test
    fun `refreshData reobserves file when online`() = runTest {

        val file = TestUtils.fakeFile(id = "1")
        coEvery { fileRepo.getFileById("123") } returns flow {
            emit(file)
        }
        every { fakeNetwork.isNetworkAvailable() } returns true

        createViewModel()

        viewModel.refreshData()
    }

}