package com.arnoagape.polyscribe.ui.screen.detail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
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
class DetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val file =
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

    @Test
    fun displaysCorrectFileDetails() {
        val author = composeTestRule.activity.getString(
            R.string.by, file.author?.displayName
        )
        val (date, time) = Format.getLocalizedDateParts(file.dateTime)
        val collectDate = composeTestRule.activity.getString(
            R.string.detail_collect_date, date, time
        )
        val isColored = composeTestRule.activity.getString(
            R.string.detail_color, file.colored
        )
        val isDoubleSided = composeTestRule.activity.getString(
            R.string.detail_double_sided, file.doubleSided
        )
        val numberOfCopies = composeTestRule.activity.getString(
            R.string.detail_number_of_copies, file.numberOfCopies
        )
        val comments = composeTestRule.activity.getString(
            R.string.detail_comments, file.comment
        )
        val preview = composeTestRule.activity.getString(R.string.preview_file)

        val picture = composeTestRule.activity.getString(R.string.contentDescription_file_preview)
        val pdfFile =
            composeTestRule.activity.getString(R.string.contentDescription_file_preview_pdf)

        composeTestRule.setContent { DetailContent(file = file) }

        composeTestRule.onNodeWithText(author)
        composeTestRule.onNodeWithText(collectDate)
        composeTestRule.onNodeWithText(isColored)
        composeTestRule.onNodeWithText(isDoubleSided)
        composeTestRule.onNodeWithText(numberOfCopies)
        composeTestRule.onNodeWithText(comments)
        composeTestRule.onNodeWithText(preview).assertIsDisplayed()
        composeTestRule.onNode(
            hasContentDescription(picture)
                    or hasContentDescription(pdfFile)
        ).assertIsDisplayed()
    }
}