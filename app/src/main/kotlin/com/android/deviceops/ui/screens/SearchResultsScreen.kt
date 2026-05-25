package com.android.deviceops.ui.screens

import android.graphics.drawable.Drawable
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.android.deviceops.data.AppInfo
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.ManageAppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    ScreenScaffold(scrollState = columnState, scrollIndicator = {}) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize().background(Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
            flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(columnState),
            rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(columnState)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Filled.Search, null, tint = TextTertiary,
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("\"$query\"", color = TextTertiary, fontSize = 12.sp)
                }
            }

            if (results.isEmpty()) item {
                Text("未找到应用", color = TextSecondary, fontSize = 13.sp)
            }

            items(results.size, key = { results[it].packageName }) { idx ->
                val app     = results[idx]
                val isFirst = idx == 0
                val isLast  = idx == results.lastIndex

                SearchAppRow(
                    app         = app,
                    vm          = vm,
                    isFirst     = isFirst,
                    isLast      = isLast,
                    showDivider = !isLast,
                    onClick     = { onAppClick(app.packageName) }
                )
            }
        }
    }
}

@Composable
private fun SearchAppRow(
    app: AppInfo,
    vm: ManageAppsViewModel,
    isFirst: Boolean,
    isLast: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var icon by remember(app.packageName) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(app.packageName) {
        val drawable: Drawable? = vm.getIcon(app.packageName, context.packageManager)
        icon = withContext(Dispatchers.Default) {
            drawable?.toBitmap(48, 48)?.asImageBitmap()
        }
    }

    val shape = RoundedCornerShape(
        topStart    = if (isFirst) 14.dp else 0.dp,
        topEnd      = if (isFirst) 14.dp else 0.dp,
        bottomStart = if (isLast)  14.dp else 0.dp,
        bottomEnd   = if (isLast)  14.dp else 0.dp,
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CardBg)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceLow),
                contentAlignment = Alignment.Center
            ) {
                icon?.let {
                    Image(it, app.label, modifier = Modifier.size(36.dp).clip(CircleShape))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(app.label, color = TextPrimary, fontSize = 13.sp,
                    fontWeight = FontWeight.W500, maxLines = 1)
                when {
                    app.countdownSeconds != null -> {
                        val m = app.countdownSeconds / 60
                        val s = app.countdownSeconds % 60
                        Text("停用中 $m:${s.toString().padStart(2, '0')}",
                            color = ErrorRed, fontSize = 11.sp)
                    }
                    app.isDisabled -> Text("已停用", color = ErrorRed, fontSize = 11.sp)
                    else -> {}
                }
            }
        }
        if (showDivider) {
            Spacer(Modifier.fillMaxWidth().height(0.5.dp).background(DividerCol))
        }
    }
}
