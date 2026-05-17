package com.android.deviceops.ui.theme

import androidx.compose.ui.graphics.Color

// ── 基础色 ────────────────────────────────────────────────────────────────
val Black   = Color(0xFF000000)
val White   = Color(0xFFFFFFFF)

// ── 卡片 / 容器 ───────────────────────────────────────────────────────────
val CardBg        = Color(0xFF252528)   // 默认卡片底色
val CardBgPressed = Color(0xFF2E2E32)   // 父区域按下反馈
val SurfaceLow    = Color(0xFF1A1A1C)

// ── 品牌蓝 ────────────────────────────────────────────────────────────────
val Brand = Color(0xFF4269FF)

// ── 开关 ──────────────────────────────────────────────────────────────────
val TrackOn  = Color(0xFF4269FF)   // 开启轨道
val TrackOff = Color(0xFF636368)   // 关闭轨道   rgb(99,99,104)
val Thumb    = Color(0xFFFCFCFF)   // 滑块（两态）

// ── 文字 ──────────────────────────────────────────────────────────────────
val TextPrimary   = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB7B7BB)
val TextTertiary  = Color(0xFF8E8E93)

// ── 语义色 ────────────────────────────────────────────────────────────────
val ErrorRed    = Color(0xFFD93E36)
val DividerCol  = Color(0xFF4D4D52)

// ── 伪装屏专用（保持原样） ────────────────────────────────────────────────
val Background  = Black
val ButtonActive = Brand
val PrimaryText  = TextPrimary
