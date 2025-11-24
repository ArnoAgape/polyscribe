package com.arnoagape.polyscribe.ui.common

/**
 * Represents one-time UI events used to communicate from ViewModels to the UI layer.
 *
 * This sealed interface defines events that are not part of the persistent state
 * (e.g., navigation, toast messages, snack bars).
 *
 * Currently, it supports:
 * - [ShowMessage]: Displays a short message using a string resource.
 */
sealed interface Event {

    /**
     * Event used to display a message to the user.
     *
     * @param message The string resource ID of the message to display.
     */
    data class ShowMessage(val message: Int) : Event
    data class ShowSuccessMessage(val message: Int) : Event
}
