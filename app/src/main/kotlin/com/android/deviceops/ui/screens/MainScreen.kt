package com.android.deviceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
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

    val columnState = rememberTransformingLazyColumnState()

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
                    text = "Device Ops",
                    color = PrimaryText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            item {
                SplitToggleButton(
                    checked         = proxyEnabled,
                    onCheckedChange = { vm.toggleProxy(context) },
                    onClick         = onHttpProxyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    colors = ToggleButtonDefaults.splitToggleButtonColors(
                        checkedContainerColor        = ChipBackground,
                        uncheckedContainerColor      = ChipBackground,
                        checkedSplitContainerColor   = ChipBackground,
                        uncheckedSplitContainerColor = ChipBackground,
                    ),
                    toggleControl = {
                        Switch(
                            checked         = proxyEnabled,
                            onCheckedChange = null,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = SwitchThumb,
                                checkedTrackColor   = SwitchTrackOn,
                                uncheckedThumbColor = SwitchThumb,
                                uncheckedTrackColor = SwitchTrackOff,
                            )
                        )
                    }
                ) {
                    Text(text = "HTTP Proxy", color = PrimaryText, fontSize = 14.sp)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
            }

            item {
                SplitToggleButton(
                    checked         = manageEnabled,
                    onCheckedChange = { vm.toggleManageApps(context) },
                    onClick         = onManageAppsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    colors = ToggleButtonDefaults.splitToggleButtonColors(
                        checkedContainerColor        = ChipBackground,
                        uncheckedContainerColor      = ChipBackground,
                        checkedSplitContainerColor   = ChipBackground,
                        uncheckedSplitContainerColor = ChipBackground,
                    ),
                    toggleControl = {
                        Switch(
                            checked         = manageEnabled,
                            onCheckedChange = null,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = SwitchThumb,
                                checkedTrackColor   = SwitchTrackOn,
                                uncheckedThumbColor = SwitchThumb,
                                uncheckedTrackColor = SwitchTrackOff,
                            )
                        )
                    }
                ) {
                    Text(text = "管理停用应用", color = PrimaryText, fontSize = 14.sp)
                }
            }
        }
    }
}
