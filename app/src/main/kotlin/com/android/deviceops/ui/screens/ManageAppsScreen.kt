package com.android.deviceops.ui.screens

import android.app.Activity
import android.app.RemoteInput
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.wear.compose.foundation.lazy.RotaryScrollableDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.wear.input.RemoteInputIntentHelper
import com.android.deviceops.data.AppFilter
import com.android.deviceops.data.AppInfo
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.ManageAppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val KEY_SEARCH = "search_query"

@Composable
fun ManageAppsScreen(
    onAppClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSearchDone: (String) -> Unit,
    vm: ManageAppsViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.loadApps(context) }

    val apps          by vm.filteredApps.collectAsStateWithLifecycle()
    val filter        by vm.filter.collectAsStateWithLifecycle()
    val disabledCount by vm.disabledCount.collectAsStateWithLifecycle()
    val columnState   = rememberTransformingLazyColumnState()

    val searchLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bundle = RemoteInput.getResultsFromIntent(result.data)
            val q = bundle?.getCharSequence(KEY_SEARCH)?.toString()?.trim() ?: ""
            if (q.isNotEmpty()) onSearchDone(q)
        }
    }

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize().background(Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
            flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(columnState),
            rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(columnState)
        ) {
            // 搜索按钮
            item {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CardBg)
                        .clickable {
                            val ri = RemoteInput.Builder(KEY_SEARCH).setLabel("搜索应用").build()
                            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                            RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(ri))
                            searchLauncher.launch(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Search, "搜索", tint = TextSecondary,
                        modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(6.dp))
            }

            // 筛选 / 统计 chip
            item {
                val mainLabel = when (filter) {
                    AppFilter.ALL    -> "已停用 $disabledCount 个应用"
                    AppFilter.USER   -> "已停用 $disabledCount 个用户应用"
                    AppFilter.SYSTEM -> "已停用 $disabledCount 个系统应用"
                }
                val subLabel = when (filter) {
                    AppFilter.ALL -> "全部应用"
                    AppFilter.USER -> "用户应用"
                    AppFilter.SYSTEM -> "系统应用"
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardBg)
                        .clickable(onClick = onFilterClick)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(mainLabel, color = TextPrimary, fontSize = 13.sp,
                        fontWeight = FontWeight.Medium)
                    Text(subLabel, color = Brand, fontSize = 11.sp)
                }
                Spacer(Modifier.height(6.dp))
            }

            // ③ 每个 app 独立 item — 真正的懒加载，不再全量渲染
            items(apps.size, key = { apps[it].packageName }) { idx ->
                val app = apps[idx]
                val isFirst = idx == 0
                val isLast  = idx == apps.lastIndex

                AppRow(
                    app       = app,
                    vm        = vm,
                    isFirst   = isFirst,
                    isLast    = isLast,
                    showDivider = !isLast,
                    onClick   = { onAppClick(app.packageName) }
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    vm: ManageAppsViewModel,
    isFirst: Boolean,
    isLast: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    // 图标懒加载：仅当此行可见时才触发 IO
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CardBg)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标占位 / 显示
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceLow),
                contentAlignment = Alignment.Center
            ) {
                icon?.let {
                    Image(it, app.label, modifier = Modifier.size(36.dp).clip(CircleShape))
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(verticalArrangement = Arrangement.Center) {
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
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(DividerCol)
            )
        }
    }
}
