package com.arnoagape.polyscribe.ui.screen.send

import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SendViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var fileRepo: FileRepository
    private lateinit var userRepo: UserRepository
    private lateinit var viewModel: SendViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        fileRepo = mockk()
        userRepo = mockk()
        context = mockk()

        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1")
        coEvery { userRepo.isUserSignedIn() } returns flowOf(true)

        viewModel = SendViewModel(
            fileRepository = fileRepo,
            userRepository = userRepo,
            context = context
        )
    }

    @Test
    fun `isFileValid is false when no file has been added`() = runTest {

        val mockUri = mockk<Uri>()

        viewModel.onAction(FormEvent.RemoveFile(mockUri))
        viewModel.state.test {
            awaitItem()
        }
    }


    @Test
    fun `isFileValid becomes true after adding a file`() = runTest {

        val mockUri = mockk<Uri>()

        every { mockUri.toString() } returns "content://media/picker/image123"
        every { mockUri.path } returns "/storage/emulated/0/DCIM/image123.jpg"

        viewModel.state.test {
            awaitItem() // valeur initiale

            viewModel.onAction(FormEvent.AddFile(mockUri))

            val updated = awaitItem()

            assertTrue(updated.isValid)
        }
    }

    @Test
    fun `sendFile emits success event`() = runTest {

        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser("1")
        coEvery { fileRepo.sendFile(any(), any()) } returns listOf("url1")

        viewModel.eventsFlow.test {

            viewModel.sendFile()

            val event = awaitItem()

            assertTrue(event is Event.FileSentSuccessfully)
        }

    }
}