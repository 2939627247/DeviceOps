package com.android.deviceops.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun DeviceOpsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme(
            primary               = Brand,
            primaryDim            = androidx.compose.ui.graphics.Color(0xFF2A4FCC),
            onPrimary             = White,
            primaryContainer      = androidx.compose.ui.graphics.Color(0xFF1A3A7A),
            onPrimaryContainer    = White,
            secondary             = TrackOff,
            secondaryDim          = androidx.compose.ui.graphics.Color(0xFF4A4A50),
            onSecondary           = White,
            secondaryContainer    = CardBg,
            onSecondaryContainer  = TextPrimary,
            tertiary              = TrackOff,
            tertiaryDim           = androidx.compose.ui.graphics.Color(0xFF4A4A50),
            onTertiary            = White,
            tertiaryContainer     = CardBg,
            onTertiaryContainer   = TextPrimary,
            onSurface             = TextPrimary,
            onSurfaceVariant      = TextSecondary,
            surfaceContainer      = CardBg,
            surfaceContainerLow   = SurfaceLow,
            surfaceContainerHigh  = CardBgPressed,
            background            = Black,
            onBackground          = TextPrimary,
            error                 = ErrorRed,
            onError               = White,
            outline               = DividerCol,
            outlineVariant        = androidx.compose.ui.graphics.Color(0xFF38383C),
        ),
        content = content
    )
}
