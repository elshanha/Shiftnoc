package com.elshan.shiftnoc.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object FullScreenCalendar : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data class Details(
        val title: String,
    ) : Screen()

}