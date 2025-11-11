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
 * This class acts as a Dagger Hilt module, responsible for providing dependencies to other parts of the application.
 * It's installed in the SingletonComponent, ensuring that dependencies provided by this module are created only once
 * and remain available throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
  /**
   * Provides a Singleton instance of PostApi using a FirebasePostApi implementation for testing purposes.
   * This means that whenever a dependency on PostApi is requested, the same instance of PostFakeApi will be used
   * throughout the application, ensuring consistent data for testing scenarios.
   *
   * @return A Singleton instance of FirebasePostApi.
   */
  @Provides
  @Singleton
  fun provideFileApi(firebaseFileApi: FirebaseFileApi): FileApi = firebaseFileApi

  @Provides
  @Singleton
  fun provideUserApi(): UserApi {
    return FirebaseUserApi()
  }

}
