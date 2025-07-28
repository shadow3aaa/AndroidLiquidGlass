package com.kyant.liquidglass.sampler

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastCoerceIn

@ExperimentalLuminanceSamplerApi
class ContinuousLuminanceSampler(
    initialLuminance: Float = 0.5f,
    durationMillis: Long = 300L,
    easing: Easing = LinearEasing,
    val precision: Float = 0.25f,
    val scaledSize: IntSize = IntSize(5, 5)
) : LuminanceSampler {

    private val luminanceAnimation = Animatable(initialLuminance)

    private val animationSpec =
        tween<Float>(durationMillis.toInt(), 0, easing)

    override val sampleIntervalMillis: Long = 0L

    override val luminance: Float
        get() = luminanceAnimation.value

    private val impulseLuminanceSampler =
        ImpulseLuminanceSampler(
            initialLuminance = initialLuminance,
            sampleIntervalMillis = sampleIntervalMillis,
            precision = precision,
            scaledSize = scaledSize
        )

    override suspend fun sample(graphicsLayer: GraphicsLayer): Float {
        val sampledLuminance = impulseLuminanceSampler.sample(graphicsLayer)
        luminanceAnimation.animateTo(sampledLuminance, animationSpec)
        return luminanceAnimation.value.fastCoerceIn(0f, 1f)
    }
}
