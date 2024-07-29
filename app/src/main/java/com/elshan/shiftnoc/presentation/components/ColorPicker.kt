package com.elshan.shiftnoc.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.screen.settings.components.toColor
import com.elshan.shiftnoc.util.DIALOGS
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun ColorPicker(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
    onColorSelected: (String) -> Unit,
) {
    val colors = listOf(
        "#FF0000", "#FF7F00", "#FFFF00", "#00FF00",
        "#0000FF", "#808080", "#FFFFFF",
        "#FFC0CB", "#8A2BE2", "" // Empty string for the color picker option
    )

    if (appState.visibleDialogs.contains(DIALOGS.ROUNDED_COLOR_PICKER)) {
        ColorPickerDialog(
            onColorSelected = { colorEnvelope ->
                val hexCodeWithHash = "#" + colorEnvelope.hexCode
                if (isValidColor(hexCodeWithHash)) {
                    onColorSelected(hexCodeWithHash)
                }
                onEvent(CalendarEvent.HideDialog(DIALOGS.ROUNDED_COLOR_PICKER))
            },
            onEvent = onEvent,
            initialColor = appState.selectedColor.toColor()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { color ->
                if (color.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                width = 1.5.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                onEvent(CalendarEvent.ShowDialog(DIALOGS.ROUNDED_COLOR_PICKER))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.color_picker),
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                width = 1.5.dp,
                                color = if (appState.selectedColor == color) Color(
                                    android.graphics.Color.parseColor(
                                        color
                                    )
                                ) else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(color)),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onColorSelected(color)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    onEvent: (CalendarEvent) -> Unit,
    onColorSelected: (ColorEnvelope) -> Unit,
    initialColor: Color = Color.Transparent
) {
    val controller = rememberColorPickerController()
    var selectedColorEnvelope by remember { mutableStateOf<ColorEnvelope?>(null) }


    AlertDialog(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        textContentColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.ROUNDED_COLOR_PICKER))
        },
        title = {
            Text(text = stringResource(R.string.select_color))
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { colorEnvelope ->
                        selectedColorEnvelope = colorEnvelope
                    }
                )

                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(25.dp),
                    controller = controller,
                    wheelRadius = 10.dp
                )

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                        .height(25.dp),
                    controller = controller,
                    wheelRadius = 10.dp
                )

                AlphaTile(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    controller = controller
                )
            }
        },
        confirmButton = {
            OutlinedButton(onClick = {
                selectedColorEnvelope?.let {
                    onColorSelected(it)
                    onEvent(CalendarEvent.HideDialog(DIALOGS.ROUNDED_COLOR_PICKER))
                }
            }) {
                Text(stringResource(R.string.select))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onEvent(CalendarEvent.HideDialog(DIALOGS.ROUNDED_COLOR_PICKER))
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


// Extension function to convert ColorEnvelope to Color
fun ColorEnvelope.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor("#${this.hexCode}"))
    } catch (e: IllegalArgumentException) {
        Color.Transparent
    }
}

// Helper function to validate color hex codes
fun isValidColor(colorStr: String): Boolean {
    return try {
        android.graphics.Color.parseColor(colorStr)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}


@Preview
@Composable
fun ColorPickerPreview() {
    ColorPicker(
        appState = AppState(),
        onEvent = {},
        onColorSelected = {},
    )
}