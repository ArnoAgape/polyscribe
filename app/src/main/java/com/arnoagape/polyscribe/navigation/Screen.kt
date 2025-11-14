package com.arnoagape.polyscribe.navigation

import kotlinx.serialization.Serializable

/**
 * Defines all navigation destinations for the app.
 * Each destination is annotated with @Serializable to enable
 * type-safe navigation using the new Navigation Compose APIs (2.7+).
 */
@Serializable
data class Detail(val fileId: String)

@Serializable
object Home

@Serializable
object Login

@Serializable
object Profile

@Serializable
object Send

@Serializable
object Settings