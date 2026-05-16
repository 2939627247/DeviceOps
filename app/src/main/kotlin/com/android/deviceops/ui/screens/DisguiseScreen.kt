package com.android.deviceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.android.deviceops.ui.theme.Background
import com.android.deviceops.ui.theme.ButtonActive
import com.android.deviceops.ui.theme.PrimaryText
import kotlinx.coroutines.withTimeoutOrNull

private const val LONG_PRESS_THRESHOLD_MS = 790L

@Composable
fun DisguiseScreen(
    onShortPress: () -> Unit,
    onLongPress: () -> Unit,
) {
    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.weight(1.2f))

                Text(
                    text = "请将手表连接至您的手机，\n然后重试。",
                    color = PrimaryText,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .width(164.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(ButtonActive)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    // 按住满 0.79s 立即触发长按，不等松手
                                    val releasedEarly = withTimeoutOrNull(LONG_PRESS_THRESHOLD_MS) {
                                        tryAwaitRelease()
                                        true // 松手了，返回 true
                                    }
                                    if (releasedEarly != null) {
                                        // 0.79s 内松手 = 短按
                                        onShortPress()
                                    } else {
                                        // 按住满 0.79s = 立即跳转，不需要松手
                                        onLongPress()
                                        tryAwaitRelease() // 消费剩余的按压事件
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "确定", color = PrimaryText, fontSize = 16.sp)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
