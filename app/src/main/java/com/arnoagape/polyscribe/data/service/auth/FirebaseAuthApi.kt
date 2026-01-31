package com.arnoagape.polyscribe.data.service.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthApi @Inject constructor() : AuthApi {

    private val auth = FirebaseAuth.getInstance()

    override fun observeFirebaseUser(): Flow<FirebaseUser?> = callbackFlow {
        trySend(auth.currentUser)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun getCurrentFirebaseUser(): FirebaseUser? =
        auth.currentUser

    override fun isSignedIn(): Flow<Boolean> = callbackFlow {
        fun emitAuthState() {
            val user = auth.currentUser
            trySend(user != null && !user.isAnonymous)
        }

        emitAuthState()

        val listener = FirebaseAuth.AuthStateListener {
            emitAuthState()
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAccount(): Result<Unit> = try {
        val user = auth.currentUser ?: return Result.failure(Exception("No user"))
        user.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}