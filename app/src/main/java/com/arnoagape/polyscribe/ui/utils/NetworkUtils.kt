package com.arnoagape.polyscribe.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.common.Event
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class checking network availability using system capabilities.
 */
@Singleton
class NetworkUtils @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /**
     * Returns true if the device has an active internet connection.
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Sends a no-network event if no internet connection is available.
     */
    fun checkNetwork(networkUtils: NetworkUtils, events: Channel<Event>) {
        if (!networkUtils.isNetworkAvailable()) {
            events.trySend(Event.ShowMessage(R.string.no_network))
            return
        }
    }
}