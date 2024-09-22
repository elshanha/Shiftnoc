package com.elshan.shiftnoc.presentation.components

import android.content.Context
import android.util.Log
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.VacationDays
import com.kizitonwose.calendar.core.CalendarMonth
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
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

enum class DayType(
    val stringResId: Int,
    val shiftType: ShiftType? = null,
    var color: String? = null
) {
    WORK_MORNING(R.string.shift_morning, ShiftType.MORNING),
    WORK_NIGHT(R.string.shift_night, ShiftType.NIGHT),
    WORK_OFF(R.string.shift_off, ShiftType.OFF),
    VACATION(R.string.day_vacation),
    HOLIDAY(R.string.day_holiday)
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

fun ShiftType.getName(context: Context): String {
    return context.getString(this.stringResId)
}

data class CombinedDayType(
    val workDayType: DayType? = null,
    val isHoliday: Boolean = false
)

fun getCombinedDayType(
    date: LocalDate,
    workPattern: WorkPattern,
    startDate: LocalDate,
    vacations: List<VacationDays>,
    holidays: List<LocalDate>
): CombinedDayType {

    // Check if the date is within any vacation range
    vacations.forEach { vacation ->
        if (date in vacation.startDate..vacation.endDate) {
            return CombinedDayType(workDayType = DayType.VACATION)
        }
    }

    if (workPattern.pattern.isEmpty()) {
        return CombinedDayType()
    }

    val daysBetween = ChronoUnit.DAYS.between(startDate, date).toInt()
    val workDayType = if (daysBetween >= 0) {
        val type = workPattern.pattern[daysBetween % workPattern.pattern.size]
        when (type) {
            ShiftType.MORNING -> DayType.WORK_MORNING
            ShiftType.NIGHT -> DayType.WORK_NIGHT
            ShiftType.OFF -> DayType.WORK_OFF
        }
    } else {
        null
    }

    // Determine if it's a holiday
    val isHoliday = holidays.contains(date)

    // Return both the workday type and holiday status
    return CombinedDayType(
        workDayType = workDayType,
        isHoliday = isHoliday
    )
}


class ListOfDays(var appState: AppState) {

    fun getListOfDaysPerMonth(
        year: Int,
        month: Month,
        workPattern: WorkPattern = appState.selectedPattern ?: defaultWorkPattern,
        startDate: LocalDate = appState.startDate ?: LocalDate.of(2024, 1, 1),
        vacations: List<VacationDays> = appState.vacations
    ): List<Pair<LocalDate, DayType?>> {

        val daysInMonth = mutableListOf<Pair<LocalDate, DayType?>>()

        val startOfMonth = LocalDate.of(year, month, 1)
        val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)

        var currentDate = startOfMonth
        while (!currentDate.isAfter(endOfMonth)) {
            val dayType =
                getCombinedDayType(
                    currentDate,
                    workPattern,
                    startDate,
                    vacations,
                    appState.holidaysList
                )
            daysInMonth.add(Pair(currentDate, dayType.workDayType))
            currentDate = currentDate.plusDays(1)
        }

        val morningDays: List<LocalDate> =
            daysInMonth.filter { it.second == DayType.WORK_MORNING }.map { it.first }
        appState.morningWorkDays = morningDays
        val nightDays: List<LocalDate> =
            daysInMonth.filter { it.second == DayType.WORK_NIGHT }.map { it.first }
        appState.nightWorkDays = nightDays

        Log.d("listOfDays", daysInMonth.toString())
        return daysInMonth
    }
}


fun countVacationOverlapWithWorkdays(
    vacation: VacationDays,
    workPattern: WorkPattern,
    startDate: LocalDate
): Int {
    var overlapCount = 0

    var currentDate = vacation.startDate
    while (currentDate <= vacation.endDate) {
        val daysBetween = ChronoUnit.DAYS.between(startDate, currentDate).toInt()

        if (daysBetween >= 0) {
            val type = workPattern.pattern[daysBetween % workPattern.pattern.size]

            if (type == ShiftType.MORNING || type == ShiftType.NIGHT) {
                overlapCount++
            }
        }
        currentDate = currentDate.plusDays(1)
    }

    return overlapCount
}





