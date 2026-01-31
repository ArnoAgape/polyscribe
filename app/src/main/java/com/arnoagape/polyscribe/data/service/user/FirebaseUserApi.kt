package com.arnoagape.polyscribe.data.service.user

import com.arnoagape.polyscribe.data.dto.UserDto
import com.arnoagape.polyscribe.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firebase implementation of [UserApi].
 * Handles authentication, profile updates and Firestore user documents.
 */
class FirebaseUserApi @Inject constructor() : UserApi {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    override fun observeUser(userId: String): Flow<User?> =
        usersCollection.document(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(UserDto::class.java)
                    ?.let(User::fromDto)
            }
            .catch {
                emit(null)
            }

    override suspend fun getUser(userId: String): User? =
        usersCollection.document(userId)
            .get()
            .await()
            .toObject(UserDto::class.java)
            ?.let(User::fromDto)

    override suspend fun updateUser(user: User): Result<Unit> = try {
        usersCollection.document(user.id)
            .set(user.toDto(), SetOptions.merge())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun ensureUserInFirestore(user: User): Result<Unit> =
        updateUser(user)
}