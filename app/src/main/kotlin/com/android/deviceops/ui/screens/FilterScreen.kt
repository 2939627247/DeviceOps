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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.android.deviceops.data.AppFilter
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.ManageAppsViewModel

@Composable
fun FilterScreen(onBack: () -> Unit, vm: ManageAppsViewModel = viewModel()) {
    val current     by vm.filter.collectAsStateWithLifecycle()
    val columnState = rememberTransformingLazyColumnState()
    val options     = listOf(AppFilter.ALL to "全部应用",
                             AppFilter.USER to "用户应用",
                             AppFilter.SYSTEM to "系统应用")

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize().background(Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text("筛选方式", color = TextPrimary, fontSize = 15.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(bottom = 12.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(CardBg, RoundedCornerShape(14.dp))
                ) {
                    options.forEachIndexed { idx, (filter, label) ->
                        val isSelected = current == filter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { vm.setFilter(filter); onBack() }
                                .background(
                                    if (isSelected) Brand.copy(alpha = 0.15f)
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = TextPrimary, fontSize = 14.sp,
                                modifier = Modifier.weight(1f))
                            Canvas(Modifier.size(18.dp)) {
                                val r = size.minDimension / 2f
                                val c = Offset(r, r)
                                if (isSelected) {
                                    drawCircle(color = Brand, radius = r, center = c)
                                    drawCircle(color = White, radius = r * 0.38f, center = c)
                                } else {
                                    drawCircle(color = DividerCol, radius = r - 1.dp.toPx(),
                                        center = c, style = Stroke(1.5.dp.toPx()))
                                }
                            }
                        }
                        if (idx < options.lastIndex)
                            Spacer(Modifier.fillMaxWidth().height(0.5.dp).background(DividerCol))
                    }
                }
            }
        }
    }
}
