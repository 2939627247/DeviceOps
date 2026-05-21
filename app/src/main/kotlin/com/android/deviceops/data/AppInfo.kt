package com.android.deviceops.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val isDisabled: Boolean = false,
    val countdownSeconds: Long? = null
)

enum class AppFilter { ALL, USER, SYSTEM }
