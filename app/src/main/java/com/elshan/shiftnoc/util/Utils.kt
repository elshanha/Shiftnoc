package com.elshan.shiftnoc.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.flow.filter
import java.time.DayOfWeek
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

object LocaleManager {
    fun setLocale(context: Context, language: String = "en"):Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}

fun updateLocale(context: Context, selectedLocale: String) {
    val updatedContext = LocaleManager.setLocale(context, selectedLocale)

    context.resources.updateConfiguration(
        updatedContext.resources.configuration,
        updatedContext.resources.displayMetrics
    )
}



fun YearMonth.displayText(short: Boolean = true): String {
    return if (this.year == Year.now().value) {
        this.month.displayText(short = short)
    } else {
        "${this.month.displayText(short = short)} ${this.year}"
    }
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.FULL else TextStyle.SHORT
    return getDisplayName(style, Locale.getDefault())
}


fun truncateDecimalPlaces(str: String): String {
    val indexOfDot = str.indexOf(".")
    if (indexOfDot < 0) {
        return str // No decimal point found
    }


    if (indexOfDot + 1 >= str.length) {
        return str
    }

    val truncatedDecimalPart = str.substring(indexOfDot + 1, minOf(indexOfDot + 3, str.length))

    return str.substring(0, indexOfDot + 1) + truncatedDecimalPart
}

