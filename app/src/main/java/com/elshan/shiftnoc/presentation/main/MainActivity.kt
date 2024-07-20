package com.elshan.shiftnoc.presentation.main

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elshan.shiftnoc.navigation.NavGraph
import com.elshan.shiftnoc.notification.RequestExactAlarmPermissionDialog
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.main.components.ShowAutostartInstructionsDialogIfNeeded
import com.elshan.shiftnoc.presentation.onboarding.OnBoardingScreen
import com.elshan.shiftnoc.presentation.viewmodel.CalendarViewModel
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.CustomSnackbarHost
import com.elshan.shiftnoc.util.DIALOGS
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val calendarViewModel by viewModels<CalendarViewModel>()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !calendarViewModel.appState.value.isReady
            }
        }
        enableEdgeToEdge()

        setContent {
            val calendarState by calendarViewModel.appState.collectAsStateWithLifecycle()
            ShiftnocTheme(
                dynamicColor = false
            ) {

                val snackbarHostState = calendarState.snackbarManager?.snackbarHostState
                    ?: remember { SnackbarHostState() }

                Scaffold(
                    snackbarHost = {
                        CustomSnackbarHost(
                            hostState = snackbarHostState,
                            onActionClick = { }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val padding = innerPadding

                    if (calendarState.visibleDialogs.contains(DIALOGS.SHOW_AUTOSTART_INSTRUCTIONS)
                        && !calendarState.isAutostartEnabled
                        && Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
                    ) {
                        ShowAutostartInstructionsDialogIfNeeded(
                            onEvent = calendarViewModel::onEvent
                        )
                    }

                    // Handle notification permission
                    HandleNotificationPermission(calendarViewModel)

                    // Handle exact alarm permission for Android 12+
                    HandleExactAlarmPermission(
                        appState = calendarState,
                        onEvent = calendarViewModel::onEvent
                    )

                    Box(
                        Modifier
                            .statusBarsPadding()
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        if (calendarState.isReady) {
                            if (calendarState.onBoardingCompleted) {
                                NavGraph(
                                    appState = calendarState,
                                    onEvent = calendarViewModel::onEvent
                                )
                            } else {
                                OnBoardingScreen {
                                    calendarViewModel.onEvent(CalendarEvent.OnBoardingCompleted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@ExperimentalPermissionsApi
fun HandleNotificationPermission(viewModel: CalendarViewModel) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(permissionState.status.isGranted) {
            viewModel.onNotificationPermissionChanged(permissionState.status.isGranted)
        }
        if (!permissionState.status.isGranted) {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
        viewModel.onEvent(CalendarEvent.ShowDialog(DIALOGS.SHOW_AUTOSTART_INSTRUCTIONS))
    } else {
        LaunchedEffect(Unit) {
            viewModel.createNotificationChannel()
            viewModel.onEvent(CalendarEvent.ShowDialog(DIALOGS.SHOW_AUTOSTART_INSTRUCTIONS))
        }
    }
}

@Composable
fun HandleExactAlarmPermission(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
) {
    if (appState.visibleDialogs.contains(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION) && !appState.isRequestExactAlarmPermissionDialogShown) {
        RequestExactAlarmPermissionDialog(
            onEvent = onEvent
        )
    }
    val context = LocalContext.current
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                delay(1500)
                onEvent(CalendarEvent.ShowDialog(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION))
            }
        }
    }
}