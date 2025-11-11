package com.arnoagape.polyscribe.data.repository

import com.arnoagape.polyscribe.data.service.user.UserApi
import com.arnoagape.polyscribe.domain.model.User
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * This class provides a repository for accessing and managing User data.
 * It utilizes dependency injection to retrieve a UserApi instance for interacting
 * with the data source. The class is marked as a Singleton using @Singleton annotation,
 * ensuring there's only one instance throughout the application.
 */
@Singleton
class UserRepository @Inject constructor(private val userApi: UserApi) {

    fun observeUser(): Flow<User?> = userApi.observeUser()
    suspend fun updateUser(user: User) = userApi.updateUser(user)
    suspend fun ensureUserInFirestore() = userApi.ensureUserInFirestore()
    fun signOut() = userApi.signOut()
    fun isUserSignedIn(): Flow<Boolean> = userApi.isUserSignedIn()
    suspend fun deleteUser() = userApi.deleteUser()
}