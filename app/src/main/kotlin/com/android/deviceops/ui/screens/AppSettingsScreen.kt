package com.android.deviceops.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.android.deviceops.viewmodel.ManageAppsViewModel

private enum class AppMode { TEST_DISABLE, ENABLE, DISABLE }

@Composable
fun AppSettingsScreen(
    packageName: String,
    onBack: () -> Unit,
    vm: ManageAppsViewModel = viewModel()
) {
    val context  = LocalContext.current
    val app      = vm.getAppInfo(packageName)
    val isSystem = app?.isSystemApp ?: false

    var selected by remember {
        mutableStateOf(
            when {
                app?.countdownSeconds != null -> AppMode.TEST_DISABLE
                app?.isDisabled == true       -> AppMode.DISABLE
                else                          -> AppMode.ENABLE
            }
        )
    }

    val countdowns by vm.filteredApps.collectAsStateWithLifecycle()
    val liveApp    = countdowns.find { it.packageName == packageName }
    val countdown  = liveApp?.countdownSeconds

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 28.dp)
        ) {
            item {
                Text(
                    text = app?.label ?: packageName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            item {
                val options = buildList {
                    if (isSystem) add(AppMode.TEST_DISABLE to "测试停用")
                    add(AppMode.ENABLE to "启用")
                    add(AppMode.DISABLE to "停用")
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    options.forEachIndexed { idx, (mode, label) ->
                        RadioOptionRow(
                            label      = label,
                            selected   = selected == mode,
                            onSelect   = { selected = mode },
                            showDivider = idx < options.lastIndex,
                            primaryColor = MaterialTheme.colorScheme.primary,
                            onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                            outlineColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        when (selected) {
                            AppMode.ENABLE       -> vm.enableApp(context, packageName)
                            AppMode.DISABLE      -> vm.disableApp(context, packageName)
                            AppMode.TEST_DISABLE -> vm.testDisableApp(context, packageName)
                        }
                        onBack()
                    },
                    modifier = Modifier.width(140.dp).height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    when {
                        countdown != null -> {
                            val m = countdown / 60
                            val s = countdown % 60
                            Text(
                                "$m:${s.toString().padStart(2, '0')}",
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        selected == AppMode.TEST_DISABLE -> Text(
                            "6分钟",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        else -> Text(
                            "确定",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    showDivider: Boolean,
    primaryColor: androidx.compose.ui.graphics.Color,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color,
    selectedContainerColor: androidx.compose.ui.graphics.Color,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) selectedContainerColor else androidx.compose.ui.graphics.Color.Transparent)
                .clickable(onClick = onSelect)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = label,
                color    = onSurfaceColor,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            RadioCircle(selected = selected, primaryColor = primaryColor, outlineColor = outlineColor)
        }
        if (showDivider) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(outlineColor)
            )
        }
    }
}

@Composable
private fun RadioCircle(
    selected: Boolean,
    primaryColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color,
) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val r   = size.minDimension / 2f
        val ctr = Offset(r, r)
        if (selected) {
            drawCircle(color = primaryColor, radius = r, center = ctr)
            drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = r * 0.38f, center = ctr)
        } else {
            drawCircle(
                color  = outlineColor,
                radius = r - 1.dp.toPx(),
                center = ctr,
                style  = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
