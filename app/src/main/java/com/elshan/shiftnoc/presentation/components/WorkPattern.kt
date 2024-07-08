package com.elshan.shiftnoc.presentation.components

import android.content.Context
import com.elshan.shiftnoc.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class WorkPattern(
    val nameResId: Int = R.string.default_work_pattern,
    val name: String,
    val pattern: List<ShiftType>,
    val isCustom: Boolean = true
)


val defaultWorkPattern = WorkPattern(
    nameResId = R.string.default_work_pattern,
    name = "Default",
    pattern = listOf(ShiftType.OFF),
    isCustom = false
)


enum class ShiftType(
    val stringResId: Int,
) {
    MORNING(R.string.shift_morning),
    NIGHT(R.string.shift_night),
    OFF(R.string.shift_off)
}

val predefinedWorkPatterns = listOf(
    defaultWorkPattern,
    WorkPattern(
        nameResId = R.string.pattern_1day_work_1day_off,
        name = "1 day work, 1 day off",
        pattern = listOf(ShiftType.MORNING, ShiftType.OFF),
        isCustom = false
    ),
    WorkPattern(
        nameResId = R.string.pattern_2days_work_2days_off,
        name = "2 days work, 2 days off",
        pattern = listOf(ShiftType.MORNING, ShiftType.NIGHT, ShiftType.OFF, ShiftType.OFF),
        isCustom = false
    ),
    WorkPattern(
        nameResId = R.string.pattern_1day_work_2days_off,
        name = "1 day work, 2 days off",
        pattern = listOf(ShiftType.MORNING, ShiftType.OFF, ShiftType.OFF),
        isCustom = false
    ),
)

fun getAllWorkPatterns(customPatterns: List<WorkPattern>): List<WorkPattern> {
    return predefinedWorkPatterns + customPatterns
}

fun ShiftType.getName(context: Context): String {
    return context.getString(this.stringResId)
}


fun getShiftType(date: LocalDate, workPattern: WorkPattern, startDate: LocalDate): ShiftType? {
    if (workPattern.pattern.isEmpty()) {
        return null // Return null if the pattern list is empty
    }

    val daysBetween = ChronoUnit.DAYS.between(startDate, date).toInt()
    return if (daysBetween >= 0) {
        val type = workPattern.pattern[daysBetween % workPattern.pattern.size]
        type
    } else {
        null
    }
}