package com.arnoagape.polyscribe.data.service.user

import com.arnoagape.polyscribe.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserApi {

    fun observeUser(): Flow<User?>

    suspend fun updateUser(user: User): Result<Unit>

    suspend fun ensureUserInFirestore(): Result<Unit>

    fun signOut(): Result<Unit>

    fun isUserSignedIn(): Flow<Boolean>

    suspend fun deleteUser(): Result<Unit>
}