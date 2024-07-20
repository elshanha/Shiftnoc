package com.elshan.shiftnoc.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun DatePickerDayComponent(
    day: CalendarDay,
    selected: Boolean,
    indicator: Boolean = true,
    isDatePicker: Boolean = false,
    onClick: (LocalDate) -> Unit = {},
) {

    if (day.position != DayPosition.MonthDate) {
        Box(modifier = Modifier.aspectRatio(1f)) // Empty box for in-dates and out-dates
        return
    }

    val textColor = if (selected) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.primary
    }

    val dayTextStyle = if (isDatePicker) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                if (day.date == LocalDate.now()) 2.dp else (-1).dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick(day.date) }

        ,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = dayTextStyle,
                color = if (DayOfWeek.SUNDAY == day.date.dayOfWeek) MaterialTheme.colorScheme.error else textColor
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (indicator) MaterialTheme.colorScheme.primary else Color.Transparent)
            )
        }
    }
}