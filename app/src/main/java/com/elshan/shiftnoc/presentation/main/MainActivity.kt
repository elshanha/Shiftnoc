package com.elshan.shiftnoc.presentation.main

import android.Manifest
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elshan.shiftnoc.navigation.NavGraph
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.onboarding.OnBoardingScreen
import com.elshan.shiftnoc.presentation.viewmodel.CalendarViewModel
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

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

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val padding = innerPadding


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permissionState =
                            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
                        if (!permissionState.status.isGranted) {
                            LaunchedEffect(key1 = Unit) {
                                permissionState.launchPermissionRequest()
                                calendarViewModel.createNotificationChannel()
                            }
                        }
                    } else {
                        LaunchedEffect(key1 = Unit) {
                            calendarViewModel.createNotificationChannel()
                        }
                    }

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
