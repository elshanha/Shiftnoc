package com.elshan.shiftnoc.presentation.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.domain.repository.MainRepository
import com.elshan.shiftnoc.notification.local.AlarmReceiver
import com.elshan.shiftnoc.notification.local.NotificationsService
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.calendar.VacationDays
import com.elshan.shiftnoc.presentation.components.DayType
import com.elshan.shiftnoc.presentation.components.ShiftType
import com.elshan.shiftnoc.presentation.components.WorkPattern
import com.elshan.shiftnoc.presentation.datastore.UserPreferencesRepository
import com.elshan.shiftnoc.util.SnackbarManager
import com.elshan.shiftnoc.util.enums.CalendarView
import com.elshan.shiftnoc.util.enums.DateKind
import com.elshan.shiftnoc.util.enums.DateSort
import com.elshan.shiftnoc.util.updateLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
        _appState.update { currentState ->
            currentState.copy(snackbarManager = SnackbarManager(viewModelScope))
        }
    }


    private fun loadPreferences() {
        loadStartDate()
        loadVacationStartDate()
        loadVacationEndDate()
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
        loadAutostartInstructionsShown()
        loadRequestExactAlarmPermission()
        loadVacations()
        loadDayColors()
        loadIncomeOnPaper()
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


    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.OnDateSelected -> saveDate(event.date, event.dateKind)
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

            is CalendarEvent.ToggleFullScreen -> {
                _appState.update { it.copy(isFullScreen = !it.isFullScreen) }
            }

            is CalendarEvent.SetAutostartInstructionsShown -> saveAutostartInstructionsShown()
            is CalendarEvent.SetRequestExactAlarmPermission -> saveRequestExactAlarmPermission()
            is CalendarEvent.ShowSnackBar -> showCustomSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = event.duration ?: SnackbarDuration.Short,
                onAction = event.onAction
            )

            is CalendarEvent.SaveVacations -> saveVacations(event.vacations)
            is CalendarEvent.OnVacationSelected -> onVacationSelected(
                event.date,
                event.dateSort
            )

            is CalendarEvent.DeleteVacation -> deleteVacation(event.vacation)
            is CalendarEvent.ShowToast -> showToast(event.message)
            is CalendarEvent.SaveDayColor -> saveDayColor(event.dayType, event.color)
            is CalendarEvent.ResetDayColors -> resetDayColors()
            is CalendarEvent.SaveIncome -> saveIncomeOnPaper(event.income)
        }
    }

    private fun onVacationSelected(date: LocalDate, dateSort: DateSort) {
        val selectedVacation = appState.value.selectedVacation
        val updatedVacation = when (dateSort) {
            DateSort.VACATION_START -> selectedVacation?.copy(startDate = date)
            DateSort.VACATION_END -> selectedVacation?.copy(endDate = date)
            else -> selectedVacation
        }

        if (updatedVacation != null && updatedVacation.startDate.isAfter(updatedVacation.endDate)) {
            Toast.makeText(
                app.applicationContext,
                app.applicationContext.getString(R.string.vacation_end_date_must_be_after_vacation_start_date),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            _appState.update {
                it.copy(
                    selectedVacation = updatedVacation ?: VacationDays(
                        startDate = date,
                        endDate = date,
                        description = ""
                    ),
                )
            }
        }
    }

    private fun saveVacations(vacations: List<VacationDays>) {
        viewModelScope.launch {
            userPreferencesRepository.saveVacations(vacations)
            _appState.update {
                it.copy(
                    vacations = vacations,
                    selectedVacation = null
                )
            }
        }
    }

    private fun loadVacations() {
        viewModelScope.launch {
            userPreferencesRepository.loadVacations.collect { vacations ->
                Log.d("CalendarViewModel", "Loaded vacations: $vacations")
                _appState.update { it.copy(vacations = vacations) }
            }
        }
    }

    private fun deleteVacation(vacation: VacationDays) {
        val updatedVacations = appState.value.vacations.toMutableList().apply {
            remove(vacation)
        }
        Toast.makeText(
            app.applicationContext,
            app.applicationContext.getString(R.string.vacation_deleted),
            Toast.LENGTH_SHORT
        ).show()
        saveVacations(updatedVacations)
    }

    private fun showCustomSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null
    ) {
        _appState.value.snackbarManager?.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
            onAction = onAction
        )
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

    private fun showNotification(note: NoteEntity) {
        val context = app.applicationContext
        viewModelScope.launch {
            notificationsService.showNotification(
                note = note,
                onPermissionError = {
                    showCustomSnackbar(
                        message = app.applicationContext.getString(R.string.please_grant_the_permission_in_the_settings),
                        actionLabel = app.applicationContext.getString(R.string.settings),
                        duration = SnackbarDuration.Short,
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    val intent =
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            putExtra(
                                                Settings.EXTRA_APP_PACKAGE,
                                                context.packageName
                                            )
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    context.startActivity(intent)
                                    viewModelScope.launch {
                                        delay(1000)
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.please_allow),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    )
                }
            )
        }
    }

    private fun addOrUpdateNoteForDate(date: LocalDate, note: NoteEntity) {
        viewModelScope.launch {
            val notesForDate = mainRepository.getNotesForDate(date).firstOrNull() ?: emptyList()
            if (notesForDate.size < 3 || note.id != 0L) {
                mainRepository.upsertNote(note)

                // Cancel existing reminder if any
                cancelExistingReminder(note)

                // Schedule new reminder if the reminder is in the future
                scheduleNewReminder(note)
                fetchAllNotes()
            }
        }
    }

    private fun cancelExistingReminder(note: NoteEntity) {
        note.reminder?.let {
            val intent = Intent(app, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                app,
                note.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun scheduleNewReminder(note: NoteEntity) {
        note.reminder?.let { reminder ->
            if (reminder.isAfter(LocalDateTime.now())) {
                showNotification(
                    note = note,
                )
            } else {
                showPastReminderToast()
            }
        }
    }

    private fun showPastReminderToast() {
        showToast(
            app.applicationContext.getString(R.string.reminder_cannot_be_in_the_past)
        )
    }

    private fun rescheduleAllNotifications() {
        viewModelScope.launch {
            val notes = mainRepository.getAllNotes().firstOrNull() ?: emptyList()
            notes.forEach { note ->
                note.reminder?.let { reminder ->
                    if (reminder.isAfter(LocalDateTime.now())) {
                        showNotification(note)
                    }
                }
            }
        }
    }

    fun onNotificationPermissionChanged(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                createNotificationChannel()
                rescheduleAllNotifications()
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
            _appState.update {
                it.copy(
                    customWorkPatterns = updatedPatterns,
                )
            }
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

    private fun saveDate(date: LocalDate, dateKind: DateKind) {
        viewModelScope.launch {
            when (dateKind) {
                DateKind.START_DATE -> {
                    userPreferencesRepository.saveDate(date, DateKind.START_DATE)
                    _appState.update { it.copy(startDate = date) }
                }

                DateKind.REMINDER_DATE -> {}
                DateKind.VACATION_START_DATE -> {
                    userPreferencesRepository.saveDate(date, DateKind.VACATION_START_DATE)
                    _appState.update { it.copy(vacationStartDate = date) }
                }

                DateKind.VACATION_END_DATE -> {
                    userPreferencesRepository.saveDate(date, DateKind.VACATION_END_DATE)
                    _appState.update { it.copy(vacationEndDate = date) }
                }
            }
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

    private fun loadStartDate() {
        viewModelScope.launch {
            userPreferencesRepository.loadStartDate.collect { date ->
                _appState.update { it.copy(startDate = date) }
            }
        }
    }

    private fun loadVacationStartDate() {
        viewModelScope.launch {
            userPreferencesRepository.loadVacationStartDate.collect { date ->
                _appState.update { it.copy(vacationStartDate = date) }
            }
        }
    }

    private fun loadVacationEndDate() {
        viewModelScope.launch {
            userPreferencesRepository.loadVacationEndDate.collect { date ->
                _appState.update { it.copy(vacationEndDate = date) }
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

    private fun saveAutostartInstructionsShown() {
        viewModelScope.launch {
            userPreferencesRepository.saveShowAutostartInstructions(true)
            _appState.update { it.copy(isAutostartEnabled = true) }
        }
    }

    private fun loadAutostartInstructionsShown() {
        viewModelScope.launch {
            userPreferencesRepository.loadAutostartInstructions.collect { autoStart ->
                _appState.update { it.copy(isAutostartEnabled = autoStart) }
            }
        }
    }

    private fun saveRequestExactAlarmPermission() {
        viewModelScope.launch {
            userPreferencesRepository.saveRequestExactAlarmPermission(true)
            _appState.update { it.copy(isRequestExactAlarmPermissionDialogShown = true) }
        }
    }

    private fun loadRequestExactAlarmPermission() {
        viewModelScope.launch {
            userPreferencesRepository.loadRequestExactAlarmPermission.collect { requestPermission ->
                _appState.update { it.copy(isRequestExactAlarmPermissionDialogShown = requestPermission) }
            }
        }
    }

    private fun saveDayColor(dayType: DayType, color: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateDayColor(dayType, color)
            val updatedColors = _appState.value.selectedDayColor.toMutableMap()
            updatedColors[dayType] = color
            _appState.update { it.copy(selectedDayColor = updatedColors) }
        }
    }

    private fun loadDayColors() {
        viewModelScope.launch {
            userPreferencesRepository.loadDayColors().collect { dayColors ->
                _appState.update { it.copy(selectedDayColor = dayColors) }
            }
        }
    }

    private fun resetDayColors() {
        val defaultColors = mapOf(
            DayType.WORK_MORNING to "#A3C9FE",
            DayType.WORK_NIGHT to "#E1E2E8",
            DayType.WORK_OFF to "#00000000",
            DayType.VACATION to "#00D100",
            DayType.HOLIDAY to "#D1D100"
        )
        viewModelScope.launch {
            userPreferencesRepository.resetDayColors(defaultColors)
            _appState.update {
                it.copy(
                    selectedDayColor = defaultColors
                )
            }
        }
    }

    private fun saveIncomeOnPaper(income: Double) {
        viewModelScope.launch {
            userPreferencesRepository.saveIncomeOnPaper(income.toString())
        }
    }

    private fun loadIncomeOnPaper() {
        viewModelScope.launch {
            userPreferencesRepository.loadIncomeOnPaper.collect { income ->
                _appState.update { it.copy(onPaper = income.toDoubleOrNull() ?: 0.0) }
            }
        }
    }


    private fun showToast(
        message: String,
    ) {
        Toast.makeText(
            app.applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

}