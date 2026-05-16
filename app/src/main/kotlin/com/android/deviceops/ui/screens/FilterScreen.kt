package com.android.deviceops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.android.deviceops.data.AppFilter
import com.android.deviceops.viewmodel.ManageAppsViewModel

@Composable
fun FilterScreen(
    onBack: () -> Unit,
    vm: ManageAppsViewModel = viewModel()
) {
    val current     by vm.filter.collectAsStateWithLifecycle()
    val columnState = rememberTransformingLazyColumnState()

    val options = listOf(
        AppFilter.ALL    to "全部应用",
        AppFilter.USER   to "用户应用",
        AppFilter.SYSTEM to "系统应用",
    )

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
                ListHeader { Text("筛选方式", fontSize = 15.sp) }
            }

            items(options.size) { idx ->
                val (filter, label) = options[idx]
                RadioButton(
                    selected = current == filter,
                    onSelect = {
                        vm.setFilter(filter)
                        onBack()
                    },
                    label = { Text(label, fontSize = 14.sp) }
                )
            }
        }
    }
}
