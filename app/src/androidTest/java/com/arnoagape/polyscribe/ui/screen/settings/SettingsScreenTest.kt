package com.arnoagape.polyscribe.ui.screen.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.arnoagape.polyscribe.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    fun hasRole(role: Role): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, role)


    @Test
    fun displaysCorrectSettingsContent() {

        val notifications = composeTestRule.activity.getString(
            R.string.notifications
        )

        composeTestRule.setContent {
            SettingsContent(
                onNotificationEnabledClicked = { },
                notificationsEnabled = false
            )
        }
        composeTestRule.onNode(hasRole(Role.Switch)).performClick()
        composeTestRule.onNodeWithText(notifications).assertIsDisplayed()

    }
}