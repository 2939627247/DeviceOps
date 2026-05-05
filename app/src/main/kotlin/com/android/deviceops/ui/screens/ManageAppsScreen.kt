package com.android.deviceops.ui.screens

import android.app.Activity
import android.app.RemoteInput
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
import androidx.wear.input.RemoteInputIntentHelper
import com.android.deviceops.data.AppFilter
import com.android.deviceops.data.AppInfo
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.ManageAppsViewModel

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
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bundle = RemoteInput.getResultsFromIntent(result.data)
            val q = bundle?.getCharSequence(KEY_SEARCH)?.toString()?.trim() ?: ""
            if (q.isNotEmpty()) onSearchDone(q)
        }
    }

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // ── Search button ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SearchIconBg)
                        .clickable {
                            val remoteInput = RemoteInput
                                .Builder(KEY_SEARCH)
                                .setLabel("搜索应用")
                                .build()
                            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                            RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
                            searchLauncher.launch(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Search,
                        contentDescription = "搜索",
                        tint               = PrimaryText,
                        modifier           = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Stats / filter chip ────────────────────────────────────────
            item {
                val mainLabel = when (filter) {
                    AppFilter.ALL    -> "已停用 $disabledCount 个应用"
                    AppFilter.USER   -> "已停用 $disabledCount 个用户应用"
                    AppFilter.SYSTEM -> "已停用 $disabledCount 个系统应用"
                }
                val subLabel = when (filter) {
                    AppFilter.ALL    -> "全部应用"
                    AppFilter.USER   -> "用户应用"
                    AppFilter.SYSTEM -> "系统应用"
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ChipBackground)
                        .clickable(onClick = onFilterClick)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = mainLabel,
                        color = PrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subLabel,
                        color = SamsungBlue,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // ── App list ───────────────────────────────────────────────────
            item {
                if (apps.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ChipBackground)
                    ) {
                        apps.forEachIndexed { idx, app ->
                            AppRow(
                                app     = app,
                                onClick = { onAppClick(app.packageName) }
                            )
                            if (idx < apps.lastIndex) {
                                Divider(
                                    color     = DividerColor,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: AppInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bmp = remember(app.packageName) {
            app.icon.toBitmap(48, 48).asImageBitmap()
        }
        Image(
            bitmap             = bmp,
            contentDescription = app.label,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(10.dp))

        Column(verticalArrangement = Arrangement.Center) {
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
                app.isDisabled -> Text(
                    text     = "已停用",
                    color    = DisabledRed,
                    fontSize = 11.sp
                )
                else -> {}
            }
        }
    }
}
