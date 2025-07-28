@file:OptIn(ExperimentalLuminanceSamplerApi::class)

package com.kyant.liquidglass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import com.kyant.liquidglass.highlight.GlassHighlightElement
import com.kyant.liquidglass.material.GlassBrushElement
import com.kyant.liquidglass.sampler.ExperimentalLuminanceSamplerApi
import com.kyant.liquidglass.sampler.LuminanceSampler
import com.kyant.liquidglass.utils.GlassShaders
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun Modifier.liquidGlass(
    state: LiquidGlassProviderState,
    style: () -> GlassStyle
): Modifier =
    this
        .then(GlassShapeElement(style = style))
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LiquidGlassElement(
                    state = state,
                    style = style,
                    luminanceSampler = null
                ) then GlassBrushElement(
                    style = style
                ) then GlassHighlightElement(
                    style = style
                )
            } else {
                Modifier
            }
        )

fun Modifier.liquidGlass(
    state: LiquidGlassProviderState,
    style: GlassStyle
): Modifier =
    this.liquidGlass(
        state = state,
        luminanceSampler = null,
        style = { style }
    )

@ExperimentalLuminanceSamplerApi
fun Modifier.liquidGlass(
    state: LiquidGlassProviderState,
    luminanceSampler: LuminanceSampler? = null,
    style: () -> GlassStyle
): Modifier =
    this
        .then(GlassShapeElement(style = style))
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LiquidGlassElement(
                    state = state,
                    style = style,
                    luminanceSampler = luminanceSampler
                ) then GlassBrushElement(
                    style = style
                ) then GlassHighlightElement(
                    style = style
                )
            } else {
                Modifier
            }
        )

@ExperimentalLuminanceSamplerApi
fun Modifier.liquidGlass(
    state: LiquidGlassProviderState,
    style: GlassStyle,
    luminanceSampler: LuminanceSampler? = null
): Modifier =
    this.liquidGlass(
        state = state,
        luminanceSampler = luminanceSampler,
        style = { style }
    )

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class LiquidGlassElement(
    val state: LiquidGlassProviderState,
    val style: () -> GlassStyle,
    val luminanceSampler: LuminanceSampler?
) : ModifierNodeElement<LiquidGlassNode>() {

    override fun create(): LiquidGlassNode {
        return LiquidGlassNode(
            state = state,
            style = style,
            luminanceSampler = luminanceSampler
        )
    }

    override fun update(node: LiquidGlassNode) {
        node.update(
            state = state,
            style = style,
            luminanceSampler = luminanceSampler
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "liquidGlass"
        properties["state"] = state
        properties["style"] = style
        properties["luminanceSampler"] = luminanceSampler
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LiquidGlassElement) return false

        if (state != other.state) return false
        if (style != other.style) return false
        if (luminanceSampler != other.luminanceSampler) return false

        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + style.hashCode()
        result = 31 * result + (luminanceSampler?.hashCode() ?: 0)
        return result
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class LiquidGlassNode(
    var state: LiquidGlassProviderState,
    var style: () -> GlassStyle,
    val luminanceSampler: LuminanceSampler?
) : DrawModifierNode, GlobalPositionAwareModifierNode, ObserverModifierNode, Modifier.Node() {

    override val shouldAutoInvalidate: Boolean = false

    private var rect: Rect? by mutableStateOf(null)
    private var graphicsLayer: GraphicsLayer? = null
    private var samplerJob: Job? = null

    private var currentStyle = style()

    private var _colorFilter: ColorFilter? = null
    private var _colorFilterEffect: RenderEffect? = null

    private var _blurRadiusPx: Float = Float.NaN
    private var _blurRenderEffect: RenderEffect? = null

    private val refractionShader = RuntimeShader(GlassShaders.refractionShaderString)
    private var _size: Size = Size.Unspecified
    private var _cornerRadiusPx: Float = Float.NaN
    private var _innerRefractionHeight: Float = Float.NaN
    private var _innerRefractionAmount: Float = Float.NaN
    private var _eccentricFactor: Float = Float.NaN
    private var _innerRefractionRenderEffect: RenderEffect? = null

    private var _renderEffect: androidx.compose.ui.graphics.RenderEffect? = null

    private var _rect: Rect? = null

    override fun ContentDrawScope.draw() {
        val style = currentStyle

        var colorFilterChanged = false
        val colorFilter = style.material.colorFilter
        if (_colorFilter != colorFilter) {
            colorFilterChanged = true
            _colorFilter = colorFilter
            _colorFilterEffect =
                if (colorFilter != null) {
                    RenderEffect.createColorFilterEffect(colorFilter.asAndroidColorFilter())
                } else {
                    null
                }
        }
        val colorFilterEffect = _colorFilterEffect

        val blurRadiusPx = style.material.blurRadius.toPx()
        val blurRadiusChanged = colorFilterChanged || _blurRadiusPx != blurRadiusPx
        if (blurRadiusChanged) {
            _blurRadiusPx = blurRadiusPx
            _blurRenderEffect =
                if (blurRadiusPx > 0f) {
                    if (colorFilterEffect != null) {
                        RenderEffect.createBlurEffect(
                            blurRadiusPx,
                            blurRadiusPx,
                            colorFilterEffect,
                            Shader.TileMode.CLAMP
                        )
                    } else {
                        RenderEffect.createBlurEffect(
                            blurRadiusPx,
                            blurRadiusPx,
                            Shader.TileMode.CLAMP
                        )
                    }
                } else {
                    colorFilterEffect
                }
        }
        val blurRenderEffect = _blurRenderEffect

        var sizeChanged = false
        val size = size
        if (_size != size) {
            sizeChanged = true
            _size = size
        }

        val cornerRadiusPx = style.shape.topStart.toPx(size, this)
        val cornerRadiusChanged = _cornerRadiusPx != cornerRadiusPx
        _cornerRadiusPx = cornerRadiusPx

        val innerRefractionHeight = style.innerRefraction.height.toPx(size, this)
        val innerRefractionAmount = style.innerRefraction.amount.toPx(size, this)
        val eccentricFactor = style.innerRefraction.eccentricFactor
        val innerRefractionChanged =
            sizeChanged || cornerRadiusChanged ||
                    _innerRefractionHeight != innerRefractionHeight ||
                    _innerRefractionAmount != innerRefractionAmount ||
                    _eccentricFactor != eccentricFactor
        if (innerRefractionChanged) {
            _innerRefractionRenderEffect =
                RenderEffect.createRuntimeShaderEffect(
                    refractionShader.apply {
                        setFloatUniform("size", size.width, size.height)
                        setFloatUniform("cornerRadius", cornerRadiusPx)

                        setFloatUniform("refractionHeight", innerRefractionHeight)
                        setFloatUniform("refractionAmount", innerRefractionAmount)
                        setFloatUniform("eccentricFactor", eccentricFactor)
                    },
                    "image"
                )
        }
        val innerRefractionRenderEffect = _innerRefractionRenderEffect!!

        if (blurRadiusChanged || innerRefractionChanged) {
            _renderEffect =
                if (blurRenderEffect != null) {
                    RenderEffect.createChainEffect(
                        innerRefractionRenderEffect,
                        blurRenderEffect
                    )
                } else {
                    innerRefractionRenderEffect
                }.asComposeRenderEffect()
        }

        graphicsLayer?.let { layer ->
            layer.renderEffect = _renderEffect

            val rect = rect!!
            if (innerRefractionChanged || _rect != rect) {
                _rect = rect
                layer.record {
                    translate(-rect.left, -rect.top) {
                        drawLayer(state.graphicsLayer)
                    }
                }
            }

            drawLayer(layer)
        }

        drawContent()
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        rect = state.rect?.let {
            coordinates.boundsInRoot().translate(-it.topLeft)
        }
    }

    override fun onObservedReadsChanged() {
        updateStyle()
    }

    override fun onAttach() {
        val graphicsContext = requireGraphicsContext()
        graphicsLayer =
            graphicsContext.createGraphicsLayer().apply {
                compositingStrategy = androidx.compose.ui.graphics.layer.CompositingStrategy.Offscreen
            }

        if (luminanceSampler != null) {
            samplerJob =
                coroutineScope.launch {
                    while (isActive) {
                        delay(luminanceSampler.sampleIntervalMillis)
                        graphicsLayer?.let { layer ->
                            luminanceSampler.sample(layer)
                        }
                    }
                }
        }

        updateStyle()
    }

    override fun onDetach() {
        val graphicsContext = requireGraphicsContext()
        graphicsLayer?.let { layer ->
            graphicsContext.releaseGraphicsLayer(layer)
            graphicsLayer = null
        }

        samplerJob?.cancel()
        samplerJob = null
    }

    fun update(
        state: LiquidGlassProviderState,
        style: () -> GlassStyle,
        luminanceSampler: LuminanceSampler?
    ) {
        if (this.state != state ||
            this.style != style ||
            this.luminanceSampler != luminanceSampler
        ) {
            this.state = state
            this.style = style
            updateStyle()
        }
    }

    private fun updateStyle() {
        observeReads { currentStyle = style() }
        invalidateDraw()
    }
}
