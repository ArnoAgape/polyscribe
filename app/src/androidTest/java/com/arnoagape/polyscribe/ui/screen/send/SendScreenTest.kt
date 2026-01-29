package com.arnoagape.polyscribe.ui.screen.send

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.arnoagape.polyscribe.R
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@LargeTest
@RunWith(AndroidJUnit4::class)
class SendScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysCorrectSendContent() {
        var addFileClicked = false
        var addPictureClicked = false
        var isColored = false
        var isDoubleSided = false

        composeTestRule.setContent {

            SendContent(
                localUris = emptyList(),
                onAddFile = { },
                onAddFileClick = { addFileClicked = true },
                onAddPictureClick = { addPictureClicked = true },
                onRemoveFile = {},
                dateTime = Instant.EPOCH,
                onDateTimeChange = {},
                colored = isColored,
                onColorationChange = { isColored = it },
                doubleSided = isDoubleSided,
                onDoubleSidedChange = { isDoubleSided = it },
                numberOfCopies = 1,
                onNumberOfCopiesChange = {},
                comments = "",
                onCommentsChanged = {},
                onSaveClicked = {},
                isFileValid = true,
                isLoading = false
            )
        }

        val collectDate = composeTestRule.activity.getString(
            R.string.hint_datetime
        )
        val colored = composeTestRule.activity.getString(
            R.string.hint_color
        )
        val doubleSided = composeTestRule.activity.getString(
            R.string.hint_double_sided
        )
        val numberOfCopies = composeTestRule.activity.getString(
            R.string.hint_number_of_copies
        )
        val comments = composeTestRule.activity.getString(
            R.string.hint_comments
        )

        val addFileButton = composeTestRule.activity.getString(R.string.add_file)
        val addPictureButton = composeTestRule.activity.getString(R.string.add_picture)
        val sendButton = composeTestRule.activity.getString(R.string.action_send)

        composeTestRule.onNodeWithText(collectDate).assertIsDisplayed()
        composeTestRule.onNodeWithText(colored).assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleSided).assertIsDisplayed()
        composeTestRule.onNodeWithText(numberOfCopies).assertIsDisplayed()
        composeTestRule.onNodeWithText(comments).assertIsDisplayed()

        composeTestRule.onNodeWithTag(colored).performClick()
        assertTrue(isColored)

        composeTestRule.onNodeWithTag(doubleSided).performClick()
        assertTrue(isDoubleSided)

        composeTestRule.onNodeWithTag(addFileButton).performClick()
        assertTrue(addFileClicked)

        composeTestRule.onNodeWithTag(addPictureButton).performClick()
        assertTrue(addPictureClicked)

        composeTestRule.onNodeWithTag(sendButton).performClick()
    }
}