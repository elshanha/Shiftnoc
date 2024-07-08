package com.elshan.shiftnoc.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.util.displayText
import com.kizitonwose.calendar.core.CalendarMonth
import java.time.Year
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthHeader(month: CalendarMonth) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 20.dp, start = 16.dp, end = 20.dp)
    ) {
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Center,
            text = if (month.yearMonth.year == Year.now().value) {
                month.yearMonth.month.displayText().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            } else {
                month.yearMonth.displayText().replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
            },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MonthBody(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .wrapContentSize()
    ) {
        content()
    }
}