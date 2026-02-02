package com.arnoagape.polyscribe.ui.screen.login

import app.cash.turbine.test
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.TestUtils
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.screen.home.HomeViewModel
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val userRepo: UserRepository = mockk()
    private val viewModel: LoginViewModel = mockk()
    private val networkUtils: NetworkUtils = mockk()

    @Before
    fun setup() {
        coEvery { userRepo.getCurrentUser() } returns TestUtils.fakeUser(id = "1")
    }

    private fun createViewModel(): LoginViewModel {
        every { networkUtils.isNetworkAvailable() } returns true
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        return LoginViewModel(
            userRepository = userRepo,
            networkUtils = networkUtils
        )
    }

    @Test
    fun `isSignedIn emits true when repository returns true`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(true)

        createViewModel()


    }

    @Test
    fun `isSignedIn emits false when repository returns false`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(false)

        createViewModel()


    }


    @Test
    fun `syncUserWithFirestore calls ensureUserInFirestore`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(false)
        coEvery { userRepo.ensureUserInFirestore() } returns Unit

        createViewModel()
        viewModel.syncUserWithFirestore()

        coVerify { userRepo.ensureUserInFirestore() }
    }


    @Test
    fun `sendEvent emits multiple events in correct order`() = runTest {
        every { userRepo.isUserSignedIn() } returns flowOf(false)
        createViewModel()

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