package com.arnoagape.polyscribe.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Extension property exposing a singleton [DataStore] instance
 * used to store lightweight user preferences.
 */
private val Context.dataStore by preferencesDataStore(name = "user_settings")

/**
 * Hilt module providing a singleton [DataStore] for user preferences.
 * Installed in [SingletonComponent] to ensure global availability.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides the [DataStore] instance responsible for persisting [Preferences].
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}