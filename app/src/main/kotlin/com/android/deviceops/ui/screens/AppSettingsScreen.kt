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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.lazy.rememberTransformingLazyColumnState
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

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 28.dp)
        ) {
            item {
                Text(
                    text = app?.label ?: packageName,
                    color = PrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            if (isSystem) {
                item {
                    RadioRow(
                        label    = "测试停用",
                        selected = selected == AppMode.TEST_DISABLE,
                        onSelect = { selected = AppMode.TEST_DISABLE }
                    )
                }
            }
            item {
                RadioRow(
                    label    = "启用",
                    selected = selected == AppMode.ENABLE,
                    onSelect = { selected = AppMode.ENABLE }
                )
            }
            item {
                RadioRow(
                    label    = "停用",
                    selected = selected == AppMode.DISABLE,
                    onSelect = { selected = AppMode.DISABLE }
                )
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
                    modifier = Modifier
                        .width(140.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonActive,
                        contentColor   = PrimaryText
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    when {
                        countdown != null -> {
                            val m = countdown / 60
                            val s = countdown % 60
                            Text(text = "$m:${s.toString().padStart(2, '0')}", fontSize = 15.sp)
                        }
                        selected == AppMode.TEST_DISABLE -> Text(text = "6分钟", fontSize = 15.sp)
                        else -> Text(text = "确定", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioDot(selected = selected)
        Spacer(Modifier.width(12.dp))
        Text(text = label, color = PrimaryText, fontSize = 13.sp)
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    val fillColor  = if (selected) RadioSelected else Color.Transparent
    val ringColor  = if (selected) RadioSelected else RadioUnselected
    val checkColor = RadioCheck

    Canvas(modifier = Modifier.size(20.dp)) {
        val r   = size.minDimension / 2f
        val ctr = Offset(r, r)

        if (selected) drawCircle(color = fillColor, radius = r, center = ctr)

        drawCircle(
            color  = ringColor,
            radius = r - 1.dp.toPx(),
            center = ctr,
            style  = Stroke(width = 1.5.dp.toPx())
        )

        if (selected) {
            val sx = r * 0.30f; val sy = r * 0.95f
            val mx = r * 0.62f; val my = r * 1.30f
            val ex = r * 1.30f; val ey = r * 0.55f
            drawLine(color = checkColor, start = Offset(sx, sy), end = Offset(mx, my),
                strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color = checkColor, start = Offset(mx, my), end = Offset(ex, ey),
                strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}
