package com.elshan.shiftnoc.presentation.onboarding

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.util.Dimens.MediumPadding2
import com.elshan.shiftnoc.util.enums.Languages
import com.elshan.shiftnoc.util.updateLocale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingScreen(
    onFinish: () -> Unit,
    defaultLanguage: String,
    onSelect: (String) -> Unit
) {

    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(defaultLanguage) }

    val pagerState = rememberPagerState(initialPage = 0) {
        page.size
    }
    val scope = rememberCoroutineScope()


    if (showLanguageDialog) {
        BasicAlertDialog(onDismissRequest = {
            showLanguageDialog = false
        }) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.medium
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp)
                )
                Text(text = "Choose a language")
                Spacer(modifier = Modifier.height(16.dp))
                Languages.entries.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(text = language.emoji + " " + language.name) },
                        onClick = {
                            selectedLanguage = language.language
                            onSelect(selectedLanguage)
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    page = if (pagerState.currentPage == 2) pagerState.currentPage - 1 else pagerState.currentPage + 1
                                )
                            }
                            showLanguageDialog = false
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MediumPadding2, vertical = MediumPadding2)
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable {
                        showLanguageDialog = !showLanguageDialog
                    },
                imageVector = Icons.Filled.Language,
                contentDescription = "Language"
            )
        }


        val buttonsState = remember {
            derivedStateOf {
                when (pagerState.currentPage) {
                    0 -> listOf("", context.getString(R.string.next))
                    1 -> listOf(context.getString(R.string.back), context.getString(R.string.next))
                    2 -> listOf(
                        context.getString(R.string.back),
                        context.getString(R.string.get_started)
                    )

                    else -> listOf("", "")
                }
            }
        }
        HorizontalPager(state = pagerState) { index ->
            OnBoardingPage(page = page[index])
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MediumPadding2)

                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerIndicator(
                modifier = Modifier.width(52.dp),
                pagesSize = page.size,
                selectedPage = pagerState.currentPage
            )

            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (buttonsState.value[0].isNotEmpty()) {
                    CalendarTextButton(
                        text = buttonsState.value[0],
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    page = pagerState.currentPage - 1
                                )
                            }

                        }
                    )
                }
                CalendarButton(
                    text = buttonsState.value[1],
                    onClick = {
                        scope.launch {

                            if (pagerState.currentPage == 2) {
                                onFinish()
                            } else {
                                pagerState.animateScrollToPage(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    page = pagerState.currentPage + 1
                                )
                            }
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.5f))
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun OnboardingPreview() {
//    OnBoardingScreen(
//        onFinish = {},
//        onEvent = {},
//        appState = AppState()
//    )
//
//}