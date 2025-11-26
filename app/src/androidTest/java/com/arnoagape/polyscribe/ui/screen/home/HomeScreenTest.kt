package com.arnoagape.polyscribe.ui.screen.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.arnoagape.polyscribe.ui.utils.Format
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val files =
        listOf(
            File(
                id = "1",
                fileUrl = listOf("file://local/path/to/file.pdf"),
                createdAt = Timestamp(1233356000, 212120),
                dateTime = Instant.now(),
                author = User(
                    id = "1",
                    displayName = "Arno",
                    phoneNumber = "06 01 02 03 04",
                    email = "jdoe@mail.com",
                    isProfessional = true
                ),
                colored = false,
                doubleSided = false,
                numberOfCopies = 9,
                comment = ""
            )
        )

    @Test
    fun displaysCorrectHomeContent() {
        val author = composeTestRule.activity.getString(
            R.string.by, files[0].author?.displayName
        )
        val (date, time) = Format.getLocalizedDateParts(files[0].createdAt)
        val dateOfSending = composeTestRule.activity.getString(
            R.string.sent_at, date, time
        )

        val picture = composeTestRule.activity.getString(R.string.contentDescription_file_preview)
        val pdfFile =
            composeTestRule.activity.getString(R.string.contentDescription_file_preview_pdf)

        composeTestRule.setContent {
            HomeContent(
                files = files,
                onFileClick = {}
            )
        }

        composeTestRule.onNodeWithText(author).assertExists()
        composeTestRule.onNodeWithText(dateOfSending).assertExists()
        composeTestRule.onNode(
            hasContentDescription(picture)
                    or hasContentDescription(pdfFile)
        ).assertExists()

    }
}