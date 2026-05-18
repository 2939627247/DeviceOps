package com.android.deviceops.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.MainViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** 轨道颜色插值：Off rgb(99,99,104) → On rgb(66,105,255) */
private fun lerpTrack(r: Float): Color {
    val red   = (99 + (66  - 99)  * r).roundToInt().coerceIn(0, 255)
    val green = (99 + (105 - 99)  * r).roundToInt().coerceIn(0, 255)
    val blue  = (104 + (255 - 104) * r).roundToInt().coerceIn(0, 255)
    return Color(red / 255f, green / 255f, blue / 255f)
}

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
                    label            = "HTTP 代理",
                    checked          = proxyEnabled,
                    onContainerClick = onHttpProxyClick,
                    onCheckedChange  = { vm.toggleProxy(context) }
                )

                SplitCard(
                    label            = "管理停用应用",
                    checked          = manageEnabled,
                    onContainerClick = onManageAppsClick,
                    onCheckedChange  = { vm.toggleManageApps(context) }
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
        if (isPressed) CardBgPressed else CardBg,
        if (isPressed) tween(80) else tween(250), label = "cardBg"
    )
    val labelScale by animateFloatAsState(
        if (isPressed) 0.975f else 1f,
        if (isPressed) tween(80) else spring(0.5f, 500f), label = "labelScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(38.dp))
            .background(cardBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Box(Modifier.width(1.dp).height(42.dp).background(DividerCol))
        Spacer(Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .width(94.dp)
                .fillMaxHeight()
                .padding(end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            ToggleSwitch(checked = checked, onToggle = onCheckedChange)
        }
    }
}

/**
 * 自定义开关，无抽搐实现：
 * - Animatable 作唯一状态源，完全控制动画时机
 * - 拖拽中 snapTo 实时跟随，松手 animateTo 从当前位置平滑到终点
 * - prevChecked 标记阻止 LaunchedEffect 与手势动画竞争
 */
@Composable
private fun ToggleSwitch(checked: Boolean, onToggle: () -> Unit) {
    val density = LocalDensity.current
    val maxPx   = with(density) { 24.dp.toPx() }
    val scope   = rememberCoroutineScope()

    // Animatable：ratio 0f=关, 1f=开
    val anim        = remember { Animatable(if (checked) 1f else 0f) }
    // 记录上次已知 checked，阻止 LaunchedEffect 覆盖手势动画
    val prevChecked = remember { mutableStateOf(checked) }

    // 仅处理外部（非手势）引发的 checked 变化
    LaunchedEffect(checked) {
        if (checked != prevChecked.value) {
            prevChecked.value = checked
            anim.animateTo(if (checked) 1f else 0f, spring(0.75f, 600f))
        }
    }

    var isPressed by remember { mutableStateOf(false) }
    val thumbSize by animateDpAsState(
        if (isPressed) 23.dp else 27.dp, tween(200), label = "sz"
    )

    val ratio     = anim.value
    val thumbOff  = with(density) { (ratio * maxPx).toDp() }
    val thumbLeft = 3.5.dp + thumbOff + (27.dp - thumbSize) / 2
    val thumbTop  = (34.dp - thumbSize) / 2

    Box(
        modifier = Modifier
            .width(58.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(lerpTrack(ratio))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down    = awaitFirstDown(requireUnconsumed = false)
                    isPressed   = true
                    val startX  = down.position.x
                    val startR  = anim.value
                    scope.launch { anim.stop() }   // 停止正在运行的动画

                    while (true) {
                        val event  = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        if (!change.pressed) {
                            // 松手：判断是点击还是拖拽
                            val delta      = change.position.x - startX
                            val isTap      = abs(delta) < with(density) { 4.dp.toPx() }
                            val newChecked = if (isTap) !checked else anim.value > 0.5f
                            val target     = if (newChecked) 1f else 0f
                            isPressed = false

                            // 从当前位置动画到终点（无跳变）
                            scope.launch { anim.animateTo(target, spring(0.75f, 600f)) }

                            if (newChecked != checked) {
                                // 先更新 prevChecked，阻止 LaunchedEffect 重复动画
                                prevChecked.value = newChecked
                                onToggle()
                            }
                            break
                        }

                        // 拖拽：snapTo 实时跟随，不产生后台动画
                        val delta = change.position.x - startX
                        if (abs(delta) > with(density) { 4.dp.toPx() }) change.consume()
                        scope.launch {
                            anim.snapTo((startR + delta / maxPx).coerceIn(0f, 1f))
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbLeft, y = thumbTop)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Thumb)
        )
    }
}
