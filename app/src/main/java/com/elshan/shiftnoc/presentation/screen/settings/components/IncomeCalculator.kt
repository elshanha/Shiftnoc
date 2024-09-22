package com.elshan.shiftnoc.presentation.screen.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.DIALOGS

@Composable
fun IncomeCalculator(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
) {

    var incomeOnPaper by remember { mutableStateOf(appState.onPaper.toInt().toString()) }
    var isSaved by remember { mutableStateOf(false) }
    if (incomeOnPaper.toDoubleOrNull() != null && incomeOnPaper.toDouble() != 0.0) {
        isSaved = true
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(end = 65.dp),
                text = stringResource(R.string.add_your_salary),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            IconButton(
                colors = IconButtonDefaults.iconButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                onClick = {
                    onEvent(CalendarEvent.ToggleDialog(DIALOGS.SHOW_INCOME_DATE_DIALOG))
                }
            ) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = "Info")
            }
        }
        HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {

            AnimatedVisibility(
                visible = appState.visibleDialogs.contains(DIALOGS.SHOW_INCOME_DATE_DIALOG),
                enter = slideInVertically() + scaleIn(
                    animationSpec = spring(
                        dampingRatio = DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = slideOutVertically() + scaleOut(
                    animationSpec = spring(
                        dampingRatio = DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            ) {
                Text(
                    modifier = Modifier
                        .padding(12.dp),
                    text = stringResource(R.string.to_see_your_income_for_a_specific_month_please_go_to_the_vertical_calendar_and_select_the_month_you_want_to_see),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable {
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(text = stringResource(R.string.income))
                    },
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    value = incomeOnPaper,
                    onValueChange = {
                        incomeOnPaper = it
                        val newIncome = it.toDoubleOrNull()
                        if (newIncome != null) {
                            onEvent(CalendarEvent.SaveIncome(newIncome))
                            isSaved = true
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            onEvent(CalendarEvent.SaveIncome(incomeOnPaper.toDouble()))
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Save",
                                tint = if (isSaved) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
fun IncomeCalculatorPreview() {
    ShiftnocTheme {
        IncomeCalculator(
            appState = AppState(),
            onEvent = {}
        )
    }
}