package com.android.deviceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
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
                ListHeader { Text("DeviceOps", fontSize = 17.sp) }
            }

            item {
                // 父按钮区域（文字+空白）点击 → 进入 HTTP 代理设置
                // 开关区域点击 → 仅切换代理开关，互不干扰
                SplitSwitchButton(
                    checked                  = proxyEnabled,
                    onCheckedChange          = { vm.toggleProxy(context) },
                    toggleContentDescription = if (proxyEnabled) "关闭 HTTP 代理" else "开启 HTTP 代理",
                    onContainerClick         = onHttpProxyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    label          = { Text("HTTP 代理", fontSize = 14.sp) },
                    secondaryLabel = { Text(if (proxyEnabled) "已启用" else "未启用", fontSize = 11.sp) }
                )
            }

            item { Spacer(Modifier.height(6.dp)) }

            item {
                // 父按钮区域点击 → 进入应用管理列表
                // 开关区域点击 → 仅切换管理开关，互不干扰
                SplitSwitchButton(
                    checked                  = manageEnabled,
                    onCheckedChange          = { vm.toggleManageApps(context) },
                    toggleContentDescription = if (manageEnabled) "关闭应用管理" else "开启应用管理",
                    onContainerClick         = onManageAppsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    label          = { Text("管理停用应用", fontSize = 14.sp) },
                    secondaryLabel = { Text(if (manageEnabled) "已启用" else "未启用", fontSize = 11.sp) }
                )
            }
        }
    }
}
