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
                    .padding(horizontal = 16.dp),
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


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(cardBg)
            .clickable(
                interactionSource = labelSource,
                indication        = null,
                onClick           = onContainerClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 内容整体缩放（卡片轮廓不动，内容 recoil）
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .scale(cardScale)
                .padding(start = 28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text          = label,
                fontSize      = 21.sp,
                fontWeight    = FontWeight.W400,
                color         = White,
                letterSpacing = (-0.1).sp,
                modifier      = Modifier
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
@Composable
private fun ToggleSwitch(checked: Boolean, onToggle: () -> Unit) {
    val density      = LocalDensity.current
    val maxOffsetDp  = 24.dp
    val maxOffsetPx  = with(density) { maxOffsetDp.toPx() }

    var isDragging   by remember { mutableStateOf(false) }
    var isPressed    by remember { mutableStateOf(false) }
    var dragRatio    by remember { mutableFloatStateOf(if (checked) 1f else 0f) }

    // 非拖拽时：带弹性曲线的位移动画
    val animOffset by animateDpAsState(
        targetValue   = if (checked) maxOffsetDp else 0.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label         = "offset"
    )

    // 非拖拽时：轨道颜色过渡
    val animTrack by animateColorAsState(
        targetValue   = if (checked) TrackOn else TrackOff,
        animationSpec = tween(250),
        label         = "track"
    )

    val displayTrack: Color = if (isDragging) lerpTrackColor(dragRatio) else animTrack
    val displayOffset: Dp   = if (isDragging) {
        with(density) { (dragRatio * maxOffsetPx).toDp() }
    } else animOffset

    // 按压时滑块缩小：27dp → 23dp
    val thumbSize by animateDpAsState(
        targetValue   = if (isPressed) 23.dp else 27.dp,
        animationSpec = tween(200),
        label         = "thumb"
    )

    // 按压反馈光圈透明度
    val feedbackAlpha by animateFloatAsState(
        targetValue   = if (isPressed) 0.12f else 0f,
        animationSpec = tween(200),
        label         = "feedback"
    )

    // 滑块中心始终垂直居中于轨道（top = (34dp - thumbSize) / 2）
    // 水平位置 = 3.5dp（起始边距）+ 偏移 + 因尺寸变化的居中补偿
    val thumbLeft = 3.5.dp + displayOffset + (27.dp - thumbSize) / 2
    val thumbTop  = (34.dp - thumbSize) / 2

    Box(
        modifier = Modifier
            .width(58.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(displayTrack)
            .pointerInput(checked) {
                awaitEachGesture {
                    // ── 按下 ──────────────────────────────────────────────
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()  // 阻止冒泡到外层 Row clickable
                    isPressed  = true
                    isDragging = false
                    val startX     = down.position.x
                    val startRatio = if (checked) 1f else 0f
                    dragRatio      = startRatio

                    // ── 等待移动或抬起 ────────────────────────────────────
                    while (true) {
                        val event  = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break

                        if (!change.pressed) {
                            // 松手：判断是点击还是拖拽
                            val delta = change.position.x - startX
                            val newChecked = if (abs(delta) < with(density) { 4.dp.toPx() }) {
                                !checked            // 轻触 → 取反
                            } else {
                                dragRatio > 0.5f    // 拖拽 → 过半线判定
                            }
                            isPressed  = false
                            isDragging = false
                            if (newChecked != checked) onToggle()
                            break
                        }

                        val delta = change.position.x - startX
                        if (abs(delta) > with(density) { 4.dp.toPx() }) {
                            isDragging = true
                            change.consume()
                        }
                        if (isDragging) {
                            dragRatio = (startRatio + delta / maxOffsetPx).coerceIn(0f, 1f)
                        }
                    }
                }
            }
    ) {
        // 按压反馈光圈（居中于滑块）
        if (feedbackAlpha > 0f) {
            Box(
                modifier = Modifier
                    .offset(x = thumbLeft - 13.5.dp, y = thumbTop - 13.5.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = feedbackAlpha))
            )
        }

        // 滑块
        Box(
            modifier = Modifier
                .offset(x = thumbLeft, y = thumbTop)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Thumb)
        )
    }
}
