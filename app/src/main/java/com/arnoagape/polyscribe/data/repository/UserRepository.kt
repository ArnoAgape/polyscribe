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

    suspend fun getCurrentUser() = userApi.getCurrentUser()
    fun observeUser(): Flow<User?> = userApi.observeUser()
    suspend fun updateUser(user: User) = userApi.updateUser(user)
    suspend fun ensureUserInFirestore() = userApi.ensureUserInFirestore()
    fun signOut() = userApi.signOut()
    fun isUserSignedIn(): Flow<Boolean> = userApi.isUserSignedIn()
    suspend fun deleteUser() = userApi.deleteUser()
}