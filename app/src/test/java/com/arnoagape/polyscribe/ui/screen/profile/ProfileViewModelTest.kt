package com.arnoagape.polyscribe.ui.screen.profile

import app.cash.turbine.test
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.common.FormEvent
import com.arnoagape.polyscribe.ui.utils.AndroidEmailValidator
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var userRepo: UserRepository
    private lateinit var network: NetworkUtils
    private lateinit var emailValidator: AndroidEmailValidator
    private lateinit var viewModel: ProfileViewModel

    private val fakeUser = TestUtils.fakeUser(id = "1")

    @Before
    fun setup() {
        userRepo = mockk(relaxed = true)
        network = mockk(relaxed = true)
        emailValidator = mockk()
    }

    private fun createViewModel() {
        viewModel = ProfileViewModel(userRepo, network, emailValidator)
    }

    @Test
    fun `init loads user from repository`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { emailValidator.validate(any()) } returns true

        createViewModel()

        assertEquals(fakeUser, viewModel.user.value)
    }

    @Test
    fun `isUserFieldsValid is false when user has empty displayName`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser.copy(displayName = ""))
        every { emailValidator.validate(any()) } returns true

        createViewModel()

        assertFalse(viewModel.isUserFieldsValid.value)
    }

    @Test
    fun `isUserFieldsValid is false when email invalid`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { emailValidator.validate(any()) } returns false

        createViewModel()

        assertFalse(viewModel.isUserFieldsValid.value)
    }

    @Test
    fun `onAction(DisplayNameChanged) updates user`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { emailValidator.validate(any()) } returns true

        createViewModel()

        viewModel.onAction(FormEvent.DisplayNameChanged("NewName"))

        assertEquals("NewName", viewModel.user.value?.displayName)
    }

    @Test
    fun `onAction(EmailChanged) updates user`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { emailValidator.validate(any()) } returns true

        createViewModel()

        viewModel.onAction(FormEvent.EmailChanged("new@mail.com"))

        assertEquals("new@mail.com", viewModel.user.value?.email)
    }

    @Test
    fun `saveUser sends NoAccount when user null`() = runTest {
        every { userRepo.observeUser() } returns flowOf(null)
        every { network.checkNetwork(any(), any()) } returns Unit

        createViewModel()

        viewModel.eventsFlow.test {
            viewModel.saveUser()
            val event = awaitItem()
            assertTrue(event is Event.ShowMessage)
            assertEquals(R.string.error_no_account_profile, (event as Event.ShowMessage).message)
        }

        assertTrue(viewModel.uiState.value is ProfileUiState.Error.NoAccount)
    }

    @Test
    fun `saveUser success updates UI and sends success event`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { emailValidator.validate(any()) } returns true
        every { network.checkNetwork(any(), any()) } returns Unit
        coEvery { userRepo.updateUser(any()) } returns Result.success(Unit)

        createViewModel()

        viewModel.eventsFlow.test {
            viewModel.saveUser()

            // success event expected
            val event = awaitItem()
            assertTrue(event is Event.ShowSuccessMessage)
        }

        assertTrue(viewModel.uiState.value is ProfileUiState.Success)
    }

    @Test
    fun `saveUser handles IOException as network error`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { emailValidator.validate(any()) } returns true
        every { network.checkNetwork(any(), any()) } returns Unit
        coEvery { userRepo.updateUser(any()) } throws IOException("fail")

        createViewModel()

        viewModel.eventsFlow.test {
            viewModel.saveUser()
            val evt = awaitItem()
            assertTrue(evt is Event.ShowMessage)
            assertEquals(R.string.no_network, (evt as Event.ShowMessage).message)
        }

        assertTrue(viewModel.uiState.value is ProfileUiState.Error.Generic)
    }

    @Test
    fun `saveUser handles generic exception`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { emailValidator.validate(any()) } returns true
        every { network.checkNetwork(any(), any()) } returns Unit
        coEvery { userRepo.updateUser(any()) } throws RuntimeException()

        createViewModel()

        viewModel.eventsFlow.test {
            viewModel.saveUser()
            val evt = awaitItem()
            assertTrue(evt is Event.ShowMessage)
            assertEquals(R.string.error_generic, (evt as Event.ShowMessage).message)
        }

        assertTrue(viewModel.uiState.value is ProfileUiState.Error.Generic)
    }

    @Test
    fun `signOut clears user when success`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        coEvery { userRepo.signOut() } returns Result.success(Unit)
        every { emailValidator.validate(any()) } returns true

        createViewModel()
        viewModel.signOut()

        assertNull(viewModel.user.value)
    }

    @Test
    fun `signOut does not clear user when failure`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        coEvery { userRepo.signOut() } returns Result.failure(Exception())
        every { emailValidator.validate(any()) } returns true

        createViewModel()
        viewModel.signOut()

        assertNotNull(viewModel.user.value)
    }

    @Test
    fun `deleteAccount clears user on success`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        coEvery { userRepo.deleteUser() } returns Result.success(Unit)
        every { emailValidator.validate(any()) } returns true

        createViewModel()
        viewModel.deleteAccount()

        assertNull(viewModel.user.value)
    }

    @Test
    fun `deleteAccount does not clear user on failure`() = runTest {
        every { userRepo.observeUser() } returns flowOf(fakeUser)
        coEvery { userRepo.deleteUser() } returns Result.failure(Exception())
        every { emailValidator.validate(any()) } returns true

        createViewModel()
        viewModel.deleteAccount()

        assertNotNull(viewModel.user.value)
    }

}