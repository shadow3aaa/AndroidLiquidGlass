package com.kyant.liquidglass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.PI

@Immutable
sealed interface GlassBorder {

    @Stable
    fun createRenderEffect(density: Density, size: Size, cornerRadius: Float): RenderEffect? {
        return null
    }

    @Immutable
    data object None : GlassBorder

    @Immutable
    data object Solid : GlassBorder

    @Immutable
    data class Highlight(
        val angle: Float = 45f,
        val decay: Float = 1f
    ) : GlassBorder {

        private var highlightShaderCache: RuntimeShader? = null

        override fun createRenderEffect(density: Density, size: Size, cornerRadius: Float): RenderEffect? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val blurRenderEffect =
                    cachedBlurRenderEffect
                        ?: RenderEffect.createBlurEffect(
                            with(density) { 0.5f.dp.toPx() },
                            with(density) { 0.5f.dp.toPx() },
                            Shader.TileMode.DECAL
                        ).also { cachedBlurRenderEffect = it }

                val highlightShader = highlightShaderCache
                    ?: RuntimeShader(LiquidGlassShaders.highlightShaderString)
                        .also { highlightShaderCache = it }

                val highlightRenderEffect =
                    RenderEffect.createRuntimeShaderEffect(
                        highlightShader.apply {
                            setFloatUniform("size", size.width, size.height)
                            setFloatUniform("cornerRadius", cornerRadius)
                            setFloatUniform("angle", angle * PI.toFloat() / 180f)
                            setFloatUniform("decay", decay)
                        },
                        "image"
                    )

                RenderEffect.createChainEffect(
                    highlightRenderEffect,
                    blurRenderEffect
                )
            } else {
                null
            }
        }

        private companion object {

            var cachedBlurRenderEffect: RenderEffect? = null
        }
    }

    companion object {

        @Stable
        val Default: Highlight = Highlight()
    }
}
