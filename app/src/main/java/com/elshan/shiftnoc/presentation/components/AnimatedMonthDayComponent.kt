package com.elshan.shiftnoc.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.screen.calendar.components.MonthDayComponent
import com.kizitonwose.calendar.core.CalendarDay

@Composable
fun AnimatedMonthDayComponent(
    day: CalendarDay,
    shiftType: ShiftType?,
    onClick: (CalendarDay) -> Unit,
    appState: AppState
) {
    val visible = appState.isVisible

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + expandIn(expandFrom = Alignment.Center),
        exit = fadeOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        MonthDayComponent(
            day = day,
            shiftType = shiftType,
            onClick = onClick,
            appState = appState
        )
    }
}
