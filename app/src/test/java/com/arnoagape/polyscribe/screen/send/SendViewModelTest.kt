package com.arnoagape.polyscribe.screen.send

import android.net.Uri
import app.cash.turbine.test
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.screen.send.SendUiState
import com.arnoagape.polyscribe.ui.screen.send.SendViewModel
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class SendViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var fileRepo: FileRepository
    private lateinit var userRepo: UserRepository

    private lateinit var fakeNetwork: NetworkUtils
    private lateinit var viewModel: SendViewModel

    @Before
    fun setup() {
        fileRepo = mockk()
        userRepo = mockk()
        fakeNetwork = mockk()

        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1")
        every { fakeNetwork.checkNetwork(any(), any()) } returns Unit

        viewModel = SendViewModel(
            fileRepository = fileRepo,
            userRepository = userRepo,
            networkUtils = fakeNetwork
        )
    }

    @Test
    fun `isFileValid is false when no file has been added`() = runTest {

        val mockUri = mockk<Uri>()

        viewModel.onAction(FormEvent.RemoveFile(mockUri))
        viewModel.isFileValid.test {
            assertFalse(awaitItem()) // initial value
        }
    }


    @Test
    fun `isFileValid becomes true after adding a file`() = runTest {

        val mockUri = mockk<Uri>()

        every { mockUri.toString() } returns "content://media/picker/image123"
        every { mockUri.path } returns "/storage/emulated/0/DCIM/image123.jpg"

        viewModel.isFileValid.test {
            awaitItem() // false initialValue
            viewModel.onAction(FormEvent.AddFile(mockUri))
            assertTrue(awaitItem()) // file added -> valid
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `sendFile emits Success when repository succeeds`() = runTest {

        // Arrange
        coEvery { fileRepo.sendFile(any(), any()) } returns listOf("url1", "url2")
        coEvery { fakeNetwork.isNetworkAvailable() } returns true
        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1")

        viewModel.onAction(FormEvent.NumberOfCopiesSet(9))

        // Act
        viewModel.sendFile()

        // Assert
        val uiState = viewModel.uiState.value
        assertTrue(uiState is SendUiState.Success)
        assertEquals(9, (uiState as SendUiState.Success).file.numberOfCopies)

        coVerify { fileRepo.sendFile(any(), any()) }
    }

    @Test
    fun `AddFile adds uri to localUris`() = runTest {
        val u = mockk<Uri>()
        every { u.toString() } returns "x"
        every { u.path } returns "/x"

        viewModel.onAction(FormEvent.AddFile(u))

        assertEquals(listOf(u), viewModel.localUris.value)
    }

    @Test
    fun `RemoveFile removes uri`() = runTest {
        val u = mockk<Uri>()
        every { u.toString() } returns "x"
        every { u.path } returns "/x"

        viewModel.onAction(FormEvent.AddFile(u))
        viewModel.onAction(FormEvent.RemoveFile(u))

        assertTrue(viewModel.localUris.value.isEmpty())
    }

    @Test
    fun `NumberOfCopiesSet does not allow values below 1`() = runTest {
        viewModel.onAction(FormEvent.NumberOfCopiesSet(-10))
        assertEquals(1, viewModel.file.value.numberOfCopies)
    }

    @Test
    fun `ColorChanged updates colored`() = runTest {
        viewModel.onAction(FormEvent.ColorChanged(true))
        assertTrue(viewModel.file.value.colored)
    }

    @Test
    fun `DoubleSidedChanged updates doubleSided`() = runTest {
        viewModel.onAction(FormEvent.DoubleSidedChanged(true))
        assertTrue(viewModel.file.value.doubleSided)
    }

    @Test
    fun `DateTimeChanged updates dateTime`() = runTest {
        val now = Instant.now()
        viewModel.onAction(FormEvent.DateTimeChanged(now))
        assertEquals(now, viewModel.file.value.dateTime)
    }

    @Test
    fun `sendFile shows no network error when offline`() = runTest {
        coEvery { fakeNetwork.isNetworkAvailable() } returns false
        viewModel.eventsFlow.test {

            viewModel.sendFile()

            val event = awaitItem()
            assertTrue(event is Event.ShowMessage)
        }

        coVerify(exactly = 0) { fileRepo.sendFile(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `sendFile emits Generic error when repository throws`() = runTest {
        // Arrange
        coEvery { fakeNetwork.isNetworkAvailable() } returns true
        coEvery { fileRepo.sendFile(any(), any()) } throws Exception("DB error")


        // Act
        viewModel.sendFile()

        // Assert
        val state = viewModel.uiState.value

        assertTrue(state is SendUiState.Error.Generic)
        coVerify { fileRepo.sendFile(any(), any()) }
    }

    @Test
    fun `sendFile emits NoAccount when user is null`() = runTest {
        coEvery { userRepo.getCurrentUser() } returns null
        coEvery { fakeNetwork.isNetworkAvailable() } returns true

        viewModel = SendViewModel(
            fileRepository = fileRepo,
            userRepository = userRepo,
            networkUtils = fakeNetwork
        )

        viewModel.sendFile()

        val state = viewModel.uiState.value
        assertTrue(state is SendUiState.Error.NoAccount)
    }

    @Test
    fun `sendFile emits success event`() = runTest {
        coEvery { fakeNetwork.isNetworkAvailable() } returns true
        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser("1")
        coEvery { fileRepo.sendFile(any(), any()) } returns listOf("url1")

        viewModel.eventsFlow.test {
            viewModel.sendFile()

            val event = awaitItem()
            assertTrue(event is Event.ShowSuccessMessage)
        }
    }

}