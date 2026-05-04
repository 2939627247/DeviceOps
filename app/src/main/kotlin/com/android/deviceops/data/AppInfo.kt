package com.android.deviceops.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSystemApp: Boolean,
    val isDisabled: Boolean = false,
    /** Non-null while a test-disable countdown is running (seconds remaining). */
    val countdownSeconds: Long? = null
)

enum class AppFilter { ALL, USER, SYSTEM }
