package com.btd.maiscreen.ui.screens

import androidx.compose.runtime.Composable
import com.btd.maiscreen.BuildConfig

@Composable
actual fun isDebugBuild(): Boolean {
    return BuildConfig.DEBUG
}

