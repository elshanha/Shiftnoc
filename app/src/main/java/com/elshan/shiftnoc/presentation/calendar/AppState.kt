package com.elshan.shiftnoc.presentation.calendar

import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.presentation.components.WorkPattern
import com.elshan.shiftnoc.util.CalendarView
import com.elshan.shiftnoc.util.Languages
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

data class AppState(
    val isReady: Boolean = false,
    val onBoardingCompleted: Boolean = false,
    val selectedPattern: WorkPattern? = null,
    var selectedDate: LocalDate? = null,
    var selectedColor: String = "#FFFFFF",
    val startDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val isVisible: Boolean = false,
    var firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val calendarView: CalendarView = CalendarView.VERTICAL_MONTHLY,
    val notes: Map<LocalDate, List<NoteEntity>> = emptyMap(),
    val noteEntity: NoteEntity? = null,
    val noteList: List<NoteEntity> = emptyList(),
    val noteText: String = "",
    val showNoteDialog: Boolean = false,
    val visibleDialogs: Set<String> = emptySet(),
    val customWorkPatterns: List<WorkPattern> = emptyList(),
    val language: String = Locale.getDefault().language

)
