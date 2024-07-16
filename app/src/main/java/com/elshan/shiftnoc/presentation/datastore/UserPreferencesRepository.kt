package com.elshan.shiftnoc.presentation.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.elshan.shiftnoc.presentation.components.ShiftType
import com.elshan.shiftnoc.presentation.components.WorkPattern
import com.elshan.shiftnoc.presentation.components.defaultWorkPattern
import com.elshan.shiftnoc.presentation.components.predefinedWorkPatterns
import com.elshan.shiftnoc.presentation.datastore.PreferencesKeys.ONBOARDING_COMPLETED
import com.elshan.shiftnoc.util.CalendarView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type
import java.time.DayOfWeek
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("user_preferences")

object PreferencesKeys {
    val START_DATE = stringPreferencesKey("start_date")
    val WORK_PATTERN = stringPreferencesKey("work_pattern")
    val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
    val CALENDAR_VIEW = stringPreferencesKey("calendar_view")
    val CUSTOM_WORK_PATTERNS = stringPreferencesKey("custom_work_patterns")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val LANGUAGE = stringPreferencesKey("language")
    val SHOW_AUTOSTART_INSTRUCTIONS = booleanPreferencesKey("show_autostart_instructions")
}

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ShiftType::class.java, ShiftTypeSerializer)
        .registerTypeAdapter(WorkPattern::class.java, WorkPatternSerializer)
        .create()

    suspend fun setOnboardingCompleted(context: Context, completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    fun isOnboardingCompleted(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[ONBOARDING_COMPLETED] ?: false // Default to false if not set
            }
    }

    val languagePreference: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "en"
        }

    suspend fun setLanguagePreference(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    val loadStartDate: Flow<LocalDate> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.START_DATE]?.let {
                LocalDate.parse(it)
            } ?: LocalDate.now() // Default to current date if not set
        }

    val loadCustomWorkPatterns: Flow<List<WorkPattern>> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CUSTOM_WORK_PATTERNS]?.let { json ->
                gson.fromJson(
                    json,
                    object : TypeToken<List<WorkPattern>>() {}.type
                )
            } ?: emptyList()
        }

    suspend fun saveCustomWorkPatterns(patterns: List<WorkPattern>) {
        val json = gson.toJson(patterns, object : TypeToken<List<WorkPattern>>() {}.type)
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_WORK_PATTERNS] = json
        }
    }


    suspend fun removeCustomWorkPattern(pattern: WorkPattern) {
        val currentPatterns = loadCustomWorkPatterns.first()
        val updatedPatterns = currentPatterns.filterNot { it.name == pattern.name }
        saveCustomWorkPatterns(updatedPatterns)
    }

    suspend fun editCustomWorkPattern(oldPattern: WorkPattern, newPattern: WorkPattern) {
        val currentPatterns = loadCustomWorkPatterns.first()
        val updatedPatterns = currentPatterns.map {
            if (it.name == oldPattern.name) newPattern else it
        }
        saveCustomWorkPatterns(updatedPatterns)
    }

    val loadWorkPattern: Flow<WorkPattern> = combine(
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WORK_PATTERN]
        },
        loadCustomWorkPatterns
    ) { workPatternName, customPatterns ->
        val allPatterns = predefinedWorkPatterns + customPatterns
        val workPattern =
            allPatterns.find { pattern -> pattern.name == workPatternName } ?: defaultWorkPattern
        Log.d("Datastore", "loadWorkPattern: $workPatternName")
        workPattern
    }


    suspend fun saveWorkPattern(pattern: WorkPattern) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORK_PATTERN] = pattern.name
            Log.d("Datastore", "saveWorkPattern: ${pattern.name}")
        }
    }

    suspend fun saveStartDate(date: LocalDate) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.START_DATE] = date.toString()
        }
    }


    val loadFirstDayOfWeek: Flow<DayOfWeek> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_DAY_OF_WEEK]?.let { DayOfWeek.valueOf(it) }
                ?: DayOfWeek.MONDAY // Default to Monday if not set
        }

    suspend fun saveFirstDayOfWeek(dayOfWeek: DayOfWeek) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] = dayOfWeek.name
        }
    }

    val loadCalendarView: Flow<CalendarView> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CALENDAR_VIEW]?.let {
                CalendarView.valueOf(it)
            } ?: CalendarView.VERTICAL_MONTHLY
        }

    suspend fun saveCalendarView(view: CalendarView) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALENDAR_VIEW] = view.name
        }
    }

    suspend fun saveShowAutostartInstructions(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_AUTOSTART_INSTRUCTIONS] = show
        }
    }

    val loadAutostartInstructions: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_AUTOSTART_INSTRUCTIONS] ?: false
        }

}

object ShiftTypeSerializer : JsonSerializer<ShiftType>, JsonDeserializer<ShiftType> {
    override fun serialize(
        src: ShiftType,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.name)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ShiftType {
        return ShiftType.valueOf(json.asString)
    }
}

object WorkPatternSerializer : JsonSerializer<WorkPattern>, JsonDeserializer<WorkPattern> {
    override fun serialize(
        src: WorkPattern,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", src.name)
        jsonObject.addProperty("isCustom", src.isCustom)

        val patternArray = JsonArray()
        src.pattern.forEach { shiftType ->
            patternArray.add(context.serialize(shiftType))
        }
        jsonObject.add("pattern", patternArray)

        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): WorkPattern {
        val jsonObject = json.asJsonObject
        val name = jsonObject.get("name").asString
        val isCustom = jsonObject.get("isCustom").asBoolean

        val patternArray = jsonObject.getAsJsonArray("pattern")
        val pattern = patternArray.map { context.deserialize<ShiftType>(it, ShiftType::class.java) }

        return WorkPattern(
            name = name,
            pattern = pattern,
            isCustom = isCustom
        )
    }
}