package com.android.deviceops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.android.deviceops.ui.screens.AppSettingsScreen
import com.android.deviceops.ui.screens.DisguiseScreen
import com.android.deviceops.ui.screens.FilterScreen
import com.android.deviceops.ui.screens.HttpProxyScreen
import com.android.deviceops.ui.screens.MainScreen
import com.android.deviceops.ui.screens.ManageAppsScreen
import com.android.deviceops.ui.screens.SearchResultsScreen
import com.android.deviceops.ui.theme.DeviceOpsTheme

object Routes {
    const val DISGUISE     = "disguise"
    const val MAIN         = "main"
    const val HTTP_PROXY   = "http_proxy"
    const val MANAGE_APPS  = "manage_apps"
    const val FILTER       = "filter"
    const val APP_SETTINGS = "app_settings/{packageName}"
    const val SEARCH       = "search/{query}"

    fun appSettings(pkg: String) = "app_settings/$pkg"
    fun search(query: String)    = "search/$query"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 禁用所有窗口动画
        window.setWindowAnimations(0)
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContent {
            DeviceOpsTheme {
                AppScaffold {
                    val nav = rememberSwipeDismissableNavController()

                    SwipeDismissableNavHost(
                        navController    = nav,
                        startDestination = Routes.DISGUISE
                    ) {
                        composable(Routes.DISGUISE) {
                            DisguiseScreen(
                                onShortPress = { finish() },
                                onLongPress  = {
                                    nav.navigate(Routes.MAIN) {
                                        popUpTo(Routes.DISGUISE) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.MAIN) {
                            MainScreen(
                                onHttpProxyClick  = { nav.navigate(Routes.HTTP_PROXY) },
                                onManageAppsClick = { nav.navigate(Routes.MANAGE_APPS) }
                            )
                        }

                        composable(Routes.HTTP_PROXY) {
                            HttpProxyScreen(onBack = { nav.popBackStack() })
                        }

                        composable(Routes.MANAGE_APPS) {
                            ManageAppsScreen(
                                onAppClick    = { pkg   -> nav.navigate(Routes.appSettings(pkg)) },
                                onFilterClick = {          nav.navigate(Routes.FILTER) },
                                onSearchDone  = { query -> nav.navigate(Routes.search(query)) }
                            )
                        }

                        composable(Routes.APP_SETTINGS) { back ->
                            val pkg = back.arguments?.getString("packageName") ?: ""
                            AppSettingsScreen(
                                packageName = pkg,
                                onBack      = { nav.popBackStack() }
                            )
                        }

                        composable(Routes.FILTER) {
                            FilterScreen(onBack = { nav.popBackStack() })
                        }

                        composable(Routes.SEARCH) { back ->
                            val query = back.arguments?.getString("query") ?: ""
                            SearchResultsScreen(
                                query      = query,
                                onAppClick = { pkg -> nav.navigate(Routes.appSettings(pkg)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
