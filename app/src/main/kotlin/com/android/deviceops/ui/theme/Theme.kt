package com.android.deviceops.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun DeviceOpsTheme(content: @Composable () -> Unit) {
    // Explicit color scheme — Material You dynamic color is NOT used.
    val colorScheme = ColorScheme(
        background            = Background,
        onBackground          = PrimaryText,
        onSurface             = PrimaryText,
        onSurfaceVariant      = SecondaryText,
        primary               = ButtonActive,
        onPrimary             = PrimaryText,
        primaryContainer      = ButtonActive,
        onPrimaryContainer    = PrimaryText,
        secondary             = ChipBackground,
        onSecondary           = PrimaryText,
        secondaryContainer    = ChipBackground,
        onSecondaryContainer  = PrimaryText,
        tertiary              = ChipBackground,
        onTertiary            = PrimaryText,
        error                 = DisabledRed,
        onError               = PrimaryText,
        surfaceContainer      = ChipBackground,
        surfaceContainerLow   = ChipBackground,
        surfaceContainerHigh  = ChipBackground,
        outline               = DividerColor,
        outlineVariant        = DividerColor,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
