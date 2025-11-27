package com.arnoagape.polyscribe.data.service.user

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

/**
 * Firebase implementation of [UserApi].
 * Handles authentication, profile updates and Firestore user documents.
 */
class FirebaseUserApi : UserApi {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /** Converts a FirebaseUser into a domain [User] model. */
    private fun FirebaseUser.toDomain(): User = User(
        id = uid,
        displayName = displayName,
        phoneNumber = phoneNumber,
        email = email,
        isProfessional = false
    )

    /** Returns the currently authenticated user. */
    override suspend fun getCurrentUser(): User? = auth.currentUser?.toDomain()

    /** Observes authentication changes and emits the current user. */
    override fun observeUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Updates the user's FirebaseAuth profile, email verification,
     * and persists additional fields in Firestore.
     */
    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not signed in"))

            val profileUpdates = userProfileChangeRequest {
                displayName = user.displayName
            }
            currentUser.updateProfile(profileUpdates).await()

            user.email?.let { email ->
                if (email != currentUser.email) {
                    currentUser.verifyBeforeUpdateEmail(email).await()
                }
            }

            val userData = mapOf(
                "displayName" to user.displayName,
                "phoneNumber" to user.phoneNumber,
                "email" to user.email,
                "isProfessional" to user.isProfessional
            )

            usersCollection.document(currentUser.uid)
                .set(userData, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ensures the authenticated user has a Firestore document.
     */
    override suspend fun ensureUserInFirestore(): Result<Unit> {
        val firebaseUser =
            auth.currentUser ?: return Result.failure(Exception("User not signed in"))
        val user = firebaseUser.toDomain()

        return try {
            val doc = usersCollection.document(user.id).get().await()
            if (!doc.exists()) {
                usersCollection.document(user.id).set(user).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Signs out the current user. */
    override fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Emits authentication status updates in real time. */
    override fun isUserSignedIn(): Flow<Boolean> = callbackFlow {
        trySend(auth.currentUser != null)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Deletes the current FirebaseAuth user and their Firestore record. */
    override suspend fun deleteUser(): Result<Unit> = try {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        usersCollection.document(currentUser.uid).delete().await()
        currentUser.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}