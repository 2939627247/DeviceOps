package com.android.deviceops.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.MainViewModel
import kotlin.math.abs
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.Animatable
import kotlin.math.roundToInt

// ── 开关轨道颜色插值端点（匹配 HTML 规范） ────────────────────────────────
// Off: rgb(99,99,104)  On: rgb(66,105,255)
private fun lerpTrackColor(ratio: Float): Color {
    val r = (99 + (66 - 99) * ratio).roundToInt().coerceIn(0, 255)
    val g = (99 + (105 - 99) * ratio).roundToInt().coerceIn(0, 255)
    val b = (104 + (255 - 104) * ratio).roundToInt().coerceIn(0, 255)
    return Color(r / 255f, g / 255f, b / 255f)
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
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 标题：27sp / W500 / letter-spacing -0.2sp
                Text(
                    text          = "Device Ops",
                    fontSize      = 27.sp,
                    fontWeight    = FontWeight.W500,
                    color         = White,
                    letterSpacing = (-0.2).sp,
                    modifier      = Modifier.padding(bottom = 6.dp)
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

/**
 * 统一卡片组件：
 *  - 左侧父按钮区域：点击→跳转，有背景亮度+文字缩放反馈
 *  - 1dp 灰细分隔线（视觉分隔，不拦截事件）
 *  - 右侧开关区域：支持点击切换 + 拖拽手势 + 实时颜色插值
 */
@Composable
private fun SplitCard(
    label: String,
    checked: Boolean,
    onContainerClick: () -> Unit,
    onCheckedChange: () -> Unit,
) {
    // 父区域按压状态（通过 InteractionSource 获取，不需要手动 gesture）
    val labelSource = remember { MutableInteractionSource() }
    val isLabelPressed by labelSource.collectIsPressedAsState()

    // 卡片底色：按下 #2E2E32，松开 #252528
    val cardBg by animateColorAsState(
        targetValue    = if (isLabelPressed) CardBgPressed else CardBg,
        animationSpec  = if (isLabelPressed) tween(80) else tween(250),
        label          = "cardBg"
    )

    // 文字微缩：按下 0.975，松开 1.0（弹性回弹）
    val labelScale by animateFloatAsState(
        targetValue   = if (isLabelPressed) 0.975f else 1f,
        animationSpec = if (isLabelPressed) tween(80)
                        else spring(dampingRatio = 0.5f, stiffness = 500f),
        label         = "labelScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(38.dp))
            .background(cardBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── 父按钮区域 ─────────────────────────────────────────────────────
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
                text          = label,
                fontSize      = 21.sp,
                fontWeight    = FontWeight.W400,
                color         = White,
                letterSpacing = (-0.1).sp,
                modifier      = Modifier.scale(labelScale)
            )
        }

        // ── 分隔线：1dp × 42dp ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(42.dp)
                .background(DividerCol)
        )
        Spacer(Modifier.width(4.dp))

        // ── 开关控制区域：94dp，右内边距 12dp ─────────────────────────────
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
 * 自定义开关：
 *  轨道 58×34dp，radius 17dp
 *  滑块 27dp，#FCFCFF，垂直居中（top=3.5dp），左偏移 3.5dp
 *  ON → translateX 24dp；OFF → translateX 0
 *  按压：滑块缩至 23dp + 白色反馈光圈（opacity 0.12）
 *  拖拽：实时插值轨道颜色（Off rgb(99,99,104) ↔ On rgb(66,105,255)）
 */
/**
 * 自定义开关：使用 Animatable 控制动画，避免拖拽结束时从后台值跳变（抽搐）。
 * 拖拽过程 snapTo 实时同步；松手后从当前位置 animateTo 终点。
 */
@Composable
private fun ToggleSwitch(checked: Boolean, onToggle: () -> Unit) {
    val density     = LocalDensity.current
    val maxPx       = with(density) { 24.dp.toPx() }
    val scope       = rememberCoroutineScope()
    val latest      = rememberUpdatedState(checked)   // 永远最新值，不重建 gesture

    // Animatable：完全手动控制，不自动追踪 checked 变化
    val thumbAnim   = remember { Animatable(if (checked) 1f else 0f) }
    val trackAnim   = remember { Animatable(if (checked) 1f else 0f) }

    // 仅当外部改变 checked（不是从拖拽触发）时才做动画
    var gestureJustToggled by remember { mutableStateOf(false) }
    LaunchedEffect(checked) {
        if (!gestureJustToggled) {
            val t = if (checked) 1f else 0f
            launch { thumbAnim.animateTo(t, spring(0.75f, 600f)) }
            launch { trackAnim.animateTo(t, tween(250)) }
        }
        gestureJustToggled = false
    }

    var isPressed  by remember { mutableStateOf(false) }
    val thumbSize  by animateDpAsState(if (isPressed) 23.dp else 27.dp, tween(200), label = "sz")
    val feedAlpha  by animateFloatAsState(if (isPressed) 0.12f else 0f,  tween(200), label = "fa")

    val ratio      = thumbAnim.value
    val thumbOff   = with(density) { (ratio * maxPx).toDp() }
    val thumbLeft  = 3.5.dp + thumbOff + (27.dp - thumbSize) / 2
    val thumbTop   = (34.dp - thumbSize) / 2
    val trackColor = lerpTrackColor(trackAnim.value)

    Box(
        modifier = Modifier
            .width(58.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(trackColor)
            .pointerInput(Unit) {                 // Unit：不因 checked 变化重建 gesture
                awaitEachGesture {
                    val down   = awaitFirstDown(requireUnconsumed = false)
                    isPressed  = true
                    val startX = down.position.x
                    val startR = thumbAnim.value
                    // 按下时停止正在运行的动画
                    scope.launch { thumbAnim.stop(); trackAnim.stop() }

                    while (true) {
                        val event  = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        if (!change.pressed) {
                            // ── 松手 ──────────────────────────────────────
                            val delta  = change.position.x - startX
                            val isTap  = abs(delta) < with(density) { 4.dp.toPx() }
                            val newOn  = if (isTap) !latest.value else thumbAnim.value > 0.5f
                            val target = if (newOn) 1f else 0f
                            isPressed  = false

                            // 从当前位置动画到终点（无跳变）
                            scope.launch {
                                launch { thumbAnim.animateTo(target, spring(0.75f, 600f)) }
                                launch { trackAnim.animateTo(target, tween(250)) }
                            }
                            if (newOn != latest.value) {
                                gestureJustToggled = true
                                onToggle()
                            }
                            break
                        }

                        // ── 拖拽中：snapTo 实时同步，不产生后台动画 ──────
                        val delta = change.position.x - startX
                        if (abs(delta) > with(density) { 4.dp.toPx() }) change.consume()
                        val newR = (startR + delta / maxPx).coerceIn(0f, 1f)
                        scope.launch {
                            thumbAnim.snapTo(newR)
                            trackAnim.snapTo(newR)
                        }
                    }
                }
            }
    ) {
        // 按压反馈光圈
        if (feedAlpha > 0f) {
            Box(
                modifier = Modifier
                    .offset(x = thumbLeft - 13.5.dp, y = thumbTop - 13.5.dp)
                    .size(54.dp).clip(CircleShape)
                    .background(White.copy(alpha = feedAlpha))
            )
        }
        // 滑块
        Box(
            modifier = Modifier
                .offset(x = thumbLeft, y = thumbTop)
                .size(thumbSize).clip(CircleShape)
                .background(Thumb)
        )
    }
}
