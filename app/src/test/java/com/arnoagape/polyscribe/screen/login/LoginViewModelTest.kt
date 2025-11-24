package com.arnoagape.polyscribe.screen.login

import app.cash.turbine.test
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.screen.login.LoginViewModel
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var userRepo: UserRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        clearAllMocks()
        userRepo = mockk(relaxed = true)

        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1")

    }

    @Test
    fun `isSignedIn emits true when repository returns true`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        viewModel = LoginViewModel(userRepo)

        assertTrue(viewModel.isSignedIn.value)
    }

    @Test
    fun `isSignedIn emits false when repository returns false`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(false)

        viewModel = LoginViewModel(userRepo)

        assertFalse(viewModel.isSignedIn.value)
    }


    @Test
    fun `syncUserWithFirestore calls ensureUserInFirestore`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(false)
        coEvery { userRepo.ensureUserInFirestore() } returns Result.success(Unit)

        viewModel = LoginViewModel(userRepo)
        viewModel.syncUserWithFirestore()

        coVerify { userRepo.ensureUserInFirestore() }
    }


    @Test
    fun `sendEvent sends event through eventsFlow`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(false)

        viewModel = LoginViewModel(userRepo)

        viewModel.eventsFlow.test {
            viewModel.sendEvent(Event.ShowMessage(com.arnoagape.polyscribe.R.string.no_network))

            val event = awaitItem()
            assertTrue(event is Event.ShowMessage)
        }
    }
}