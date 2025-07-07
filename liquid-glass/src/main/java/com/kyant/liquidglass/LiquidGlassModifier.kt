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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.ceil
import kotlin.math.min

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
                compositingStrategy = CompositingStrategy.Offscreen
                clip = true
                shape = style.shape
            }
            .drawWithCache {
                val contentBlurRadiusPx = style.material.blurRadius.toPx()
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

                val hasBleed = style.bleed.opacity > 0f
                val refractionRenderEffect =
                    RenderEffect.createChainEffect(
                        RenderEffect.createRuntimeShaderEffect(
                            shadersCache.getRefractionShader(hasBleed).apply {
                                setFloatUniform("size", size.width, size.height)
                                setFloatUniform("cornerRadius", cornerRadiusPx)

                                setFloatUniform(
                                    "refractionHeight",
                                    style.innerRefraction.height.toPx(this@drawWithCache, size)
                                )
                                setFloatUniform(
                                    "refractionAmount",
                                    style.innerRefraction.amount.toPx(this@drawWithCache, size)
                                )
                                setFloatUniform(
                                    "eccentricFactor",
                                    style.innerRefraction.eccentricFactor
                                )

                                if (hasBleed) {
                                    setFloatUniform(
                                        "bleedOpacity",
                                        style.bleed.opacity
                                    )
                                }
                            },
                            "image"
                        ),
                        contentRenderEffect
                    )

                val refractionWithBleedRenderEffect =
                    if (hasBleed) {
                        val bleedRenderEffect =
                            RenderEffect.createChainEffect(
                                RenderEffect.createRuntimeShaderEffect(
                                    shadersCache.getBleedShader().apply {
                                        setFloatUniform("size", size.width, size.height)
                                        setFloatUniform("cornerRadius", cornerRadiusPx)

                                        setFloatUniform(
                                            "eccentricFactor",
                                            style.innerRefraction.eccentricFactor
                                        )
                                        setFloatUniform(
                                            "bleedAmount",
                                            style.bleed.amount.toPx(this@drawWithCache, size)
                                        )
                                    },
                                    "image"
                                ),
                                contentRenderEffect
                            )

                        val bleedBlurRadiusPx = style.bleed.blurRadius.toPx()
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

                val materialRenderEffect =
                    if (style.material != GlassMaterial.Default) {
                        RenderEffect.createRuntimeShaderEffect(
                            shadersCache.getMaterialShader().apply {
                                setFloatUniform(
                                    "contrast",
                                    style.material.contrast
                                )
                                setFloatUniform(
                                    "whitePoint",
                                    style.material.whitePoint
                                )
                                setFloatUniform(
                                    "chromaMultiplier",
                                    style.material.chromaMultiplier
                                )
                            },
                            "image"
                        )
                    } else {
                        null
                    }

                val renderEffect =
                    if (materialRenderEffect != null) {
                        RenderEffect.createChainEffect(
                            materialRenderEffect,
                            refractionWithBleedRenderEffect
                        ).asComposeRenderEffect()
                    } else {
                        refractionWithBleedRenderEffect.asComposeRenderEffect()
                    }

                val graphicsLayer = obtainGraphicsLayer()
                graphicsLayer.renderEffect = renderEffect

                val strokeWidthPx = ceil(min(style.border.width.toPx(), size.minDimension / 2))
                val halfStroke = strokeWidthPx / 2
                val borderTopLeft = Offset(halfStroke, halfStroke)
                val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                val border =
                    if (strokeWidthPx > 0f) {
                        val borderBrush = style.border.createBrush(this, borderSize, cornerRadiusPx)
                        if (borderBrush != null) {
                            val borderOutline = style.shape.createOutline(borderSize, layoutDirection, this)
                            Pair(borderOutline, borderBrush)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                val stroke = Stroke(strokeWidthPx)

                onDrawBehind {
                    val rect = rect ?: return@onDrawBehind
                    graphicsLayer.record {
                        translate(-rect.left, -rect.top) {
                            drawLayer(providerState.graphicsLayer)
                        }
                    }

                    clipRect(0f, 0f, size.width, size.height) {
                        drawLayer(graphicsLayer)
                    }

                    if (style.material.tint.isSpecified) {
                        drawRect(style.material.tint)
                    }

                    if (border != null) {
                        val (borderOutline, borderBrush) = border

                        translate(borderTopLeft.x, borderTopLeft.y) {
                            drawOutline(
                                outline = borderOutline,
                                brush = borderBrush,
                                style = stroke,
                                blendMode = BlendMode.Plus
                            )
                        }
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
