package com.arnoagape.polyscribe.ui.screen.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.domain.model.User
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val user = User(
        id = "1",
        displayName = "Gerry Ariella",
        phoneNumber = "0606060606",
        email = "gariella@mail.com",
        isProfessional = false
    )

    @Test
    fun displaysCorrectProfileContent() {

        val nameField = composeTestRule.activity.getString(
            R.string.user_name
        )
        val emailField = composeTestRule.activity.getString(
            R.string.user_email
        )

        val userName = user.displayName.toString()
        val userEmail = user.email.toString()
        val profileImage = composeTestRule.activity.getString(R.string.profile_picture)

        val saveButton = composeTestRule.activity.getString(
            R.string.action_save
        )
        val signOutButton = composeTestRule.activity.getString(
            R.string.action_sign_out
        )
        val deleteAccountButton = composeTestRule.activity.getString(
            R.string.action_delete_account
        )

        composeTestRule.setContent {
            ProfileContent(
                userName = userName,
                onNameChanged = { },
                emailAddress = userEmail,
                onEmailChanged = { },
                onSaveClick = { },
                onSignOutClick = { },
                onDeleteAccountClick = { },
                isUserFieldsValid = true,
                isLoading = false
            )
        }

        composeTestRule.onNodeWithContentDescription(profileImage).assertExists()
        composeTestRule.onNodeWithText(nameField).assertIsDisplayed()
        composeTestRule.onNodeWithText(userName).assertIsDisplayed()
        composeTestRule.onNodeWithText(emailField).assertIsDisplayed()
        composeTestRule.onNodeWithText(userEmail).assertIsDisplayed()
        composeTestRule.onNodeWithTag(saveButton).performClick()
        composeTestRule.onNodeWithText(signOutButton).performClick()
        composeTestRule.onNodeWithText(deleteAccountButton).performClick()

    }
}