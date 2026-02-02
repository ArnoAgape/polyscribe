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

        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1234")
        every { fakeNetwork.checkNetwork(any(), any()) } returns Unit
        every { userRepo.observeUser() } returns flowOf(TestUtils.fakeUser(id = "1234"))

        savedStateHandle = SavedStateHandle().apply {
            set("fileId", "123")
        }
        every { userRepo.isUserSignedIn() } returns flowOf(true)

    }

    fun createViewModel() {
        viewModel = DetailViewModel(
            fileRepository = fileRepo,
            userRepository = userRepo,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun `observeFile sets Loading first`() = runTest {

        coEvery { fileRepo.observeFile(any()) } returns flow {
            delay(1)
            emit(TestUtils.fakeFile(id = "123"))
        }

        createViewModel()

        viewModel.fileState.test {
            assertTrue(awaitItem() is DetailUiState.Loading)
            assertTrue(awaitItem() is DetailUiState.Success)
        }
    }

    @Test
    fun `observeFile emits Error_Empty when file is null`() = runTest {

        coEvery { fileRepo.observeFile("123") } returns flow {
            delay(1)
            emit(null)
        }

        createViewModel()

        viewModel.fileState.test {
            assertTrue(awaitItem() is DetailUiState.Loading)
            val second = awaitItem()
            assertTrue(second is DetailUiState.Error.Empty)
        }
    }

    @Test
    fun `observeFile emits Generic error when exception thrown`() = runTest {

        coEvery { fileRepo.observeFile("123") } returns flow {
            delay(1)
            throw RuntimeException("boom")
        }

        createViewModel()

        viewModel.fileState.test {
            assertTrue(awaitItem() is DetailUiState.Loading)
            val second = awaitItem()
            assertTrue(second is DetailUiState.Error.Generic)
        }
    }

}