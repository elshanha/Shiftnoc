package com.elshan.shiftnoc.presentation.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme

data class SettingsItem(
    val title: String,
    val onClick: () -> Unit = {},
    val icon: ImageVector
)

@Composable
fun SettingsComponent(categoryTitle: String, categoryList: List<SettingsItem>) {
    Column(
        modifier = Modifier.padding(16.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (categoryTitle.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                text = categoryTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Column(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            categoryList.forEachIndexed { index, item ->
                CategoryItemComponent(
                    title = item.title,
                    icon = item.icon,
                    onClick = item.onClick
                )
                if (index != categoryList.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItemComponent(title: String, icon: ImageVector, onClick: () -> Unit) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .background(MaterialTheme.colorScheme.onSecondaryContainer)
        .padding(16.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview
@Composable
fun SettingsCategoryComponentPreview() {
    val generalCategoryList = listOf(
        SettingsItem("Language", icon = Icons.Default.ArrowForwardIos),
        SettingsItem("Week", icon = Icons.Default.ArrowForwardIos)
    )
    ShiftnocTheme {
        SettingsComponent(
            categoryTitle = "General",
            categoryList = generalCategoryList
        )
    }
}