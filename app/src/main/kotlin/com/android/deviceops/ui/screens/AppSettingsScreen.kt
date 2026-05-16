package com.android.deviceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.android.deviceops.viewmodel.ManageAppsViewModel

private enum class AppMode { TEST_DISABLE, ENABLE, DISABLE }

@Composable
fun AppSettingsScreen(
    packageName: String,
    onBack: () -> Unit,
    vm: ManageAppsViewModel = viewModel()
) {
    val context  = androidx.compose.ui.platform.LocalContext.current
    val app      = vm.getAppInfo(packageName)
    val isSystem = app?.isSystemApp ?: false

    var selected by remember {
        mutableStateOf(
            when {
                app?.countdownSeconds != null -> AppMode.TEST_DISABLE
                app?.isDisabled == true       -> AppMode.DISABLE
                else                          -> AppMode.ENABLE
            }
        )
    }

    val countdowns by vm.filteredApps.collectAsStateWithLifecycle()
    val liveApp    = countdowns.find { it.packageName == packageName }
    val countdown  = liveApp?.countdownSeconds

    val columnState = rememberTransformingLazyColumnState()

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
                ListHeader {
                    Text(app?.label ?: packageName, fontSize = 14.sp)
                }
            }

            if (isSystem) item {
                RadioButton(
                    selected = selected == AppMode.TEST_DISABLE,
                    onSelect = { selected = AppMode.TEST_DISABLE },
                    label    = { Text("测试停用") }
                )
            }

            item {
                RadioButton(
                    selected = selected == AppMode.ENABLE,
                    onSelect = { selected = AppMode.ENABLE },
                    label    = { Text("启用") }
                )
            }

            item {
                RadioButton(
                    selected = selected == AppMode.DISABLE,
                    onSelect = { selected = AppMode.DISABLE },
                    label    = { Text("停用") }
                )
            }

            item {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        when (selected) {
                            AppMode.ENABLE       -> vm.enableApp(context, packageName)
                            AppMode.DISABLE      -> vm.disableApp(context, packageName)
                            AppMode.TEST_DISABLE -> vm.testDisableApp(context, packageName)
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .width(140.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    when {
                        countdown != null -> {
                            val m = countdown / 60
                            val s = countdown % 60
                            Text("$m:${s.toString().padStart(2, '0')}", fontSize = 15.sp)
                        }
                        selected == AppMode.TEST_DISABLE -> Text("6分钟", fontSize = 15.sp)
                        else -> Text("确定", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
