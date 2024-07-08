package com.elshan.shiftnoc.presentation.onboarding


import androidx.annotation.RawRes
import com.elshan.shiftnoc.R

data class Page(
    val title: String,
    val description: String,
    @RawRes val lottie: Int
)

val page = listOf(
    Page(
        title = "Manage Your Shifts",
        description = "Effortlessly organize and customize your work schedule.",
        lottie = R.raw.man_calendar
    ),
    Page(
        title = "Stay on Track",
        description = "Keep track of your work hours and schedules with ease.",
        lottie = R.raw.calendar_trim
    ),
    Page(
        title = "Set Reminders",
        description = "Never miss a workday again with personalized reminders.",
        lottie = R.raw.calendar_inmotion
    )
)
