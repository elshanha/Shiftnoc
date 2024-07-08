package com.elshan.shiftnoc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.screen.calendar.FullScreenCalendar
import com.elshan.shiftnoc.presentation.screen.settings.SettingsScreen

@Composable
fun NavGraph(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit
) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.FullScreenCalendar) {

        composable<Screen.FullScreenCalendar> {
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
    }

}