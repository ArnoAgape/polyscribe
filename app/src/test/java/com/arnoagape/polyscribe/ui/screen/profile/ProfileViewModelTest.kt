package com.arnoagape.polyscribe.ui.screen.profile

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val userRepo: UserRepository = mockk()
    private val networkUtils: NetworkUtils = mockk()
    private val emailValidator: AndroidEmailValidator = mockk()

    private val fakeUser = TestUtils.fakeUser(id = "1")
    private val fakeUserSession = TestUtils.fakeUserSession(id = "1")

    private fun createViewModel(): ProfileViewModel {

        every { userRepo.observeUser() } returns flowOf(fakeUser)
        every { userRepo.observeUserSession() } returns flowOf(fakeUserSession)

        return ProfileViewModel(
            userRepository = userRepo,
            networkUtils = networkUtils,
            emailValidator = emailValidator
        )
    }

    @Test
    fun `init loads user from repository`() = runTest {
        createViewModel()

        assertThat(fakeUser.displayName).isEqualTo("Gerry Ariella")
        assertThat(fakeUser.email).isEqualTo("gariella@mail.com")
    }

    @Test
    fun `isUserFieldsValid is false when user has empty displayName`() = runTest {
        val viewModel = createViewModel()

        every { userRepo.observeUser() } returns flowOf(fakeUser.copy(displayName = ""))

        val fieldsValid = viewModel.isUserFieldsValid.value
        assertThat(fieldsValid).isFalse()
    }

    @Test
    fun `isUserFieldsValid is false when user has an invalid email`() = runTest {
        val viewModel = createViewModel()

        every { emailValidator.validate(any()) } returns false

        val fieldsValid = viewModel.isUserFieldsValid.value
        assertThat(fieldsValid).isFalse()
    }

    @Test
    fun `onAction(DisplayNameChanged) updates user`() = runTest {
        val viewModel = createViewModel()

        viewModel.user.test {
            val initialUser = awaitItem()
            assertThat(initialUser?.displayName).isEqualTo(fakeUser.displayName)

            viewModel.onAction(FormEvent.DisplayNameChanged("NewName"))

            val updatedUser = awaitItem()
            assertThat(updatedUser?.displayName).isEqualTo("NewName")
        }
    }

    @Test
    fun `onAction(EmailChanged) updates user`() = runTest {
        val viewModel = createViewModel()

        viewModel.user.test {
            val initialUser = awaitItem()
            assertThat(initialUser?.email).isEqualTo(fakeUser.email)

            viewModel.onAction(FormEvent.EmailChanged("test@mail.fr"))

            val updatedUser = awaitItem()
            assertThat(updatedUser?.email).isEqualTo("test@mail.fr")
        }
    }

    @Test
    fun `saveUser emits success event`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns true
        every { emailValidator.validate(any()) } returns true
        coEvery { userRepo.updateUser(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()

        viewModel.user.test {
            assertThat(awaitItem()).isEqualTo(fakeUser)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.eventsFlow.test {
            viewModel.saveUser()

            val event = awaitItem()
            assertThat(event).isInstanceOf(Event.ShowSuccessMessage::class)
            assertThat((event as Event.ShowSuccessMessage).message)
                .isEqualTo(R.string.success_user_updated)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveUser failure emits generic error`() = runTest {
        every { networkUtils.isNetworkAvailable() } returns true
        coEvery { userRepo.updateUser(any()) } throws RuntimeException()

        val viewModel = createViewModel()

        viewModel.user.test {
            assertThat(awaitItem()).isEqualTo(fakeUser)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.eventsFlow.test {
            viewModel.saveUser()

            val event = awaitItem()
            assertThat(event).isInstanceOf(Event.ShowMessage::class)
            assertThat((event as Event.ShowMessage).message)
                .isEqualTo(R.string.error_generic)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOut emits success event`() = runTest {
        coEvery { userRepo.signOut() } returns Result.success(Unit)

        val viewModel = createViewModel()

        viewModel.eventsFlow.test {
            viewModel.signOut()

            val event = awaitItem()
            assertThat(event).isInstanceOf(Event.ShowSuccessMessage::class)
            assertThat((event as Event.ShowSuccessMessage).message)
                .isEqualTo(R.string.success_sign_out)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteAccount emits success event`() = runTest {
        coEvery { userRepo.deleteUser() } returns Result.success(Unit)

        val viewModel = createViewModel()

        viewModel.eventsFlow.test {
            viewModel.deleteAccount()

            val event = awaitItem()
            assertThat(event).isInstanceOf(Event.ShowSuccessMessage::class)
            assertThat((event as Event.ShowSuccessMessage).message)
                .isEqualTo(R.string.success_deleted_account)

            cancelAndIgnoreRemainingEvents()
        }
    }

}