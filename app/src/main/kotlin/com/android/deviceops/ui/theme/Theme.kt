package com.android.deviceops.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun DeviceOpsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme(
            primary               = Color(0xFF4F80FF),
            onPrimary             = Color(0xFFFFFFFF),
            primaryContainer      = Color(0xFF1A3A7A),
            onPrimaryContainer    = Color(0xFFD8E2FF),
            secondary             = Color(0xFF636368),
            onSecondary           = Color(0xFFFFFFFF),
            secondaryContainer    = Color(0xFF2A2A2E),
            onSecondaryContainer  = Color(0xFFE0E0E6),
            tertiary              = Color(0xFF636368),
            onTertiary            = Color(0xFFFFFFFF),
            tertiaryContainer     = Color(0xFF2A2A2E),
            onTertiaryContainer   = Color(0xFFE0E0E6),
            surface               = Color(0xFF1C1C1F),
            onSurface             = Color(0xFFE6E1E5),
            onSurfaceVariant      = Color(0xFFB0B0B8),
            surfaceContainer      = Color(0xFF252528),
            surfaceContainerLow   = Color(0xFF1A1A1C),
            surfaceContainerHigh  = Color(0xFF303035),
            background            = Color(0xFF000000),
            onBackground          = Color(0xFFFFFFFF),
            error                 = Color(0xFFCF6679),
            onError               = Color(0xFF000000),
            errorContainer        = Color(0xFF8C1D18),
            onErrorContainer      = Color(0xFFFFDAD6),
            outline               = Color(0xFF4D4D52),
            outlineVariant        = Color(0xFF38383C),
        ),
        content = content
    )
}
