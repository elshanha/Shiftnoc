package com.elshan.shiftnoc.presentation.onboarding


import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.elshan.shiftnoc.R

data class Page(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @RawRes val lottie: Int
)

val page = listOf(
    Page(
        title = R.string.manage_your_shifts,
        description = R.string.effortlessly_organize_and_customize_your_work_schedule,
        lottie = R.raw.man_calendar
    ),
    Page(
        title = R.string.stay_on_track,
        description = R.string.keep_track_of_your_work_hours_and_schedules_with_ease,
        lottie = R.raw.calendar_trim
    ),
    Page(
        title = R.string.set_reminders,
        description = R.string.never_miss_a_workday_again_with_personalized_reminders,
        lottie = R.raw.calendar_inmotion
    )
)
