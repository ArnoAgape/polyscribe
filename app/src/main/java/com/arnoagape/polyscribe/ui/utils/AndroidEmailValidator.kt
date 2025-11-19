package com.arnoagape.polyscribe.ui.utils

import android.util.Patterns
import javax.inject.Inject

class AndroidEmailValidator @Inject constructor() {
    fun validate(email: String?): Boolean {
        return !email.isNullOrBlank()
                && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}