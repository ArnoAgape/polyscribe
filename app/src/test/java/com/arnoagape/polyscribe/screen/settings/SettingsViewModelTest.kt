package com.arnoagape.polyscribe.screen.settings

import com.arnoagape.polyscribe.MainDispatcherRule
import com.arnoagape.polyscribe.data.repository.SettingsRepository
import com.arnoagape.polyscribe.ui.screen.settings.SettingsViewModel
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsRepo: SettingsRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        settingsRepo = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `toggleNotifications true calls repo and subscribes to global topic`() = runTest {
        // Arrange
        mockkStatic(FirebaseMessaging::class)
        val mockFcm = mockk<FirebaseMessaging>(relaxed = true)
        every { FirebaseMessaging.getInstance() } returns mockFcm

        viewModel = SettingsViewModel(settingsRepo)

        // Act
        viewModel.toggleNotifications(true)

        // Assert
        coVerify(exactly = 1) { settingsRepo.setNotificationsEnabled(true) }
        verify(exactly = 1) { mockFcm.subscribeToTopic("global") }
        verify(exactly = 0) { mockFcm.unsubscribeFromTopic(any()) }

        unmockkStatic(FirebaseMessaging::class)
    }

    @Test
    fun `toggleNotifications false calls repo and unsubscribes from global topic`() = runTest {
        // Arrange
        mockkStatic(FirebaseMessaging::class)
        val mockFcm = mockk<FirebaseMessaging>(relaxed = true)
        every { FirebaseMessaging.getInstance() } returns mockFcm

        viewModel = SettingsViewModel(settingsRepo)

        // Act
        viewModel.toggleNotifications(false)

        // Assert
        coVerify(exactly = 1) { settingsRepo.setNotificationsEnabled(false) }
        verify(exactly = 1) { mockFcm.unsubscribeFromTopic("global") }
        verify(exactly = 0) { mockFcm.subscribeToTopic(any()) }

        unmockkStatic(FirebaseMessaging::class)
    }
}