package com.arnoagape.polyscribe.data.service.user

import com.arnoagape.polyscribe.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining user account operations.
 * Implementations handle authentication and Firestore persistence.
 */
interface UserApi {

    suspend fun getCurrentUser(): User?
    fun isGuest(): Boolean
    fun observeUser(): Flow<User?>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun ensureUserInFirestore(): Result<Unit>
    fun signOut(): Result<Unit>
    fun isUserSignedIn(): Flow<Boolean>
    suspend fun deleteUser(): Result<Unit>
}