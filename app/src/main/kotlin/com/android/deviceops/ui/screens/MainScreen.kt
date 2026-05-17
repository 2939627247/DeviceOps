package com.android.deviceops.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.android.deviceops.viewmodel.MainViewModel
import kotlin.math.roundToInt

private val CardBg     = Color(0xFF202124)
private val DividerCol = Color(0xFF4D4D52)
// 开关颜色（与概念图一致）
private val TrackOn    = Color(0xFF4269FF)   // 蓝色轨道
private val TrackOff   = Color(0xFF333537)   // 灰色轨道
private val ThumbColor = Color(0xFFFFFFFF)   // 白色圆块（两态统一）

@Composable
fun MainScreen(
    onHttpProxyClick: () -> Unit,
    onManageAppsClick: () -> Unit,
    vm: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.init(context) }

    val proxyEnabled  by vm.proxyEnabled.collectAsStateWithLifecycle()
    val manageEnabled by vm.manageAppsEnabled.collectAsStateWithLifecycle()

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text       = "Device Ops",
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    modifier   = Modifier.padding(bottom = 8.dp)
                )

                SplitCard(
                    label            = "HTTP 代理",
                    checked          = proxyEnabled,
                    onContainerClick = onHttpProxyClick,
                    onToggle         = { vm.toggleProxy(context) }
                )

                SplitCard(
                    label            = "管理停用应用",
                    checked          = manageEnabled,
                    onContainerClick = onManageAppsClick,
                    onToggle         = { vm.toggleManageApps(context) }
                )
            }
        }
    }
}

@Composable
private fun SplitCard(
    label: String,
    checked: Boolean,
    onContainerClick: () -> Unit,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(CardBg)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 父按钮区域 → 点击跳转
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onContainerClick)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text       = label,
                color      = Color.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 细分隔线（视觉，不拦截事件）
        Spacer(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(DividerCol)
        )

        // 开关区域 → 仅切换状态
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            SlideSwitch(checked = checked, onToggle = onToggle)
        }
    }
}

@Composable
private fun SlideSwitch(checked: Boolean, onToggle: () -> Unit) {
    val trackColor = if (checked) TrackOn else TrackOff
    val thumbFrac  by animateFloatAsState(
        targetValue   = if (checked) 1f else 0f,
        animationSpec = tween(220),
        label         = "thumb"
    )

    val trackW = 56.dp
    val trackH = 32.dp
    val thumbD = 28.dp
    val pad    = 2.dp

    Box(
        modifier = Modifier
            .width(trackW)
            .height(trackH)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable(onClick = onToggle)
    ) {
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val p = measurable.measure(constraints)
                    layout(p.width, p.height) {
                        val travel = (trackW - thumbD - pad * 2).toPx()
                        val x = (pad.toPx() + travel * thumbFrac).roundToInt()
                        val y = ((trackH - thumbD) / 2).toPx().roundToInt()
                        p.placeRelative(x, y)
                    }
                }
                .size(thumbD)
                .clip(CircleShape)
                .background(ThumbColor)
        )
    }
}
