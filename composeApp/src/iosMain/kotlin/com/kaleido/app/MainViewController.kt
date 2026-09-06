package com.kaleido.app

import androidx.compose.ui.window.ComposeUIViewController
import com.kaleido.app.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() },
) {
    App()
}
