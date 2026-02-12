package com.arnoagape.polyscribe.ui.screen.login

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arnoagape.polyscribe.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysCorrectLoginContent() {

        val signInText = composeTestRule.activity.getString(
            R.string.sign_in_title
        )
        val emailButton = composeTestRule.activity.getString(
            R.string.sign_in_email
        )
        val googleButton = composeTestRule.activity.getString(
            R.string.sign_in_google
        )

        composeTestRule.setContent {
            LoginContent(
                onEmailSignInClick = {},
                onGuestSignInClick = {},
                onGoogleSignInClick = {}
            )
        }

        composeTestRule.onNodeWithText(signInText).assertExists()
        composeTestRule.onNodeWithContentDescription("Logo Polyscribe").assertExists()
        composeTestRule.onNodeWithTag(emailButton).performClick()
        composeTestRule.onNodeWithTag(googleButton).performClick()

    }
}