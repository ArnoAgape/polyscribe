package com.arnoagape.polyscribe.data.service.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthApi {
    fun observeFirebaseUser(): Flow<FirebaseUser?>
    suspend fun getCurrentFirebaseUser(): FirebaseUser?
    fun isSignedIn(): Flow<Boolean?>
    fun signOut(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}