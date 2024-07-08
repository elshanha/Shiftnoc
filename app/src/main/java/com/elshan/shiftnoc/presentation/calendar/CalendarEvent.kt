package com.elshan.shiftnoc.presentation.calendar

import android.content.Context
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.presentation.components.ShiftType
import com.elshan.shiftnoc.presentation.components.WorkPattern
import com.elshan.shiftnoc.util.CalendarView
import com.elshan.shiftnoc.util.Languages
import java.time.DayOfWeek
import java.time.LocalDate

sealed class CalendarEvent {
    data class OnDateSelected(val date: LocalDate) : CalendarEvent()
    data class OnWorkPatternSelected(val workPattern: WorkPattern) : CalendarEvent()
    data class OnFirstDayOfWeekSelected(val firstDayOfWeek: DayOfWeek) : CalendarEvent()
    data class OnCalendarViewChanged(val calendarView: CalendarView) : CalendarEvent()
    data class AddOrUpdateNote(val note: NoteEntity) : CalendarEvent()
    data class DeleteNote(val note: NoteEntity) : CalendarEvent()
    data object DeleteAllNotes : CalendarEvent()
    data class ShowDialog(val dialog: String) : CalendarEvent()
    data class HideDialog(val dialog: String) : CalendarEvent()
    data class ToggleDialog(val dialogKey: String) : CalendarEvent()
    data class OnCustomWorkPatternAdded(val customPattern: WorkPattern) : CalendarEvent()

    data class AddNewPattern(val name: String, val pattern: List<ShiftType>) : CalendarEvent()
    data class UpdateCustomWorkPattern(val oldPattern: WorkPattern, val newPattern: WorkPattern) :
        CalendarEvent()

    data class RemoveCustomWorkPattern(val pattern: WorkPattern) : CalendarEvent()
    data class EditCustomWorkPattern(val oldPattern: WorkPattern, val newPattern: WorkPattern) : CalendarEvent()
    data class SetSelectedPattern(val workPattern: WorkPattern?) : CalendarEvent()

    data object OnBoardingCompleted : CalendarEvent()
    data class SetLanguagePreference(val language: String, val context : Context) : CalendarEvent()

}