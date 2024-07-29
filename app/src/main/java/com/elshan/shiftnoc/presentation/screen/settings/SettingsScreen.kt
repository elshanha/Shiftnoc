package com.elshan.shiftnoc.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.screen.settings.components.CalendarViewSelector
import com.elshan.shiftnoc.presentation.screen.settings.components.ColorsSelector
import com.elshan.shiftnoc.presentation.screen.settings.components.LanguageSelector
import com.elshan.shiftnoc.presentation.screen.settings.components.SettingsComponent
import com.elshan.shiftnoc.presentation.screen.settings.components.SettingsItem
import com.elshan.shiftnoc.presentation.screen.settings.components.WeekSelectorDialog
import com.elshan.shiftnoc.util.enums.Languages
import com.elshan.shiftnoc.util.updateLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onEvent: (CalendarEvent) -> Unit,
    appState: AppState,
    navController: NavController
) {
    val context = LocalContext.current

    var showBottomSheetById by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState()


    val settingsGeneral = listOf(
        SettingsItem(
            title = stringResource(R.string.language_selector),
            onClick = {
                showBottomSheetById = R.string.language_selector
            },
            icon = Icons.Outlined.Language,
        ),
        SettingsItem(
            title = stringResource(R.string.select_first_day_of_the_week),
            onClick = {
                showBottomSheetById = R.string.week_selector
            },
            icon = Icons.Outlined.CalendarToday,
        ),
        SettingsItem(
            title = stringResource(R.string.calendar_view),
            onClick = {
                showBottomSheetById = R.string.calendar_view
            },
            icon = Icons.Outlined.CalendarMonth,
        ),
        SettingsItem(
            title = stringResource(R.string.color_picker),
            onClick = {
                showBottomSheetById = R.string.color_picker
            },
            icon = Icons.Outlined.ColorLens,
        )
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->

        if (showBottomSheetById != 0) {
            ModalBottomSheet(
                dragHandle = {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
                },
                onDismissRequest = {
                    showBottomSheetById = 0
                },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    when (showBottomSheetById) {
                        R.string.language_selector -> {
                            LanguageSelector(defaultLanguage = appState.language) {
                                onEvent(CalendarEvent.SetLanguagePreference(it, context))
                                updateLocale(context, it)
                            }
                        }

                        R.string.week_selector -> {
                            WeekSelectorDialog(dayOfWeek = appState.firstDayOfWeek) {
                                onEvent(it)
                                showBottomSheetById = 0
                            }
                        }

                        R.string.calendar_view -> {
                            CalendarViewSelector(selectedCalendarView = appState.calendarView) {
                                onEvent(it)
                                showBottomSheetById = 0
                            }
                        }

                        R.string.color_picker -> {
                            ColorsSelector(
                                selectedDayColors = appState.selectedDayColor,
                                onEvent = {
                                    onEvent(it)
                                    showBottomSheetById = 0
                                },
                            )
                        }
                    }
                }
            }
        }


        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SettingsComponent(
                    categoryTitle = stringResource(R.string.general),
                    categoryList = settingsGeneral
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        onEvent = {},
        appState = AppState(),
        navController = NavController(LocalContext.current)
    )
}