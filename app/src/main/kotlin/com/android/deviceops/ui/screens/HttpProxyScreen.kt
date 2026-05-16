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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.android.deviceops.viewmodel.HttpProxyViewModel

@Composable
fun HttpProxyScreen(
    onBack: () -> Unit,
    vm: HttpProxyViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.init(context) }

    val host       by vm.host.collectAsStateWithLifecycle()
    val port       by vm.port.collectAsStateWithLifecycle()
    val isSaved    by vm.isSaved.collectAsStateWithLifecycle()
    val hasChanges by vm.hasChanges.collectAsStateWithLifecycle()

    val columnState = rememberTransformingLazyColumnState()
    val inputColor  = if (isSaved)
        MaterialTheme.colorScheme.onSurfaceVariant
    else
        MaterialTheme.colorScheme.onSurface

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
                ListHeader { Text("HTTP 代理", fontSize = 15.sp) }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    Text(
                        "主机",
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                    ProxyField(
                        value       = host,
                        onValue     = vm::setHost,
                        placeholder = "输入主机地址",
                        textColor   = inputColor,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    Text(
                        "端口",
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                    ProxyField(
                        value       = port,
                        onValue     = vm::setPort,
                        placeholder = "0 – 65535",
                        textColor   = inputColor,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction    = ImeAction.Done
                        )
                    )
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { if (hasChanges) { vm.save(context); onBack() } },
                    enabled  = hasChanges,
                    modifier = Modifier.width(140.dp).height(48.dp),
                    shape    = RoundedCornerShape(24.dp)
                ) {
                    Text("确定", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ProxyField(
    value: String,
    onValue: (String) -> Unit,
    placeholder: String,
    textColor: androidx.compose.ui.graphics.Color,
    keyboardOptions: KeyboardOptions,
) {
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val hint    = MaterialTheme.colorScheme.onSurfaceVariant
    BasicTextField(
        value           = value,
        onValueChange   = onValue,
        singleLine      = true,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(color = textColor, fontSize = 14.sp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(surface)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(placeholder, color = hint, fontSize = 14.sp)
            inner()
        }
    )
}
