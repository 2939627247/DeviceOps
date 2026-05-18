package com.android.deviceops.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.MainViewModel

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
            modifier = Modifier.fillMaxSize().background(Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    "Device Ops",
                    fontSize = 27.sp, fontWeight = FontWeight.W500,
                    color = White, letterSpacing = (-0.2).sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                SplitCard(
                    label = "HTTP 代理",
                    checked = proxyEnabled,
                    onContainerClick = onHttpProxyClick,
                    onCheckedChange = { vm.toggleProxy(context) }
                )

                SplitCard(
                    label = "管理停用应用",
                    checked = manageEnabled,
                    onContainerClick = onManageAppsClick,
                    onCheckedChange = { vm.toggleManageApps(context) }
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
    onCheckedChange: () -> Unit,
) {
    val labelSource = remember { MutableInteractionSource() }
    val isPressed   by labelSource.collectIsPressedAsState()

    val cardBg by animateColorAsState(
        targetValue   = if (isPressed) CardBgPressed else CardBg,
        animationSpec = if (isPressed) tween(80) else tween(250),
        label         = "cardBg"
    )
    val labelScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.975f else 1f,
        animationSpec = if (isPressed) tween(80) else spring(0.5f, 500f),
        label         = "labelScale"
    )

    // 内置 Switch 颜色
    val switchColors = SwitchDefaults.colors().copy(
        checkedThumbColor    = Thumb,
        checkedTrackColor    = TrackOn,
        checkedBorderColor   = Color.Transparent,
        checkedIconColor     = Color.Transparent,
        uncheckedThumbColor  = Thumb,
        uncheckedTrackColor  = TrackOff,
        uncheckedBorderColor = Color.Transparent,
        uncheckedIconColor   = Color.Transparent,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(38.dp))
            .background(cardBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 父区域：点击 → 跳转
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    interactionSource = labelSource,
                    indication        = null,
                    onClick           = onContainerClick
                )
                .padding(start = 28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                label,
                fontSize = 21.sp, fontWeight = FontWeight.W400,
                color = White, letterSpacing = (-0.1).sp,
                modifier = Modifier.scale(labelScale)
            )
        }

        // 分隔线 1dp × 42dp
        Box(Modifier.width(1.dp).height(42.dp).background(DividerCol))
        Spacer(Modifier.width(4.dp))

        // 开关区域：内置 Switch 处理点击+拖拽+动画
        Box(
            modifier = Modifier
                .width(94.dp)
                .fillMaxHeight()
                .padding(end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Switch(
                checked         = checked,
                onCheckedChange = { onCheckedChange() },
                colors          = switchColors
            )
        }
    }
}
