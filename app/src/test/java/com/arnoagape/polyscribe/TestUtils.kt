package com.arnoagape.polyscribe

import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.domain.model.User
import com.google.firebase.Timestamp
import java.time.Instant

object TestUtils {

    fun fakeFile(id: String): File {
        return File(
            id = id,
            fileUrl = listOf("file://local/path/to/file.pdf"),
            createdAt = Timestamp(1233356000, 212120),
            dateTime = Instant.now(),
            author = User(
                id = "1",
                displayName = "John Doe",
                phoneNumber = "06 01 02 03 04",
                email = "jdoe@mail.com",
                isProfessional = true
            ),
            colored = false,
            doubleSided = false,
            numberOfCopies = 9,
            comment = ""
        )
    }

    fun fakeUser(id: String): User {
        return User(id,
            displayName = "Gerry Ariella",
            phoneNumber = "0606060606",
            email = "gariella@mail.com",
            isProfessional = false)
    }

}