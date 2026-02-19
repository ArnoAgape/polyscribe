package com.arnoagape.polyscribe.ui.common

import com.arnoagape.polyscribe.domain.model.SessionType

/**
 * Represents one-time UI events sent from ViewModels to the UI layer.
 *
 * These events are transient actions such as showing messages or
 * triggering navigation, and are not part of the persistent UI state.
 */
sealed interface Event {

    /**
     * Shows a message to the user using a string resource.
     */
    data class ShowMessage(val message: Int) : Event

    /**
     * Shows a message to the user using a string resource.
     */
    data class ErrorUploadFiles(val message: Int) : Event

    /**
     * Shows a success message to the user using a string resource.
     */
    data class ShowSuccessMessage(val message: Int) : Event

    data class FileSentSuccessfully(val sessionType: SessionType) : Event
}
