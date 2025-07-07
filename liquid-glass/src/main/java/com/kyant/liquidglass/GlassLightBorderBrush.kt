package com.kyant.liquidglass

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import kotlin.math.PI

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class GlassLightBorderBrush(
    private val color: Color,
    private val cornerRadius: Float,
    private val borderWidth: Float,
    private val lightSourceAngle: Float,
    private val lightSourceDecay: Float
) : ShaderBrush() {

    override fun createShader(size: Size): Shader {
        return RuntimeShader(
            """// This file belongs to Kyant. You must not use it without permission.
    ${LiquidGlassShaders.sdRectangleShaderUtils}
    
    half4 main(float2 coord) {
        float cornerRadius = ${cornerRadius};
        float2 size = float2(${size.width}, ${size.height});
        float2 halfSize = size * 0.5;
        float2 centeredCoord = coord - halfSize;
        
        float2 grad = gradSdRoundedRectangle(centeredCoord, halfSize, cornerRadius);
        float2 topLightNormal = float2(-cos(${lightSourceAngle / 180.0 * PI}), -sin(${lightSourceAngle / 180.0 * PI}));
        float topLightFraction = dot(topLightNormal, grad);
        float bottomLightFraction = dot(-topLightNormal, grad);
        float fraction = pow(max(topLightFraction, bottomLightFraction), $lightSourceDecay);
        
        float sd = sdRoundedRectangle(centeredCoord, halfSize, cornerRadius);
        sd = min(sd, 0.0);
        fraction = mix(fraction, 0.0, pow(-sd / $borderWidth, 0.5));
        
        return half4(${color.red}, ${color.green}, ${color.blue}, ${color.alpha}) * fraction;
    }"""
        )
    }
}
