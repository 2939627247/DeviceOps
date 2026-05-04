package com.android.deviceops.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.lazy.rememberTransformingLazyColumnState
import com.android.deviceops.data.AppInfo
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.ManageAppsViewModel

@Composable
fun SearchResultsScreen(
    query: String,
    onAppClick: (String) -> Unit,
    vm: ManageAppsViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.loadApps(context) }

    val allApps by vm.filteredApps.collectAsStateWithLifecycle()
    val results = remember(query, allApps) {
        allApps.filter { it.label.contains(query, ignoreCase = true) }
    }

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Search,
                        contentDescription = null,
                        tint               = SecondaryText,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = "\"$query\"", color = SecondaryText, fontSize = 12.sp)
                }
            }

            if (results.isEmpty()) {
                item {
                    Text("未找到应用", color = SecondaryText, fontSize = 13.sp)
                }
            } else {
                // ── Fix 2: A scheme — all results inside ONE rounded container
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChipBackground)
                    ) {
                        results.forEachIndexed { idx, app ->
                            SearchAppRow(
                                app     = app,
                                onClick = { onAppClick(app.packageName) }
                            )
                            if (idx < results.lastIndex) {
                                Divider(color = DividerColor, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAppRow(app: AppInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bmp = remember(app.packageName) { app.icon.toBitmap(48, 48).asImageBitmap() }
        Image(
            bitmap             = bmp,
            contentDescription = app.label,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text       = app.label,
                color      = PrimaryText,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines   = 1
            )
            when {
                app.countdownSeconds != null -> {
                    val m = app.countdownSeconds / 60
                    val s = app.countdownSeconds % 60
                    Text(
                        text     = "停用-$m:${s.toString().padStart(2, '0')}",
                        color    = DisabledRed,
                        fontSize = 11.sp
                    )
                }
                app.isDisabled -> Text("己停用", color = DisabledRed, fontSize = 11.sp)
                else -> {}
            }
        }
    }
}
