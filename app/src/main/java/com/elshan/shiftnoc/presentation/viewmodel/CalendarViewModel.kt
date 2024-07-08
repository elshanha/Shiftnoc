package com.elshan.shiftnoc.presentation.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.domain.repository.MainRepository
import com.elshan.shiftnoc.notification.AlarmReceiver
import com.elshan.shiftnoc.notification.NotificationsService
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.components.ShiftType
import com.elshan.shiftnoc.presentation.components.WorkPattern
import com.elshan.shiftnoc.presentation.components.getAllWorkPatterns
import com.elshan.shiftnoc.presentation.datastore.UserPreferencesRepository
import com.elshan.shiftnoc.util.CalendarView
import com.elshan.shiftnoc.util.updateLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mainRepository: MainRepository,
    private val notificationsService: NotificationsService,
    private val alarmManager: AlarmManager,
    private val app: Application
) : ViewModel() {

    private val _appState = MutableStateFlow(AppState())
    val appState = _appState.asStateFlow()

    init {
        loadPreferences()
        fetchAllNotes()
        checkExactAlarmPermission()
    }

    private fun loadPreferences() {
        getStartDate()
        loadOnboardingPreference()
        getFirstDayOfWeek()
        getCalendarView()
        viewModelScope.launch {
            userPreferencesRepository.loadCustomWorkPatterns.collect { customPatterns ->
                _appState.update { it.copy(customWorkPatterns = customPatterns) }
                getWorkPattern()
            }
        }
        loadLanguagePreference()
    }

    private fun fetchAllNotes() {
        viewModelScope.launch {
            mainRepository.getAllNotes().collect { notes ->
                val notesMap = notes.groupBy { it.date }
                _appState.update {
                    it.copy(
                        notes = notesMap,
                        noteEntity = notesMap.getOrDefault(
                            _appState.value.selectedDate,
                            emptyList()
                        ).find { note ->
                            note.id == _appState.value.noteEntity?.id
                        },
                    )
                }
            }
        }
    }


    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                _appState.update { it.copy(visibleDialogs = _appState.value.visibleDialogs + "exactAlarmPermission") }
            }
        }
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.OnDateSelected -> saveStartDate(event.date)
            is CalendarEvent.OnWorkPatternSelected -> saveWorkPattern(event.workPattern)
            is CalendarEvent.OnFirstDayOfWeekSelected -> saveFirstDayOfWeek(event.firstDayOfWeek)
            is CalendarEvent.OnCalendarViewChanged -> saveCalendarView(event.calendarView)
            is CalendarEvent.AddOrUpdateNote -> addOrUpdateNoteForDate(event.note.date, event.note)
            is CalendarEvent.DeleteNote -> deleteNote(event.note)
            is CalendarEvent.DeleteAllNotes -> deleteAllNotes()
            is CalendarEvent.ShowDialog -> showDialog(event.dialog)
            is CalendarEvent.HideDialog -> hideDialog(event.dialog)
            is CalendarEvent.ToggleDialog -> toggleDialog(event.dialogKey)
            is CalendarEvent.AddNewPattern -> addNewPattern(event.name, event.pattern)

            is CalendarEvent.UpdateCustomWorkPattern -> updateCustomWorkPattern(
                event.oldPattern,
                event.newPattern
            )

            is CalendarEvent.RemoveCustomWorkPattern -> removeCustomWorkPattern(event.pattern)
            is CalendarEvent.OnCustomWorkPatternAdded -> addCustomWorkPattern(event.customPattern)
            is CalendarEvent.EditCustomWorkPattern -> editCustomWorkPattern(
                event.oldPattern,
                event.newPattern
            )

            is CalendarEvent.SetSelectedPattern -> setSelectedPattern(event.workPattern)
            is CalendarEvent.OnBoardingCompleted -> setOnboardingCompleted()
            is CalendarEvent.SetLanguagePreference -> viewModelScope.launch {
                _appState.update { it.copy(language = event.language) }
                updateLocale(
                    app.applicationContext,
                    event.language
                )
                UserPreferencesRepository(event.context).setLanguagePreference(event.language)
            }

        }
    }

    private fun toggleDialog(dialogKey: String) {
        if (_appState.value.visibleDialogs.contains(dialogKey)) {
            hideDialog(dialogKey)
        } else {
            showDialog(dialogKey)
        }
    }

    private fun showDialog(dialogKey: String) {
        _appState.update { it.copy(visibleDialogs = _appState.value.visibleDialogs + dialogKey) }
    }

    private fun hideDialog(dialogKey: String) {
        _appState.update { it.copy(visibleDialogs = _appState.value.visibleDialogs - dialogKey) }
    }

    fun createNotificationChannel() {
        notificationsService.createNotificationChannel()
    }

    private fun showNotification(noteId: Long, content: String, reminder: LocalDateTime) {
        viewModelScope.launch {
            notificationsService.showNotification(noteId, content, reminder)
        }
    }

    fun hideNotification(notificationId: Int) {
        notificationsService.hideNotification(notificationId)
    }

    private fun selectDate(date: LocalDate) {
        _appState.update {
            it.copy(selectedDate = date, noteEntity = null)
        }
    }

    private fun addOrUpdateNoteForDate(date: LocalDate, note: NoteEntity) {
        viewModelScope.launch {
            val notesForDate = mainRepository.getNotesForDate(date).firstOrNull() ?: emptyList()
            if (notesForDate.size < 3 || note.id != 0L) {
                mainRepository.upsertNote(note)

                // Cancel existing reminder if any
                note.reminder?.let { reminder ->
                    val intent = Intent(app, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        app,
                        note.id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(pendingIntent)
                }

                // Schedule new reminder if the reminder is in the future
                note.reminder?.let { reminder ->
                    if (reminder.isAfter(LocalDateTime.now())) {
                        showNotification(note.id, note.content, reminder)
                    } else {
                        Toast.makeText(
                            app.applicationContext,
                            app.applicationContext.getString(R.string.reminder_cannot_be_in_the_past),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                fetchAllNotes()
            } else {

            }
        }
    }


    private fun loadLanguagePreference() {
        viewModelScope.launch {
            userPreferencesRepository.languagePreference.collect { language ->
                _appState.update { it.copy(language = language) }
                updateLocale(app.applicationContext, language)
            }
        }
    }

    fun setLanguagePreference(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguagePreference(language)
        }
    }


    private fun setSelectedPattern(pattern: WorkPattern?) {
        _appState.update { it.copy(selectedPattern = pattern) }
    }


    private fun addNewPattern(name: String, pattern: List<ShiftType>) {
        val newPattern = WorkPattern(
            name = name,
            pattern = pattern,
            isCustom = true
        )
        addCustomWorkPattern(newPattern)
        saveWorkPattern(newPattern)
    }


    private fun addCustomWorkPattern(pattern: WorkPattern) {
        viewModelScope.launch {
            val currentPatterns = userPreferencesRepository.loadCustomWorkPatterns.first()
            val updatedPatterns = currentPatterns + pattern
            userPreferencesRepository.saveCustomWorkPatterns(updatedPatterns)
            _appState.update { it.copy(
                customWorkPatterns = updatedPatterns,
            ) }
        }
    }


    private fun updateCustomWorkPattern(oldPattern: WorkPattern, newPattern: WorkPattern) {
        _appState.update { state ->
            val updatedPatterns = state.customWorkPatterns.toMutableList()
            val index = updatedPatterns.indexOfFirst { it == oldPattern }
            if (index != -1) {
                updatedPatterns[index] = newPattern
                state.copy(customWorkPatterns = updatedPatterns)
            } else {
                state
            }
        }
    }

    private fun editCustomWorkPattern(oldPattern: WorkPattern, newPattern: WorkPattern) {
        viewModelScope.launch {
            userPreferencesRepository.editCustomWorkPattern(oldPattern, newPattern)
            val updatedPatterns = userPreferencesRepository.loadCustomWorkPatterns.first()
            _appState.update { it.copy(customWorkPatterns = updatedPatterns) }
        }
    }

    private fun removeCustomWorkPattern(pattern: WorkPattern) {
        viewModelScope.launch {
            userPreferencesRepository.removeCustomWorkPattern(pattern)
            val updatedPatterns = userPreferencesRepository.loadCustomWorkPatterns.first()
            _appState.update { it.copy(customWorkPatterns = updatedPatterns) }
        }
    }

    fun getAllWorkPatterns(): List<WorkPattern> {
        return getAllWorkPatterns(_appState.value.customWorkPatterns)
    }


    private fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            mainRepository.deleteNote(note)
            fetchAllNotes()
        }
    }

    private fun deleteAllNotes() {
        viewModelScope.launch {
            mainRepository.deleteAllNotes()
            fetchAllNotes()
        }
    }

    private fun loadOnboardingPreference() {
        viewModelScope.launch {
            userPreferencesRepository.isOnboardingCompleted(app.applicationContext)
                .collect { completed ->
                    _appState.update { it.copy(onBoardingCompleted = completed, isReady = true) }
                }
        }
    }

    private fun setOnboardingCompleted() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(app.applicationContext, true)
            _appState.update { it.copy(onBoardingCompleted = true) }
        }
    }

    private fun saveStartDate(date: LocalDate) {
        viewModelScope.launch {
            userPreferencesRepository.saveStartDate(date)
            _appState.update { it.copy(startDate = date) }
        }
    }

    private fun saveWorkPattern(pattern: WorkPattern) {
        viewModelScope.launch {
            userPreferencesRepository.saveWorkPattern(pattern)
            _appState.update { it.copy(selectedPattern = pattern) }
        }
    }

    private fun saveFirstDayOfWeek(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            userPreferencesRepository.saveFirstDayOfWeek(dayOfWeek)
            _appState.update { it.copy(firstDayOfWeek = dayOfWeek) }
        }
    }

    private fun getFirstDayOfWeek() {
        viewModelScope.launch {
            userPreferencesRepository.loadFirstDayOfWeek.collect { dayOfWeek ->
                _appState.update { it.copy(firstDayOfWeek = dayOfWeek) }
            }
        }
    }

    private fun getStartDate() {
        viewModelScope.launch {
            userPreferencesRepository.loadStartDate.collect { date ->
                _appState.update { it.copy(startDate = date) }
            }
        }
    }

    private fun getWorkPattern() {
        viewModelScope.launch {
            userPreferencesRepository.loadWorkPattern.collect { pattern ->
                _appState.update { it.copy(selectedPattern = pattern) }
            }
        }

    }

    private fun saveCalendarView(calendarView: CalendarView) {
        viewModelScope.launch {
            userPreferencesRepository.saveCalendarView(calendarView)
            _appState.update { it.copy(calendarView = calendarView) }
        }
    }

    private fun getCalendarView() {
        viewModelScope.launch {
            userPreferencesRepository.loadCalendarView.collect { calendarView ->
                _appState.update { it.copy(calendarView = calendarView) }
            }
        }
    }

    private fun saveCustomWorkPatterns(patterns: List<WorkPattern>) {
        viewModelScope.launch {
            userPreferencesRepository.saveCustomWorkPatterns(patterns)
            _appState.update { it.copy(customWorkPatterns = patterns) }
        }
    }

    private fun getCustomWorkPatterns() {
        viewModelScope.launch {
            userPreferencesRepository.loadCustomWorkPatterns.collect { customPatterns ->
                _appState.update { it.copy(customWorkPatterns = customPatterns) }
            }
        }
    }
}