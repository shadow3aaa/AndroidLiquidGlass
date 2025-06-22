package com.kyant.liquidglass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

@Composable
fun rememberLiquidGlassState(): LiquidGlassState {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) {
        LiquidGlassState(graphicsLayer)
    }
}

class LiquidGlassState internal constructor(
    internal val graphicsLayer: GraphicsLayer
) {

    internal var rect: Rect? by mutableStateOf(null)

    private var _colorManipulationShader: RuntimeShader? = null
    private var _refractionShader: RuntimeShader? = null
    private var _bleedShader: RuntimeShader? = null

    internal val colorManipulationShader: RuntimeShader
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        get() {
            if (_colorManipulationShader == null) {
                _colorManipulationShader = RuntimeShader(LiquidGlassShaders.colorManipulationShaderString)
            }
            return _colorManipulationShader!!
        }

    internal val refractionShader: RuntimeShader
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        get() {
            if (_refractionShader == null) {
                _refractionShader = RuntimeShader(LiquidGlassShaders.refractionShaderString)
            }
            return _refractionShader!!
        }

    internal val bleedShader: RuntimeShader
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        get() {
            if (_bleedShader == null) {
                _bleedShader = RuntimeShader(LiquidGlassShaders.bleedShaderString)
            }
            return _bleedShader!!
        }
}
