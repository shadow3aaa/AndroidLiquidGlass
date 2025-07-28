package com.kyant.glass.playground

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp
import com.kyant.expressa.m3.motion.MotionScheme
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.LocalTextStyle
import com.kyant.expressa.ui.ProvideTextStyle
import com.kyant.expressa.ui.Text
import com.kyant.liquidglass.GlassStyle
import com.kyant.liquidglass.LiquidGlassProviderState
import com.kyant.liquidglass.liquidGlass
import com.kyant.liquidglass.material.GlassMaterial

@Composable
fun <T : Comparable<T>> SliderChip(
    value: PreviewState.LiquidGlassParamValue<T>,
    label: String?,
    icon: @Composable () -> Unit,
    providerState: LiquidGlassProviderState,
    modifier: Modifier = Modifier,
    dragDelta: (dragAmount: Offset) -> Float = { it.x }
) {
    val layoutDirection = LocalLayoutDirection.current

    val sliderColor = primaryContainer
    val unsafeColors = yellowStaticColors
    val errorColor = errorContainer

    Row(
        modifier
            .liquidGlass(
                providerState,
                GlassStyle(
                    CornerShape.full,
                    material = GlassMaterial(
                        brush = SolidColor(surfaceBright.copy(alpha = 0.6f))
                    )
                )
            )
            .drawBehind {
                if (value.isValid) {
                    drawRect(
                        if (value.isSafe) sliderColor else unsafeColors.accentContainer,
                        topLeft =
                            if (layoutDirection == Ltr) Offset.Zero
                            else Offset(size.width * (1f - value.progress), 0f),
                        size = size.copy(width = size.width * value.progress)
                    )
                } else {
                    drawRect(errorColor)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val delta = dragDelta(dragAmount) / size.width * if (layoutDirection == Ltr) 1f else -1f
                    value.setProgress(value.progress + delta)
                }
            }
            .padding(12.dp, 8.dp)
            .animateContentSize(MotionScheme.fastSpatial()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column {
            if (label != null) {
                Text(
                    label,
                    bodyMedium
                )
            }

            ProvideTextStyle(
                if (label != null) labelMedium
                else labelLarge
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides Ltr
                ) {
                    if (value.isValid) {
                        Text(
                            value.label,
                            if (value.isSafe) primary else unsafeColors.accent
                        )
                    } else {
                        Row {
                            Text(
                                value.unsafeLabel,
                                LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
                                error
                            )
                            Text(" -> ")
                            Text(value.label)
                        }
                    }
                }
            }
        }
    }
}
