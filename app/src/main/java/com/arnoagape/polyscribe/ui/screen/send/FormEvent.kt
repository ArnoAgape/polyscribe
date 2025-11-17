package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri

/**
 * A sealed class representing different events that can occur on a form.
 */
sealed class FormEvent {

    data class DateChanged(val date: String) : FormEvent()
    data class TimeChanged(val time: String) : FormEvent()
    data class ColorChanged(val isColored: Boolean) : FormEvent()
    data class DoubleSidedChanged(val isDoubleSided: Boolean) : FormEvent()
    data class NumberOfCopiesSet(val value: Int) : FormEvent()
    data class AddFile(val uri: Uri) : FormEvent()
    data class RemoveFile(val uri: Uri) : FormEvent()
    data class CommentChanged(val comment: String) : FormEvent()

}