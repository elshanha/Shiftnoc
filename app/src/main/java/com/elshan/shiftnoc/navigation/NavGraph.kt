package com.elshan.shiftnoc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.datastore.UserPreferencesRepository
import com.elshan.shiftnoc.presentation.screen.calendar.FullScreenCalendar
import com.elshan.shiftnoc.presentation.screen.details.DetailsScreen
import com.elshan.shiftnoc.presentation.screen.settings.SettingsScreen
import com.elshan.shiftnoc.util.updateLocale

@Composable
fun NavGraph(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit
) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.FullScreenCalendar) {

        composable<Screen.FullScreenCalendar> {

            val context = LocalContext.current
            LaunchedEffect(Unit) {
                val dataPreference = UserPreferencesRepository(context = context)
                dataPreference.languagePreference.collect {
                    updateLocale(context, it)
                }
            }

            FullScreenCalendar(
                appState = appState,
                onEvent = onEvent,
                navController = navController
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onEvent = onEvent,
                appState = appState,
                navController = navController
            )
        }

        composable<Screen.Details> {
            val args = it.arguments

            DetailsScreen(
                title = args?.getString("title") ?: "",
                appState = appState,
                onEvent = onEvent
            )
        }
    }

}