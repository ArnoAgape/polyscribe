package com.arnoagape.polyscribe.ui.screen.login

import app.cash.turbine.test
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
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
    fun `sendEvent emits multiple events in correct order`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(false)
        viewModel = LoginViewModel(userRepo)

        val eventNoNetwork = Event.ShowMessage(R.string.no_network)
        val eventSuccess = Event.ShowSuccessMessage(R.string.success_user_updated)

        viewModel.eventsFlow.test {
            viewModel.sendEvent(eventNoNetwork)
            viewModel.sendEvent(eventSuccess)

            assertEquals(eventNoNetwork, awaitItem())
            assertEquals(eventSuccess, awaitItem())
        }
    }
}