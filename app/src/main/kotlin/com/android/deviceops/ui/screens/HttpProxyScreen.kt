package com.android.deviceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.RotaryScrollableDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.android.deviceops.ui.theme.*
import com.android.deviceops.viewmodel.HttpProxyViewModel

@Composable
fun HttpProxyScreen(onBack: () -> Unit, vm: HttpProxyViewModel = viewModel()) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.init(context) }

    val host       by vm.host.collectAsStateWithLifecycle()
    val port       by vm.port.collectAsStateWithLifecycle()
    val isSaved    by vm.isSaved.collectAsStateWithLifecycle()
    val hasChanges by vm.hasChanges.collectAsStateWithLifecycle()
    val columnState = rememberTransformingLazyColumnState()
    val inputColor  = if (isSaved) TextSecondary else TextPrimary

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier.fillMaxSize().background(Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
            flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(columnState),
            rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(columnState)
        ) {
            item {
                Text("HTTP 代理", color = TextPrimary, fontSize = 15.sp,
                    fontWeight = FontWeight.W500, modifier = Modifier.padding(bottom = 12.dp))
            }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProxyField("主机", host, "输入主机地址", inputColor,
                        KeyboardOptions(imeAction = ImeAction.Next)) { vm.setHost(it) }
                    ProxyField("端口", port, "0 – 65535", inputColor,
                        KeyboardOptions(keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done)) { vm.setPort(it) }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { if (hasChanges) { vm.save(context); onBack() } },
                    enabled = hasChanges,
                    modifier = Modifier.width(140.dp).height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("确定", fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ProxyField(
    label: String, value: String, placeholder: String,
    textColor: androidx.compose.ui.graphics.Color,
    keyboardOptions: KeyboardOptions, onValue: (String) -> Unit
) {
    Column {
        Text(label, color = TextSecondary, fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        BasicTextField(
            value = value, onValueChange = onValue, singleLine = true,
            keyboardOptions = keyboardOptions,
            textStyle = TextStyle(color = textColor, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = TextTertiary, fontSize = 14.sp)
                inner()
            }
        )
    }
}
