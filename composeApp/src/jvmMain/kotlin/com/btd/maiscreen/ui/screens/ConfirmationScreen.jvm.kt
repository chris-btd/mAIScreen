package com.btd.maiscreen.ui.screens

import androidx.compose.runtime.Composable

@Composable
actual fun isDebugBuild(): Boolean {
    // For desktop, always return true in development
    // You can make this more sophisticated if needed
    return true
}

