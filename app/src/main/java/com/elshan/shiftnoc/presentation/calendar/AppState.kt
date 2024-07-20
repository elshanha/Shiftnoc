package com.elshan.shiftnoc.presentation.calendar

import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.presentation.components.DayType
import com.elshan.shiftnoc.presentation.components.WorkPattern
import com.elshan.shiftnoc.util.enums.CalendarView
import com.elshan.shiftnoc.util.SnackbarManager
import com.elshan.shiftnoc.util.enums.DateKind
import com.elshan.shiftnoc.util.enums.DateSort
import com.elshan.shiftnoc.util.enums.Vacation
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

data class AppState(
    val isReady: Boolean = false,
    val onBoardingCompleted: Boolean = false,
    val selectedPattern: WorkPattern? = null,
    var selectedDate: LocalDate? = null,
    var vacationStartDate: LocalDate? = null,
    var vacationEndDate: LocalDate? = null,
    var selectedColor: String = "#FFFFFF",
    val startDate: LocalDate? = null,
    var firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val calendarView: CalendarView = CalendarView.VERTICAL_MONTHLY,
    val notes: Map<LocalDate, List<NoteEntity>> = emptyMap(),
    val noteEntity: NoteEntity? = null,
    val visibleDialogs: Set<String> = emptySet(),
    val customWorkPatterns: List<WorkPattern> = emptyList(),
    val language: String = Locale.getDefault().language,
    val isFullScreen: Boolean = false,
    val isAutostartEnabled: Boolean = false,
    val isRequestExactAlarmPermissionDialogShown: Boolean = false,
    val dayType: DayType = DayType.WORK_OFF,
    val dateKind: DateKind = DateKind.START_DATE,
    val vacation: Vacation = Vacation.START,
    val vacations: List<VacationDays> = emptyList(),
    val selectedVacation: VacationDays? = null,
    val vacationDateSort: DateSort = DateSort.NONE,
    val snackbarManager: SnackbarManager? = null,
)
