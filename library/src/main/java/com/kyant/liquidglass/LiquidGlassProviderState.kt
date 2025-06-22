package com.kyant.liquidglass

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

val LocalLiquidGlassProviderState: ProvidableCompositionLocal<LiquidGlassProviderState> =
    staticCompositionLocalOf { error("CompositionLocal LocalLiquidGlassProviderState not present") }

@Composable
fun rememberLiquidGlassProviderState(): LiquidGlassProviderState {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) {
        LiquidGlassProviderState(graphicsLayer)
    }
}

class LiquidGlassProviderState
internal constructor(
    internal val graphicsLayer: GraphicsLayer,
) {

    internal var rect: Rect? by mutableStateOf(null)
}

fun Modifier.liquidGlassProvider(
    state: LiquidGlassProviderState,
    backgroundColor: Color? = null
): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this
            .drawWithContent {
                drawContent()

                state.graphicsLayer.record {
                    backgroundColor?.let { drawRect(it) }
                    this@drawWithContent.drawContent()
                }
            }
            .onGloballyPositioned { layoutCoordinates ->
                state.rect = layoutCoordinates.boundsInRoot()
            }
    } else {
        this
    }
