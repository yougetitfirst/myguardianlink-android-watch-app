package com.myguardianlink.myguardianwatch.presentation.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Extra insets so content stays inside the visible area on round watch faces.
 */
@Composable
fun rememberRoundScreenContentPadding(): PaddingValues {
    val config = LocalConfiguration.current
    return remember(config.screenWidthDp, config.screenHeightDp) {
        val horizontal = (config.screenWidthDp * 0.12f).dp
            .coerceAtLeast(18.dp)
            .coerceAtMost(34.dp)
        val top = (config.screenHeightDp * 0.05f).dp
            .coerceAtLeast(12.dp)
            .coerceAtMost(24.dp)
        val bottom = (config.screenHeightDp * 0.11f).dp
            .coerceAtLeast(22.dp)
            .coerceAtMost(38.dp)
        PaddingValues(
            start = horizontal,
            top = top,
            end = horizontal,
            bottom = bottom,
        )
    }
}
