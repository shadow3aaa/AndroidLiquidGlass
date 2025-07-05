package com.kyant.liquidglass

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun Modifier.liquidGlass(
    style: LiquidGlassStyle,
    providerState: LiquidGlassProviderState = LocalLiquidGlassProviderState.current
): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shadersCache = remember { LiquidGlassShadersCache() }
        var rect: Rect? by remember { mutableStateOf(null) }

        this
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
                            shadersCache.refractionShader.apply {
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
                                    shadersCache.bleedShader.apply {
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
                            android.graphics.BlendMode.SRC_OVER
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
                                shadersCache.colorManipulationShader.apply {
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

                val graphicsLayer = obtainGraphicsLayer()
                graphicsLayer.renderEffect = renderEffect

                val outline = style.shape.createOutline(size, layoutDirection, this)
                val borderWidthPx = style.borderWidth.toPx()
                val borderBrush =
                    if (borderWidthPx > 0f) {
                        GlassLightBorderBrush(
                            color = style.borderColor,
                            cornerRadius = cornerRadiusPx,
                            borderWidth = borderWidthPx
                        )
                    } else {
                        null
                    }

                onDrawBehind {
                    val rect = rect ?: return@onDrawBehind
                    graphicsLayer.record {
                        translate(-rect.left, -rect.top) {
                            drawLayer(providerState.graphicsLayer)
                        }
                    }

                    drawLayer(graphicsLayer)
                    borderBrush?.let {
                        drawOutline(
                            outline = outline,
                            brush = it,
                            style = Stroke(borderWidthPx),
                            blendMode = BlendMode.Plus
                        )
                    }
                }
            }
            .onGloballyPositioned { layoutCoordinates ->
                rect = providerState.rect?.let {
                    layoutCoordinates.boundsInRoot().translate(-it.topLeft)
                }
            }
    } else {
        this
    }
