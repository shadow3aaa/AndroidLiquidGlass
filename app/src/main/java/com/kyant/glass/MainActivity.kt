package com.kyant.glass

import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import com.kyant.expressa.m3.LocalColorScheme
import com.kyant.expressa.m3.color.ColorScheme
import com.kyant.expressa.overscroll.rememberOffsetOverscrollFactory
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ripple.LocalRippleConfiguration
import com.kyant.expressa.ripple.RippleConfiguration
import com.kyant.expressa.ripple.ripple
import com.kyant.expressa.ui.LocalContentColor
import com.kyant.glass.playground.Preview
import com.kyant.glass.playground.PreviewState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val rippleConfiguration = remember {
                RippleConfiguration(
                    rippleAlpha = RippleAlpha(
                        hoveredAlpha = 2f * 0.08f,
                        focusedAlpha = 2f * 0.10f,
                        pressedAlpha = 2f * 0.10f,
                        draggedAlpha = 2f * 0.16f
                    )
                )
            }
            val overscrollFactory = rememberOffsetOverscrollFactory()

            CompositionLocalProvider(
                LocalColorScheme provides ColorScheme.systemDynamic()
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides onSurface,
                    LocalRippleConfiguration provides rippleConfiguration,
                    LocalIndication provides ripple(),
                    LocalOverscrollFactory provides overscrollFactory,
                ) {
                    val view = LocalView.current
                    val background = surfaceContainer
                    LaunchedEffect(view, background) {
                        view.rootView.background = background.toArgb().toDrawable()
                    }

                    val previewState = remember { PreviewState() }
                    Preview(previewState)
                }
            }
        }

        window.decorView.windowInsetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
