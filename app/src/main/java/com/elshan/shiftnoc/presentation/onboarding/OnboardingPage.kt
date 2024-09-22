package com.elshan.shiftnoc.presentation.onboarding

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.util.Dimens.MediumPadding1
import com.elshan.shiftnoc.util.Dimens.MediumPadding2


@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    page: Page,
) {
    Column(
        modifier = modifier,
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(page.lottie))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 42.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxHeight(0.65f)
        )
        Spacer(modifier = Modifier.height(MediumPadding1))
        Text(
            modifier = Modifier.padding(horizontal = MediumPadding2),
            text = stringResource(
                id = page.title
            ),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
        )
        Text(
            modifier = Modifier.padding(horizontal = MediumPadding2, vertical = 12.dp),
            text = stringResource(
                id = page.description
            ),
            style = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.onSecondaryContainer
            ),
        )
    }
}

//@Preview(showBackground = true)
//@Preview(uiMode = UI_MODE_NIGHT_YES)
//@Composable
//fun OnBoardingPagePreview() {
//        OnBoardingPage(
//            page = Page(
//                title = "Lorem Ipsum is simply dummy",
//                description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
//                lottie = R.raw.calendar_reminder
//            )
//        )
//}