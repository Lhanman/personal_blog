package com.personalblog.app.ui.screen

import androidx.compose.runtime.Composable
import com.personalblog.app.ui.DeprecatedUiScreen
import com.personalblog.app.ui.viewmodel.AuthViewModel

@Deprecated("默认登录 UI 已弃用，等待设计稿实现。")
@Suppress("UNUSED_PARAMETER")
@Composable
fun LoginScreen(viewModel: AuthViewModel, onLoginSuccess: () -> Unit) {
    DeprecatedUiScreen("LoginScreen")
}
