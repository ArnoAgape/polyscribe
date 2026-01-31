package com.arnoagape.polyscribe.data.repository

import com.arnoagape.polyscribe.data.UserSession
import com.arnoagape.polyscribe.data.service.auth.AuthApi
import com.arnoagape.polyscribe.data.service.user.UserApi
import com.arnoagape.polyscribe.domain.model.User
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Repository that handles user-related data operations.
 * Uses [UserApi] to interact with the underlying data source.
 */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi
) {

    /**
     * 🔹 Session utilisateur (pour FileRepository, Send, etc.)
     */
    fun observeUserSession(): Flow<UserSession> =
        authApi.observeFirebaseUser()
            .map { firebaseUser ->
                when {
                    firebaseUser == null ->
                        UserSession(userId = null, isGuest = false)

                    firebaseUser.isAnonymous ->
                        UserSession(userId = firebaseUser.uid, isGuest = true)

                    else ->
                        UserSession(userId = firebaseUser.uid, isGuest = false)
                }
            }

    suspend fun getCurrentSession(): UserSession {
        val firebaseUser = authApi.getCurrentFirebaseUser()
        return when {
            firebaseUser == null ->
                UserSession(userId = null, isGuest = false)

            firebaseUser.isAnonymous ->
                UserSession(userId = firebaseUser.uid, isGuest = true)

            else ->
                UserSession(userId = firebaseUser.uid, isGuest = false)
        }
    }

    /**
     * 🔹 Profil utilisateur (pour Profile, UI)
     */
    fun observeUser(): Flow<User?> =
        authApi.observeFirebaseUser()
            .flatMapLatest { firebaseUser ->
                if (firebaseUser == null || firebaseUser.isAnonymous) {
                    flowOf(null)
                } else {
                    userApi.observeUser(firebaseUser.uid)
                }
            }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = authApi.getCurrentFirebaseUser()
        return if (firebaseUser != null && !firebaseUser.isAnonymous) {
            userApi.getUser(firebaseUser.uid)
        } else null
    }

    suspend fun updateUser(user: User): Result<Unit> =
        userApi.updateUser(user)

    suspend fun ensureUserInFirestore() {
        val firebaseUser = authApi.getCurrentFirebaseUser()
            ?.takeIf { !it.isAnonymous }
            ?: return

        val user = User(
            id = firebaseUser.uid,
            displayName = firebaseUser.displayName,
            email = firebaseUser.email,
            phoneNumber = firebaseUser.phoneNumber,
            professional = false
        )

        userApi.ensureUserInFirestore(user)
    }

    fun isUserSignedIn(): Flow<Boolean?> =
        authApi.isSignedIn()

    fun signOut(): Result<Unit> =
        authApi.signOut()

    suspend fun deleteUser(): Result<Unit> =
        authApi.deleteAccount()
}