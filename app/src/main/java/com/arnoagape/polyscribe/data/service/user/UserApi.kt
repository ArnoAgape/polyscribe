package com.arnoagape.polyscribe.data.service.user

import com.arnoagape.polyscribe.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining user account operations.
 * Implementations handle authentication and Firestore persistence.
 */
interface UserApi {

    fun observeUser(userId: String): Flow<User?>
    suspend fun getUser(userId: String): User?
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun ensureUserInFirestore(user: User): Result<Unit>
}