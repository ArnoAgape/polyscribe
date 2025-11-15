package com.arnoagape.polyscribe.ui.screen.send

import android.net.Uri
import java.time.LocalDate
import java.time.LocalTime

/**
 * A sealed class representing different events that can occur on a form.
 */
sealed class FormEvent {

    data class DateChanged(val date: LocalDate) : FormEvent()
    data class TimeChanged(val time: LocalTime) : FormEvent()
    data class ColorChanged(val isColored: Boolean) : FormEvent()
    data class DoubleSidedChanged(val isDoubleSided: Boolean) : FormEvent()
    data class NumberOfCopiesChanged(val delta: Int) : FormEvent()
    data class AddFile(val uri: Uri) : FormEvent()
    data class RemoveFile(val uri: Uri) : FormEvent()
    data class AddPicture(val uri: Uri) : FormEvent()
    data class RemovePicture(val uri: Uri) : FormEvent()
    data class CommentChanged(val comment: String) : FormEvent()

}