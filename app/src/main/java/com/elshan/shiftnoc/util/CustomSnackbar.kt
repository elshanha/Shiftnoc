package com.elshan.shiftnoc.util


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    onActionClick: () -> Unit = {},
) {
    val brush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.onPrimary,
            MaterialTheme.colorScheme.primary,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = MaterialTheme.shapes.small
            )
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(start = 8.dp),
                text = snackbarData.visuals.message,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {
                        snackbarData.performAction()
                        onActionClick()
                    }) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CustomSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit = {}
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            CustomSnackbar(
                snackbarData = snackbarData,
                onActionClick = onActionClick
            )
        }
    )
}

class SnackbarManager(private val coroutineScope: CoroutineScope) {
    var snackbarHostState = SnackbarHostState()

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null
    ) {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration
            )
            when (result) {
                SnackbarResult.ActionPerformed -> onAction?.invoke()
                SnackbarResult.Dismissed -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
}

@Composable
fun rememberSnackbarManager(): SnackbarManager {
    val coroutineScope = rememberCoroutineScope()
    return remember { SnackbarManager(coroutineScope) }
}
