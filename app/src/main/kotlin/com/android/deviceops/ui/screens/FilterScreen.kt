package com.android.deviceops.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.lazy.rememberTransformingLazyColumnState
import com.android.deviceops.data.AppFilter
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.ManageAppsViewModel

@Composable
fun FilterScreen(
    onBack: () -> Unit,
    vm: ManageAppsViewModel = viewModel()
) {
    val current     by vm.filter.collectAsStateWithLifecycle()
    val columnState = rememberTransformingLazyColumnState()

    val options = listOf(
        AppFilter.ALL    to "全部应用",
        AppFilter.USER   to "用户应用",
        AppFilter.SYSTEM to "系统应用",
    )

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 32.dp)
        ) {
            item {
                Text(
                    text = "筛选方式",
                    color = PrimaryText,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            items(options.size) { idx ->
                val (filter, label) = options[idx]
                val isSelected = current == filter

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isSelected) {
                                vm.setFilter(filter)
                                onBack()
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text     = label,
                        color    = PrimaryText,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    FilterRadioDot(selected = isSelected)
                }

                if (idx < options.size - 1) {
                    Divider(
                        modifier  = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        color     = DividerColor,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterRadioDot(selected: Boolean) {
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
