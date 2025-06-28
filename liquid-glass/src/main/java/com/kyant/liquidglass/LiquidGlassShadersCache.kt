package com.kyant.liquidglass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi

internal class LiquidGlassShadersCache() {

    private var _colorManipulationShader: RuntimeShader? = null
    private var _refractionShader: RuntimeShader? = null
    private var _bleedShader: RuntimeShader? = null

    val colorManipulationShader: RuntimeShader
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        get() {
            if (_colorManipulationShader == null) {
                _colorManipulationShader = RuntimeShader(LiquidGlassShaders.colorManipulationShaderString)
            }
            return _colorManipulationShader!!
        }

    val refractionShader: RuntimeShader
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        get() {
            if (_refractionShader == null) {
                _refractionShader = RuntimeShader(LiquidGlassShaders.refractionShaderString)
            }
            return _refractionShader!!
        }

    val bleedShader: RuntimeShader
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        get() {
            if (_bleedShader == null) {
                _bleedShader = RuntimeShader(LiquidGlassShaders.bleedShaderString)
            }
            return _bleedShader!!
        }
}
