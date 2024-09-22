package com.elshan.shiftnoc.util.holiday

import java.time.LocalDate
import java.time.MonthDay

val staticHolidays: List<Pair<MonthDay, String>> = listOf(
    MonthDay.of(1, 1) to "New Year's Day",
    MonthDay.of(1, 2) to "New Year's Holiday (Second day)",
    MonthDay.of(1, 20) to "Martyrs' Day",
    MonthDay.of(3, 8) to "Women's Day",
    MonthDay.of(3, 20) to "Novruz Day 1",
    MonthDay.of(3, 21) to "Novruz Day 2",
    MonthDay.of(3, 22) to "Novruz Day 3",
    MonthDay.of(3, 23) to "Novruz Day 4",
    MonthDay.of(3, 24) to "Novruz Day 5",
    MonthDay.of(5, 9) to "Victory Day over Fascism",
    MonthDay.of(5, 28) to "Republic Day",
    MonthDay.of(6, 15) to "National Salvation Day",
    MonthDay.of(6, 26) to "Armed Forces Day",
    MonthDay.of(10, 18) to "National Independence Day",
    MonthDay.of(11, 8) to "Victory Day of Azerbaijan",
    MonthDay.of(11, 9) to "Flag Day",
    MonthDay.of(11, 12) to "Constitution Day",
    MonthDay.of(11, 17) to "National Revival Day",
    MonthDay.of(12, 31) to "World Azerbaijanis Solidarity Day"
)

val eidAlFitrDates = mapOf(
    2023 to listOf(LocalDate.of(2023, 4, 21), LocalDate.of(2023, 4, 22)),
    2024 to listOf(LocalDate.of(2024, 4, 10), LocalDate.of(2024, 4, 11)),
    // Add more years here
)

val eidAlAdhaDates = mapOf(
    2023 to listOf(LocalDate.of(2023, 6, 28), LocalDate.of(2023, 6, 29)),
    2024 to listOf(LocalDate.of(2024, 6, 16), LocalDate.of(2024, 6, 17)),
    // Add more years here
)

fun getDynamicHolidaysForYear(year: Int): List<LocalDate> {
    val eidAlFitr = eidAlFitrDates[year] ?: emptyList()
    val eidAlAdha = eidAlAdhaDates[year] ?: emptyList()

    return eidAlFitr + eidAlAdha
}

fun getAllHolidaysForYear(year: Int): List<LocalDate> {
    val staticHolidaysForYear = staticHolidays.map {
        LocalDate.of(year, it.first.monthValue, it.first.dayOfMonth)
    }
    val dynamicHolidaysForYear = getDynamicHolidaysForYear(year)

    return staticHolidaysForYear + dynamicHolidaysForYear
}

fun getAllHolidaysForYearRange(currentYear: Int): List<LocalDate> {
    val currentYearHolidays = getAllHolidaysForYear(currentYear)
    val nextYearHolidays = getAllHolidaysForYear(currentYear + 1)
    return currentYearHolidays + nextYearHolidays
}


fun getHolidaysForMonth(year: Int, month: Int): List<LocalDate> {
    return getAllHolidaysForYear(year).filter { it.monthValue == month }
}