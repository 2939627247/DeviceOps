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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.material3.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.lazy.rememberTransformingLazyColumnState
import com.android.deviceops.ui.theme.*
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
    val inputColor  = if (isSaved) InputTextSaved else InputTextActive

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            state = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 28.dp)
        ) {
            item {
                Text(
                    text = "Set global HTTP proxy",
                    color = PrimaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    Text(
                        text = "Host",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                    ProxyInputField(
                        value       = host,
                        onValue     = { vm.setHost(it) },
                        placeholder = "输入主机地址",
                        textColor   = inputColor,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
                    Text(
                        text = "Port",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                    ProxyInputField(
                        value       = port,
                        onValue     = { vm.setPort(it) },
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
                    onClick = {
                        if (hasChanges) {
                            vm.save(context)
                            onBack()
                        }
                    },
                    enabled = hasChanges,
                    modifier = Modifier
                        .width(150.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = ButtonActive,
                        disabledContainerColor = ButtonInactive,
                        contentColor           = PrimaryText,
                        disabledContentColor   = PrimaryText
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("确定", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ProxyInputField(
    value: String,
    onValue: (String) -> Unit,
    placeholder: String,
    textColor: androidx.compose.ui.graphics.Color,
    keyboardOptions: KeyboardOptions,
) {
    BasicTextField(
        value           = value,
        onValueChange   = onValue,
        singleLine      = true,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(color = textColor, fontSize = 14.sp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(InputBoxBg)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(text = placeholder, color = SecondaryText, fontSize = 14.sp)
            }
            inner()
        }
    )
}
