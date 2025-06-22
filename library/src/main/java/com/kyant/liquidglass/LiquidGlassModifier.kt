package com.kyant.liquidglass

import android.graphics.BlendMode
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlass(
    style: LiquidGlassStyle,
    providerState: LiquidGlassProviderState = LocalLiquidGlassProviderState.current,
    state: LiquidGlassState = rememberLiquidGlassState()
): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = style.shape
            )
            .graphicsLayer {
                clip = true
                shape = style.shape
            }
            .drawWithCache {
                val contentBlurRadiusPx = style.blurRadius.toPx()
                val contentRenderEffect =
                    if (contentBlurRadiusPx > 0f) {
                        RenderEffect.createBlurEffect(
                            contentBlurRadiusPx,
                            contentBlurRadiusPx,
                            Shader.TileMode.DECAL
                        )
                    } else {
                        RenderEffect.createOffsetEffect(0f, 0f)
                    }

                val cornerRadiusPx = style.shape.topStart.toPx(size, this)

                val refractionRenderEffect =
                    RenderEffect.createChainEffect(
                        RenderEffect.createRuntimeShaderEffect(
                            state.refractionShader.apply {
                                setFloatUniform("size", size.width, size.height)
                                setFloatUniform("cornerRadius", cornerRadiusPx)

                                setFloatUniform("refractionHeight", style.refractionHeight.toPx())
                                setFloatUniform("refractionAmount", style.refractionAmount.toPx())
                                setFloatUniform("eccentricFactor", style.eccentricFactor)

                                setFloatUniform("bleedOpacity", style.bleedOpacity)
                            },
                            "image"
                        ),
                        contentRenderEffect
                    )

                val refractionAndBleedRenderEffect =
                    if (style.bleedOpacity > 0f) {
                        val bleedRenderEffect =
                            RenderEffect.createChainEffect(
                                RenderEffect.createRuntimeShaderEffect(
                                    state.bleedShader.apply {
                                        setFloatUniform("size", size.width, size.height)
                                        setFloatUniform("cornerRadius", cornerRadiusPx)

                                        setFloatUniform("eccentricFactor", style.eccentricFactor)
                                        setFloatUniform("bleedAmount", style.bleedAmount.toPx())
                                    },
                                    "image"
                                ),
                                contentRenderEffect
                            )

                        val bleedBlurRadiusPx = style.bleedBlurRadius.toPx()
                        val blurredBleedRenderEffect =
                            if (bleedBlurRadiusPx > 0f) {
                                RenderEffect.createChainEffect(
                                    bleedRenderEffect,
                                    RenderEffect.createBlurEffect(
                                        bleedBlurRadiusPx,
                                        bleedBlurRadiusPx,
                                        Shader.TileMode.CLAMP
                                    )
                                )
                            } else {
                                bleedRenderEffect
                            }

                        RenderEffect.createBlendModeEffect(
                            blurredBleedRenderEffect,
                            refractionRenderEffect,
                            BlendMode.SRC_OVER
                        )
                    } else {
                        refractionRenderEffect
                    }

                val contrast = style.contrast
                val whitePoint = style.whitePoint
                val chromaMultiplier = style.chromaMultiplier
                val renderEffect =
                    if (contrast != 0f || whitePoint != 0f || chromaMultiplier != 1f) {
                        RenderEffect.createChainEffect(
                            RenderEffect.createRuntimeShaderEffect(
                                state.colorManipulationShader.apply {
                                    setFloatUniform("contrast", contrast)
                                    setFloatUniform("whitePoint", whitePoint)
                                    setFloatUniform("chromaMultiplier", chromaMultiplier)
                                },
                                "image"
                            ),
                            refractionAndBleedRenderEffect
                        ).asComposeRenderEffect()
                    } else {
                        refractionAndBleedRenderEffect.asComposeRenderEffect()
                    }

                state.graphicsLayer.renderEffect = renderEffect

                onDrawBehind {
                    val rect = state.rect ?: return@onDrawBehind
                    state.graphicsLayer.record {
                        withTransform({
                            clipRect(0f, 0f, size.width, size.height)
                            translate(-rect.left, -rect.top)
                        }) {
                            drawLayer(providerState.graphicsLayer)
                        }
                    }
                    drawLayer(state.graphicsLayer)
                }
            }
            .onGloballyPositioned { layoutCoordinates ->
                state.rect = providerState.rect?.let {
                    layoutCoordinates.boundsInRoot().translate(-it.topLeft)
                }
            }
    } else {
        this
    }
