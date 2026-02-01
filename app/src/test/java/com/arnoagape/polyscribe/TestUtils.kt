package com.arnoagape.polyscribe

import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.google.firebase.Timestamp
import java.time.Instant

/**
 * Utility object providing fake domain models for unit tests.
 */
object TestUtils {

    /**
     * Creates a fake [File] instance with predefined values.
     *
     * @param id Unique identifier for the file.
     * @return A fake [File] used for testing.
     */
    fun fakeFile(id: String): File {
        return File(
            id = id,
            fileUrl = listOf("file://local/path/to/file.pdf"),
            createdAt = Timestamp(1233356000, 212120),
            collectDate = Instant.now(),
            author = User(
                id = "1",
                displayName = "John Doe",
                phoneNumber = "06 01 02 03 04",
                email = "jdoe@mail.com",
                professional = true
            ),
            colored = false,
            doubleSided = false,
            numberOfCopies = 9,
            comment = ""
        )
    }

    /**
     * Creates a fake [User] instance for testing purposes.
     *
     * @param id Unique identifier for the user.
     * @return A fake [User].
     */
    fun fakeUser(id: String): User {
        return User(id,
            displayName = "Gerry Ariella",
            phoneNumber = "0606060606",
            email = "gariella@mail.com",
            professional = false)
    }

}