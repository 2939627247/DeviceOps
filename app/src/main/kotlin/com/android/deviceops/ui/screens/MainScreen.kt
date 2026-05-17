package com.android.deviceops.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.wear.compose.material3.*
import com.android.deviceops.viewmodel.MainViewModel
import kotlin.math.roundToInt

private val TrackOn    = Color(0xFFA8C7FA)
private val ThumbOn    = Color(0xFF0D47A1)
private val IconOn     = Color(0xFFD3E3FD)
private val TrackOff   = Color(0xFF333537)
private val ThumbOff   = Color(0xFF8E918F)
private val IconOff    = Color(0xFF5F6368)
private val CardBg     = Color(0xFF202124)
private val DividerCol = Color(0xFF4D4D52)

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
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DeviceOps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                SplitCard(
                    label = "HTTP 代理",
                    secondary = if (proxyEnabled) "已启用" else "未启用",
                    checked = proxyEnabled,
                    onContainerClick = onHttpProxyClick,
                    onToggle = { vm.toggleProxy(context) }
                )

                SplitCard(
                    label = "管理停用应用",
                    secondary = if (manageEnabled) "已启用" else "未启用",
                    checked = manageEnabled,
                    onContainerClick = onManageAppsClick,
                    onToggle = { vm.toggleManageApps(context) }
                )
            }
        }
    }
}

/** 统一圆角卡片，左侧点击跳转，右侧仅控制开关，中间一条灰细线 */
@Composable
private fun SplitCard(
    label: String,
    secondary: String,
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
        // 父按钮区域：点击 → 跳转二级页面
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onContainerClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(secondary, color = Color(0xFFB0B0B8), fontSize = 11.sp)
        }

        // 视觉分隔线（仅视觉，不拦截事件）
        Spacer(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(DividerCol)
        )

        // 开关区域：点击 → 仅切换状态，不跳转
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            CustomSwitch(checked = checked, onToggle = onToggle)
        }
    }
}

@Composable
private fun CustomSwitch(checked: Boolean, onToggle: () -> Unit) {
    val trackColor by animateColorAsState(if (checked) TrackOn else TrackOff, tween(200), label = "track")
    val thumbColor by animateColorAsState(if (checked) ThumbOn else ThumbOff, tween(200), label = "thumb")
    val iconColor  by animateColorAsState(if (checked) IconOn  else IconOff,  tween(200), label = "icon")
    val thumbFrac  by animateFloatAsState(if (checked) 1f else 0f, tween(200), label = "frac")

    val trackW = 52.dp
    val trackH = 30.dp
    val thumbD = 26.dp
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
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        val travel = (trackW - thumbD - pad * 2).toPx()
                        val x = (pad.toPx() + travel * thumbFrac).roundToInt()
                        val y = ((trackH - thumbD) / 2).toPx().roundToInt()
                        placeable.placeRelative(x, y)
                    }
                }
                .size(thumbD)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
