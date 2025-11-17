package com.arnoagape.polyscribe.data.service.user

import android.util.Log
import com.arnoagape.polyscribe.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserApi : UserApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    private fun FirebaseUser.toDomain(): User = User(
        id = uid,
        displayName = displayName,
        phoneNumber = phoneNumber,
        email = email,
        isProfessional = false
    )

    override suspend fun getCurrentUser(): User? = auth.currentUser?.toDomain()

    override fun observeUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not signed in"))

            // --- 1. Updates FirebaseAuth profile ---
            val profileUpdates = userProfileChangeRequest {
                displayName = user.displayName
            }
            currentUser.updateProfile(profileUpdates).await()

            // --- 2. Updates email address with verification ---
            user.email?.let { email ->
                if (email != currentUser.email) {
                    currentUser.verifyBeforeUpdateEmail(email).await()
                }
            }

            // --- 3. Updates phone number (Firestore only) ---
            val userData = mapOf(
                "displayName" to user.displayName,
                "phoneNumber" to user.phoneNumber,
                "email" to user.email,
                "isProfessional" to user.isProfessional
            )

            usersCollection.document(currentUser.uid).set(userData, SetOptions.merge()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun ensureUserInFirestore(): Result<Unit> {
        val firebaseUser = auth.currentUser ?: return Result.failure(Exception("User not signed in"))
        val user = firebaseUser.toDomain()
        return try {
            val doc = usersCollection.document(user.id).get().await()
            if (!doc.exists()) {
                usersCollection.document(user.id).set(user).await()
                Log.d("UserRepository", "Document Firestore created for ${user.email}")
            } else {
                Log.d("UserRepository", "Document Firestore already exists for ${user.email}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun isUserSignedIn(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun deleteUser(): Result<Unit> = try {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        usersCollection.document(currentUser.uid).delete().await()
        currentUser.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

}