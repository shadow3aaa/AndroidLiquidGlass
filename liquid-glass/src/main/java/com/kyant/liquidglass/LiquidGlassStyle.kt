package com.kyant.liquidglass

import androidx.annotation.FloatRange
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("FunctionName")
fun LiquidGlassStyle(
    shape: CornerBasedShape,
    blurRadius: Dp = 2.dp,
    borderWidth: Dp = 3.dp,
    borderColor: Color = Color.White,

    refractionHeight: Dp = 8.dp,
    refractionAmount: Dp = (-16).dp,
    @FloatRange(from = 0.0, to = 1.0) eccentricFactor: Float = 1f,

    bleedAmount: Dp = 0.dp,
    bleedBlurRadius: Dp = 0.dp,
    @FloatRange(from = 0.0, to = 1.0) bleedOpacity: Float = 0f,

    @FloatRange(from = -1.0, to = 1.0) contrast: Float = 0f,
    @FloatRange(from = -1.0, to = 1.0) whitePoint: Float = 0f,
    @FloatRange(from = 0.5, to = 2.0) chromaMultiplier: Float = 1f
): ImmutableLiquidGlassStyle =
    ImmutableLiquidGlassStyle(
        shape = shape,
        blurRadius = blurRadius,
        borderWidth = borderWidth,
        borderColor = borderColor,
        refractionHeight = refractionHeight,
        refractionAmount = refractionAmount,
        eccentricFactor = eccentricFactor,
        bleedAmount = bleedAmount,
        bleedBlurRadius = bleedBlurRadius,
        bleedOpacity = bleedOpacity,
        contrast = contrast,
        whitePoint = whitePoint,
        chromaMultiplier = chromaMultiplier
    )

fun MutableLiquidGlassState(
    shape: CornerBasedShape,
    blurRadius: Dp = 2.dp,
    borderWidth: Dp = 3.dp,
    borderColor: Color = Color.White,

    refractionHeight: Dp = 8.dp,
    refractionAmount: Dp = (-16).dp,
    @FloatRange(from = 0.0, to = 1.0) eccentricFactor: Float = 1f,

    bleedAmount: Dp = 0.dp,
    bleedBlurRadius: Dp = 0.dp,
    @FloatRange(from = 0.0, to = 1.0) bleedOpacity: Float = 0f,

    @FloatRange(from = -1.0, to = 1.0) contrast: Float = 0f,
    @FloatRange(from = -1.0, to = 1.0) whitePoint: Float = 0f,
    @FloatRange(from = 0.5, to = 2.0) chromaMultiplier: Float = 1f
): MutableLiquidGlassState =
    MutableLiquidGlassState(
        initialStyle = ImmutableLiquidGlassStyle(
            shape = shape,
            blurRadius = blurRadius,
            borderWidth = borderWidth,
            borderColor = borderColor,
            refractionHeight = refractionHeight,
            refractionAmount = refractionAmount,
            eccentricFactor = eccentricFactor,
            bleedAmount = bleedAmount,
            bleedBlurRadius = bleedBlurRadius,
            bleedOpacity = bleedOpacity,
            contrast = contrast,
            whitePoint = whitePoint,
            chromaMultiplier = chromaMultiplier
        )
    )

sealed interface LiquidGlassStyle {

    val shape: CornerBasedShape

    val blurRadius: Dp

    val borderWidth: Dp

    val borderColor: Color

    val refractionHeight: Dp

    val refractionAmount: Dp

    @get:FloatRange(from = 0.0, to = 1.0)
    val eccentricFactor: Float

    val bleedAmount: Dp

    val bleedBlurRadius: Dp

    @get:FloatRange(from = 0.0, to = 1.0)
    val bleedOpacity: Float

    @get:FloatRange(from = -1.0, to = 1.0)
    val contrast: Float

    @get:FloatRange(from = -1.0, to = 1.0)
    val whitePoint: Float

    @get:FloatRange(from = 0.5, to = 2.0)
    val chromaMultiplier: Float
}

class ImmutableLiquidGlassStyle
internal constructor(
    override val shape: CornerBasedShape,
    override val blurRadius: Dp = 2.dp,
    override val borderWidth: Dp = 3.dp,
    override val borderColor: Color = Color.White,

    override val refractionHeight: Dp = 8.dp,
    override val refractionAmount: Dp = (-16).dp,
    @param:FloatRange(from = 0.0, to = 1.0) override val eccentricFactor: Float = 1f,

    override val bleedAmount: Dp = 0.dp,
    override val bleedBlurRadius: Dp = 0.dp,
    @param:FloatRange(from = 0.0, to = 1.0) override val bleedOpacity: Float = 0f,

    @param:FloatRange(from = -1.0, to = 1.0) override val contrast: Float = 0f,
    @param:FloatRange(from = -1.0, to = 1.0) override val whitePoint: Float = 0f,
    @param:FloatRange(from = 0.5, to = 2.0) override val chromaMultiplier: Float = 1f
) : LiquidGlassStyle {

    fun copy(
        shape: CornerBasedShape = this.shape,
        blurRadius: Dp = this.blurRadius,
        borderWidth: Dp = this.borderWidth,
        borderColor: Color = this.borderColor,

        refractionHeight: Dp = this.refractionHeight,
        refractionAmount: Dp = this.refractionAmount,
        @FloatRange(from = 0.0, to = 1.0) eccentricFactor: Float = this.eccentricFactor,

        bleedAmount: Dp = this.bleedAmount,
        bleedBlurRadius: Dp = this.bleedBlurRadius,
        @FloatRange(from = 0.0, to = 1.0) bleedOpacity: Float = this.bleedOpacity,

        @FloatRange(from = -1.0, to = 1.0) contrast: Float = this.contrast,
        @FloatRange(from = -1.0, to = 1.0) whitePoint: Float = this.whitePoint,
        @FloatRange(from = 0.5, to = 2.0) chromaMultiplier: Float = this.chromaMultiplier
    ): ImmutableLiquidGlassStyle =
        ImmutableLiquidGlassStyle(
            shape = shape,
            blurRadius = blurRadius,
            borderWidth = borderWidth,
            borderColor = borderColor,
            refractionHeight = refractionHeight,
            refractionAmount = refractionAmount,
            eccentricFactor = eccentricFactor,
            bleedAmount = bleedAmount,
            bleedBlurRadius = bleedBlurRadius,
            bleedOpacity = bleedOpacity,
            contrast = contrast,
            whitePoint = whitePoint,
            chromaMultiplier = chromaMultiplier
        )
}

class MutableLiquidGlassState
internal constructor(
    val initialStyle: LiquidGlassStyle
) : LiquidGlassStyle {

    override var shape: CornerBasedShape by mutableStateOf(initialStyle.shape)
    override var blurRadius: Dp by mutableStateOf(initialStyle.blurRadius)
    override var borderWidth: Dp by mutableStateOf(initialStyle.borderWidth)
    override var borderColor: Color by mutableStateOf(initialStyle.borderColor)

    override var refractionHeight: Dp by mutableStateOf(initialStyle.refractionHeight)
    override var refractionAmount: Dp by mutableStateOf(initialStyle.refractionAmount)
    override var eccentricFactor: Float by mutableFloatStateOf(initialStyle.eccentricFactor)

    override var bleedAmount: Dp by mutableStateOf(initialStyle.bleedAmount)
    override var bleedBlurRadius: Dp by mutableStateOf(initialStyle.bleedBlurRadius)
    override var bleedOpacity: Float by mutableFloatStateOf(initialStyle.bleedOpacity)

    override var contrast: Float by mutableFloatStateOf(initialStyle.contrast)
    override var whitePoint: Float by mutableFloatStateOf(initialStyle.whitePoint)
    override var chromaMultiplier: Float by mutableFloatStateOf(initialStyle.chromaMultiplier)
}
