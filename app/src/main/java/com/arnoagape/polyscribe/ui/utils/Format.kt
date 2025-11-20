package com.arnoagape.polyscribe.ui.utils

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Format {

    fun getLocalizedDateParts(value: Any): Pair<String, String> {
        val locale = Locale.getDefault()

        val instant: Instant = when (value) {
            is Timestamp -> value.toDate().toInstant()
            is Instant -> value
            else -> error("Unsupported type: ${value::class.simpleName}. Expected Timestamp or Instant.")
        }

        val zoned = instant.atZone(ZoneId.systemDefault())
        val localDate = zoned.toLocalDate()
        val localTime = zoned.toLocalTime()

        return if (locale.language == "fr") {
            val date = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
                .format(localDate)
            val time = DateTimeFormatter.ofPattern("HH'h'mm", locale)
                .format(localTime)
            date to time
        } else {
            val day = localDate.dayOfMonth
            val suffix = getEnglishOrdinalSuffix(day)

            val date = DateTimeFormatter.ofPattern("MMMM d'$suffix' yyyy", locale)
                .format(localDate)

            val isAmPm = locale.country in listOf("US", "CA", "GB", "AU", "NZ")
            val timeFormatter = if (isAmPm)
                DateTimeFormatter.ofPattern("h:mm a", locale)
            else
                DateTimeFormatter.ofPattern("HH:mm", locale)

            val time = timeFormatter.format(localTime)

            date to time
        }
    }


    fun getEnglishOrdinalSuffix(day: Int): String = when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}