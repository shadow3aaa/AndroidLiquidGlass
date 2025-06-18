package com.kyant.glass.playground

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateTo
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.expressa.m3.motion.MotionScheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class PreviewState {

    var configurationMode: ConfigurationMode? by mutableStateOf(null)
    val isInConfigurationMode: Boolean by derivedStateOf {
        configurationMode != null
    }

    var displayControls: Boolean by mutableStateOf(true)

    var imageBitmap: ImageBitmap? by mutableStateOf(null)

    val minSize: DpSize = DpSize(24.dp, 24.dp)
    var size: DpSize by mutableStateOf(DpSize(200.dp, 200.dp))
    var offset: Offset by mutableStateOf(Offset.Zero)

    val blurRadius: LiquidGlassParamValue<Dp> =
        LiquidGlassParamValue(
            initialValue = 0.dp,
            valueRange = { 0.dp..24.dp },
            typeConverter = Dp.VectorConverter,
            valueLabel = { "${it.value.fastRoundToInt()} dp" }
        )
    val opacity: LiquidGlassParamValue<Float> =
        LiquidGlassParamValue(
            initialValue = 0f,
            valueRange = { 0f..0.2f },
            typeConverter = Float.VectorConverter,
            valueLabel = { "${(it * 100).fastRoundToInt()}%" }
        )
    val chromaMultiplier: LiquidGlassParamValue<Float> =
        LiquidGlassParamValue(
            initialValue = 1f,
            valueRange = { 0.5f..2f },
            typeConverter = Float.VectorConverter,
            valueLabel = { "%.2f".format(it) }
        )

    val cornerRadius: LiquidGlassParamValue<Dp> =
        LiquidGlassParamValue(
            initialValue = 32.dp,
            valueRange = { 0.dp..min(size.width, size.height) / 2 },
            typeConverter = Dp.VectorConverter,
            valueLabel = { "${it.value.fastRoundToInt()} dp" }
        )
    val refractionHeight: LiquidGlassParamValue<Dp> =
        LiquidGlassParamValue(
            initialValue = 24.dp,
            valueRange = { 0.dp..cornerRadius.value },
            typeConverter = Dp.VectorConverter,
            valueLabel = { "${it.value.fastRoundToInt()} dp" }
        )
    val refractionAmount: LiquidGlassParamValue<Dp> =
        LiquidGlassParamValue(
            initialValue = (-32).dp,
            valueRange = { (-512).dp..0.dp },
            typeConverter = Dp.VectorConverter,
            valueLabel = { "${it.value.fastRoundToInt()} dp" }
        )
    val dispersionHeight: LiquidGlassParamValue<Dp> =
        LiquidGlassParamValue(
            initialValue = 0.dp,
            valueRange = { 0.dp..cornerRadius.value },
            typeConverter = Dp.VectorConverter,
            valueLabel = { "${it.value.fastRoundToInt()} dp" }
        )
    val eccentricFactor: LiquidGlassParamValue<Float> =
        LiquidGlassParamValue(
            initialValue = 1f,
            valueRange = { 0f..1f },
            typeConverter = Float.VectorConverter,
            valueLabel = { "%.2f".format(it) }
        )

    fun loadImage(context: Context, uri: Uri) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageBitmap = bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun reset() {
        imageBitmap = null
        coroutineScope {
            launch {
                AnimationState(
                    TwoWayConverter(
                        convertToVector = { AnimationVector2D(it.width.value, it.height.value) },
                        convertFromVector = { DpSize(it.v1.dp, it.v2.dp) },
                    ), size
                )
                    .animateTo(DpSize(200.dp, 200.dp), MotionScheme.slowSpatial()) {
                        size = value
                    }
            }
            launch {
                AnimationState(Offset.VectorConverter, offset)
                    .animateTo(Offset.Zero, MotionScheme.slowSpatial()) {
                        offset = value
                    }
            }

            launch { blurRadius.reset() }
            launch { opacity.reset() }
            launch { chromaMultiplier.reset() }

            launch { cornerRadius.reset() }
            launch { refractionHeight.reset() }
            launch { refractionAmount.reset() }
            launch { dispersionHeight.reset() }
            launch { eccentricFactor.reset() }
        }
    }

    inner class LiquidGlassParamValue<T : Comparable<T>>(
        val initialValue: T,
        private val valueRange: () -> ClosedRange<T>,
        private val typeConverter: TwoWayConverter<T, AnimationVector1D>,
        private val valueLabel: (T) -> String
    ) {

        private var state: T by mutableStateOf(initialValue)

        val value: T by derivedStateOf {
            state.coerceIn(valueRange())
        }

        val unsafeValue: T
            get() = state

        val isValid: Boolean by derivedStateOf {
            state in valueRange()
        }

        val progress: Float by derivedStateOf {
            val range = valueRange()
            if (range.isEmpty()) {
                0f
            } else {
                val current = typeConverter.convertToVector(value).value
                val start = typeConverter.convertToVector(range.start).value
                val end = typeConverter.convertToVector(range.endInclusive).value
                if (start == end) {
                    0f
                } else {
                    (current - start) / (end - start)
                }
            }
        }

        val label: String by derivedStateOf {
            valueLabel(value)
        }

        val unsafeLabel: String by derivedStateOf {
            valueLabel(unsafeValue)
        }

        fun setValue(value: T) {
            state = value.coerceIn(valueRange())
        }

        fun setFloatValue(value: Float) {
            setValue(typeConverter.convertFromVector(AnimationVector1D(value)))
        }

        fun setProgress(progress: Float) {
            val range = valueRange()
            val start = typeConverter.convertToVector(range.start).value
            val end = typeConverter.convertToVector(range.endInclusive).value
            setFloatValue(lerp(start, end, progress))
        }

        suspend fun reset() {
            AnimationState(typeConverter, state)
                .animateTo(initialValue, MotionScheme.slowSpatial()) {
                    setValue(value)
                }
        }
    }
}
