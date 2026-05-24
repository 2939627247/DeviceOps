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
import androidx.wear.compose.foundation.lazy.RotaryScrollableDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.android.deviceops.ui.theme.*
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

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize().background(Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
            flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(columnState),
            rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(columnState)
        ) {
            item {
                Text(
                    app?.label ?: packageName,
                    color = TextPrimary, fontSize = 15.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(bottom = 12.dp)
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
                        .padding(horizontal = 12.dp)
                        .background(CardBg, RoundedCornerShape(14.dp))
                ) {
                    options.forEachIndexed { idx, (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = mode }
                                .background(
                                    if (selected == mode) Brand.copy(alpha = 0.15f)
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = TextPrimary, fontSize = 14.sp,
                                modifier = Modifier.weight(1f))
                            RadioDot(selected = selected == mode)
                        }
                        if (idx < options.lastIndex) {
                            Spacer(Modifier.fillMaxWidth().height(0.5.dp).background(DividerCol))
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
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
                    val label = when {
                        countdown != null -> {
                            val m = countdown / 60; val s = countdown % 60
                            "$m:${s.toString().padStart(2, '0')}"
                        }
                        selected == AppMode.TEST_DISABLE -> "6分钟"
                        else -> "确定"
                    }
                    Text(label, fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    Canvas(Modifier.size(18.dp)) {
        val r   = size.minDimension / 2f
        val ctr = Offset(r, r)
        if (selected) {
            drawCircle(color = Brand, radius = r, center = ctr)
            drawCircle(color = White, radius = r * 0.38f, center = ctr)
        } else {
            drawCircle(color = DividerCol, radius = r - 1.dp.toPx(),
                center = ctr, style = Stroke(1.5.dp.toPx()))
        }
    }
}
