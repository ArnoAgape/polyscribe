package com.arnoagape.polyscribe.di

import com.arnoagape.polyscribe.data.service.file.FileApi
import com.arnoagape.polyscribe.data.service.file.FirebaseFileApi
import com.arnoagape.polyscribe.data.service.user.FirebaseUserApi
import com.arnoagape.polyscribe.data.service.user.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-wide dependencies.
 * Installed in [SingletonComponent] to ensure single instances
 * across the whole app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /**
     * Provides a singleton [FileApi] implementation backed by Firebase.
     */
    @Provides
    @Singleton
    fun provideFileApi(firebaseFileApi: FirebaseFileApi): FileApi = firebaseFileApi

    /**
     * Provides a singleton [UserApi] implementation backed by Firebase.
     */
    @Provides
    @Singleton
    fun provideUserApi(): UserApi = FirebaseUserApi()
}