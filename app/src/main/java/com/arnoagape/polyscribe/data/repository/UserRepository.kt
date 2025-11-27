package com.arnoagape.polyscribe.data.repository

import com.arnoagape.polyscribe.data.service.user.UserApi
import com.arnoagape.polyscribe.domain.model.User
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Repository that handles user-related data operations.
 * Uses [UserApi] to interact with the underlying data source.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {

    /** Returns the currently signed-in user. */
    suspend fun getCurrentUser() = userApi.getCurrentUser()

    /** Observes the authenticated user's data. */
    fun observeUser(): Flow<User?> = userApi.observeUser()

    /** Updates the user data. */
    suspend fun updateUser(user: User) = userApi.updateUser(user)

    /** Ensures the user exists in Firestore. */
    suspend fun ensureUserInFirestore() = userApi.ensureUserInFirestore()

    /** Signs the user out. */
    fun signOut() = userApi.signOut()

    /** Emits whether a user is currently signed in. */
    fun isUserSignedIn(): Flow<Boolean> = userApi.isUserSignedIn()

    /** Permanently deletes the user's account. */
    suspend fun deleteUser() = userApi.deleteUser()
}