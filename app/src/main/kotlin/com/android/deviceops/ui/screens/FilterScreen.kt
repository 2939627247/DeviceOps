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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.android.deviceops.data.AppFilter
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

    val primary          = MaterialTheme.colorScheme.primary
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val outlineVariant   = MaterialTheme.colorScheme.outlineVariant
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

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
                    "筛选方式",
                    color    = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .background(surfaceContainer, RoundedCornerShape(12.dp))
                ) {
                    options.forEachIndexed { idx, (filter, label) ->
                        val isSelected = current == filter
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) primaryContainer
                                        else androidx.compose.ui.graphics.Color.Transparent
                                    )
                                    .clickable {
                                        vm.setFilter(filter)
                                        onBack()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text     = label,
                                    color    = onSurface,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Canvas(Modifier.size(18.dp)) {
                                    val r   = size.minDimension / 2f
                                    val ctr = Offset(r, r)
                                    if (isSelected) {
                                        drawCircle(color = primary, radius = r, center = ctr)
                                        drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = r * 0.38f, center = ctr)
                                    } else {
                                        drawCircle(color = outlineVariant, radius = r - 1.dp.toPx(), center = ctr, style = Stroke(1.5.dp.toPx()))
                                    }
                                }
                            }
                            if (idx < options.lastIndex) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(outlineVariant)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
