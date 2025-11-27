package com.arnoagape.polyscribe.data.service.user

import com.arnoagape.polyscribe.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining user account operations.
 * Implementations handle authentication and Firestore persistence.
 */
interface UserApi {

    /** Returns the currently authenticated user or null. */
    suspend fun getCurrentUser(): User?

    /** Observes authentication state and user data. */
    fun observeUser(): Flow<User?>

    /** Updates the user profile and Firestore document. */
    suspend fun updateUser(user: User): Result<Unit>

    /** Ensures the user document exists in Firestore. */
    suspend fun ensureUserInFirestore(): Result<Unit>

    /** Signs out the current user. */
    fun signOut(): Result<Unit>

    /** Emits authentication status changes. */
    fun isUserSignedIn(): Flow<Boolean>

    /** Permanently deletes the current user and their Firestore document. */
    suspend fun deleteUser(): Result<Unit>
}