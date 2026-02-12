package com.arnoagape.polyscribe.ui.screen.login

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.domain.model.SessionType
import com.arnoagape.polyscribe.ui.common.Event
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val userRepo: UserRepository = mockk()
    private val networkUtils: NetworkUtils = mockk()
    private val onAllowed = mockk<() -> Unit>()
    private val signedInFlow = MutableStateFlow<Boolean?>(null)

    private fun createViewModel(): LoginViewModel {
        every { userRepo.isUserSignedIn() } returns signedInFlow

        return LoginViewModel(
            userRepository = userRepo,
            networkUtils = networkUtils
        )
    }

    @Test
    fun `onSignInRequested emits no network message when offline`() = runTest {
        val viewModel = createViewModel()

        every { networkUtils.isNetworkAvailable() } returns false

        viewModel.eventsFlow.test {
            viewModel.onSignInRequested(onAllowed = onAllowed)

            val event = awaitItem()
            assertThat(event).isInstanceOf(Event.ShowMessage::class.java)

            val offlineEvent = event as Event.ShowMessage
            assertThat(offlineEvent.message).isEqualTo(R.string.no_network)
        }
    }

    @Test
    fun `Authenticated overrides Guest session`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.loginAsGuest()
            val guest = awaitItem()
            assertThat(guest.session).isEqualTo(SessionType.Guest)

            signedInFlow.value = true
            val authenticated = awaitItem()

            assertThat(authenticated.session).isEqualTo(SessionType.Authenticated)
        }
    }

    @Test
    fun `state switches from Guest to Authenticated when user signs in`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()

            viewModel.loginAsGuest()
            val guest = awaitItem()
            assertThat(guest.session).isEqualTo(SessionType.Guest)

            signedInFlow.value = true

            val authenticated = awaitItem()
            assertThat(authenticated.session).isEqualTo(SessionType.Authenticated)
        }
    }
}