package com.elshan.shiftnoc.presentation.components

import android.util.Log
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.VacationDays
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.atStartOfMonth
import java.time.LocalDate
import java.time.Month

class IncomeCalculations(
    var appState: AppState
) {

    fun countDayTypesInMonth(
        year: Int,
        month: Month,
        workPattern: WorkPattern = appState.selectedPattern ?: defaultWorkPattern,
        startDate: LocalDate = appState.startDate ?: LocalDate.of(2024, 1, 1),
        vacations: List<VacationDays> = appState.vacations
    ): Map<DayType, Int> {

        val counts = mutableMapOf<DayType, Int>().apply {
            DayType.entries.forEach { put(it, 0) }
        }

        val startOfMonth = LocalDate.of(year, month, 1)
        val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)

        var currentDate = startOfMonth
        while (!currentDate.isAfter(endOfMonth)) {
            val combinedDayType = getCombinedDayType(
                currentDate,
                workPattern,
                startDate,
                vacations,
                appState.holidaysList
            )

            val dayType = combinedDayType.workDayType
            if (dayType != null) {
                counts[dayType] = counts[dayType]!! + 1

                if (combinedDayType.isHoliday) {
                    counts[DayType.HOLIDAY] = counts[DayType.HOLIDAY]!! + 1
                }
            }

            currentDate = currentDate.plusDays(1)
        }

        Log.d("listOf", counts.toString())

        return mapOf(
            DayType.WORK_MORNING to counts[DayType.WORK_MORNING]!!,
            DayType.WORK_NIGHT to counts[DayType.WORK_NIGHT]!!,
            DayType.WORK_OFF to counts[DayType.WORK_OFF]!!,
            DayType.VACATION to counts[DayType.VACATION]!!,
            DayType.HOLIDAY to counts[DayType.HOLIDAY]!!
        )
    }


    fun calculateMonthlyIncome(
        onPaper: Double = appState.onPaper,
        mornings: Int = appState.dayCount[DayType.WORK_MORNING] ?: 0,
        nights: Int = appState.dayCount[DayType.WORK_NIGHT] ?: 0,
        vacations: List<VacationDays> = appState.vacations,
        holidays: List<LocalDate> = appState.holidaysList
    ): Double {

        val workPattern = appState.selectedPattern ?: defaultWorkPattern
        var totalDays = mornings + nights
        vacations.forEach { vacation ->
            if (vacation.startDate !in appState.selectedMonth.yearMonth.atStartOfMonth()..appState.selectedMonth.yearMonth.atEndOfMonth()
                || vacation.endDate !in appState.selectedMonth.yearMonth.atStartOfMonth()..appState.selectedMonth.yearMonth.atEndOfMonth()
            ) {
                return@forEach
            }
            val overlapCount = countVacationOverlapWithWorkdays(
                vacation = vacation,
                workPattern = workPattern,
                startDate = vacation.startDate
            )
            totalDays += overlapCount
            Log.d("Vacations", "totalDays: $totalDays")
        }

        // Calculate the actual days after applying vacations (mornings + nights already reflect vacations)
        val actualDays = mornings + nights

        // Income calculation based on actual and total days
        val result: Double = if (totalDays != 0) (actualDays.toDouble() / totalDays) else 0.0
        val actualIncome = result * onPaper
        val hours = 10
        val totalHoursPerMonth = actualDays * hours
        val incomePerHour: Double =
            if (totalHoursPerMonth != 0) actualIncome / totalHoursPerMonth else 0.0

        // Evening and night income calculation
        val eveningHours = (nights * (hours - 8)) + 2
        val nightHours = nights * hours

        val eveningIncome = (eveningHours * incomePerHour) / 5
        val nightIncome = (nightHours * incomePerHour) * 0.4

        // Calculate holiday income
        val holidayIncome = calculateHolidayIncome(
            holidays = holidays,
            morningWorkDays = appState.morningWorkDays,
            nightWorkDays = appState.nightWorkDays,
            incomePerHour = incomePerHour
        )

        // Add holiday income to regular income
        val grossIncome: Double =
            (eveningIncome + nightIncome) + actualIncome + holidayIncome + calculateVacationIncome()

        // Deduction calculations
        val socialInsurance = 6 + (grossIncome - 200) * 0.1
        val unemploymentInsurance = grossIncome * 0.005
        val compulsoryMedicalInsurance = grossIncome * 0.02

        val totalDeductions = socialInsurance + unemploymentInsurance + compulsoryMedicalInsurance

        val netIncome = grossIncome - totalDeductions

        Log.d("Hesablama", netIncome.toString())

        return netIncome
    }


    private fun calculateGrossIncome(
        onPaper: Double = appState.onPaper,
        mornings: Int = appState.dayCount[DayType.WORK_MORNING] ?: 0,
        nights: Int = appState.dayCount[DayType.WORK_NIGHT] ?: 0,
        vacations: List<VacationDays> = appState.vacations,
        holidays: List<LocalDate> = appState.holidaysList
    ): Double {

        val totalDays = mornings + nights

        val hours = 10
        val actualDays = mornings + nights
        val result: Double = if (totalDays != 0) (actualDays.toDouble() / totalDays) else 0.0
        val actualIncome = result * onPaper
        val totalHoursPerMonth = actualDays * hours

        val incomePerHour: Double =
            if (totalHoursPerMonth != 0) actualIncome / totalHoursPerMonth else 0.0

        val eveningHours = (nights * (hours - 8)) + 2
        val nightHours = nights * hours

        val eveningIncome = (eveningHours * incomePerHour) / 5
        val nightIncome = (nightHours * incomePerHour) * 0.4

        // Calculate holiday income
        val holidayIncome = calculateHolidayIncome(
            holidays = holidays,
            morningWorkDays = appState.morningWorkDays,
            nightWorkDays = appState.nightWorkDays,
            incomePerHour = incomePerHour
        )

        val grossIncome: Double = (eveningIncome + nightIncome) + actualIncome + holidayIncome
        Log.d("Hesablama", "Gross income: $grossIncome")
        return grossIncome
    }

    private fun calculateTwelveMonthsIncome(
        selectedMonth: CalendarMonth = appState.selectedMonth
    ): Double {
        var totalIncome = 0.0

        val selectedYear = selectedMonth.yearMonth.year
        val selectedMonthValue = selectedMonth.yearMonth.month.value

        // Loop through the last 12 months
        for (i in 0..11) {
            // Calculate the month and year for the current iteration
            val currentMonth = selectedMonthValue - i
            val currentYear = selectedYear - if (currentMonth <= 0) 1 else 0
            val actualMonth = if (currentMonth <= 0) 12 + currentMonth else currentMonth

            // Get the day counts for the current month
            val dayCounts = countDayTypesInMonth(
                year = currentYear,
                month = Month.of(actualMonth)
            )

            // Calculate the income for the current month
            val monthlyIncome = calculateGrossIncome(
                onPaper = appState.onPaper,
                mornings = dayCounts[DayType.WORK_MORNING] ?: 0,
                nights = dayCounts[DayType.WORK_NIGHT] ?: 0,
            )

            // Add to total income
            totalIncome += monthlyIncome
        }

        Log.d("Hesablama", "Total gross income for last 12 months: $totalIncome")
        return totalIncome
    }


    private fun calculateVacationIncome(
        selectedMonth: CalendarMonth = appState.selectedMonth
    ): Double {
        val twelveMonthsIncome = calculateTwelveMonthsIncome()
        val totalHoursPerYear = 364.8
        val result = twelveMonthsIncome / totalHoursPerYear

        val count = countDayTypesInMonth(
            year = selectedMonth.yearMonth.year,
            month = selectedMonth.yearMonth.month
        )
        val totalVacationDays = count[DayType.VACATION] ?: 0

        val vacationIncome = result * totalVacationDays

        Log.d("Vacations", "Vacation size: $totalVacationDays")
        Log.d("Vacations", "Vacation income: $vacationIncome")

        return vacationIncome
    }


    private fun calculateHolidayIncome(
        holidays: List<LocalDate> = appState.holidaysList,
        morningWorkDays: List<LocalDate> = appState.morningWorkDays,
        nightWorkDays: List<LocalDate> = appState.nightWorkDays,
        incomePerHour: Double = 5.3
    ): Double {
        var totalHolidayIncome = 0.0

        holidays.forEachIndexed { index, holiday ->
            // Case 1: Holiday coincides with a morning workday
            if (morningWorkDays.contains(holiday)) {
                totalHolidayIncome += 10 * incomePerHour
            }

            // Case 2: Holiday coincides with a night workday
            if (nightWorkDays.contains(holiday)) {
                totalHolidayIncome += 2 * incomePerHour
            }

            // Case 3: Holiday is the day after a night workday
            if (nightWorkDays.contains(holiday.minusDays(1))) {
                totalHolidayIncome += 8 * incomePerHour
            }

            // Case 4: Handle consecutive holidays
            // If there is a next holiday, check if the next day is also a holiday
            if (index < holidays.size - 1 && holidays[index + 1] == holiday.plusDays(1)) {
                // Next day is a holiday; we already handled consecutive workday-related logic
                // So we don't need special handling here unless additional rules are needed.
            }
        }
        Log.d("Holidays", "holiday income is $totalHolidayIncome")
        return totalHolidayIncome
    }



}

