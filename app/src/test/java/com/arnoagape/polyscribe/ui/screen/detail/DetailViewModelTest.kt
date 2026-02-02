package com.arnoagape.polyscribe.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
    private val fileRepo: FileRepository = mockk()
    private val userRepo: UserRepository = mockk()
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle().apply {
            set("fileId", "123")
        }
    }

    private fun createViewModel(): DetailViewModel {
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        return DetailViewModel(
            fileRepository = fileRepo,
            userRepository = userRepo,
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `file success contains correct data`() = runTest {
        every { fileRepo.observeFile(any()) } returns flowOf(TestUtils.fakeFile("1"))

        val viewModel = createViewModel()

        viewModel.state.test {
            val success = awaitItem()

            val file = (success.uiState as DetailUiState.Success).file

            assertThat(file.colored).isFalse()
            assertThat(file.doubleSided).isTrue()
            assertThat(file.numberOfCopies).isEqualTo(9)
            assertThat(file.comment).isEqualTo("I love Polyscribe!")
            assertThat(file.author?.displayName).isEqualTo("John Doe")
            assertThat(file.fileUrl).isEqualTo(listOf("file://local/path/to/file.pdf"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeFile emits Error_Empty when file is null`() = runTest {

        coEvery { fileRepo.observeFile("123") } returns flow {
            delay(1)
            emit(null)
        }

        val viewModel = createViewModel()

        viewModel.state.test {
            val loading = awaitItem()
            assertThat(loading.uiState).isEqualTo(DetailUiState.Loading)

            val empty = awaitItem()
            assertThat(empty.uiState)
                .isEqualTo(DetailUiState.Error.Empty("Impossible to find the file"))
        }
    }

    @Test
    fun `observeFile emits Generic error when exception thrown`() = runTest {

        coEvery { fileRepo.observeFile("123") } returns flow {
            delay(1)
            throw RuntimeException("boom")
        }

        val viewModel = createViewModel()

        viewModel.state.test {
            val loading = awaitItem()
            assertThat(loading.uiState).isEqualTo(DetailUiState.Loading)

            val error = awaitItem()
            assertThat(error.uiState)
                .isEqualTo(DetailUiState.Error.Generic("boom"))
        }
    }

}