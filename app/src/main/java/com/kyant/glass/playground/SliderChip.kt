package com.kyant.glass.playground

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kyant.expressa.m3.motion.MotionScheme
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.LocalTextStyle
import com.kyant.expressa.ui.ProvideTextStyle
import com.kyant.expressa.ui.Text

@Composable
fun <T : Comparable<T>> SliderChip(
    value: PreviewState.LiquidGlassParamValue<T>,
    label: String?,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dragDelta: (dragAmount: Offset) -> Float = { it.x }
) {
    val containerColor = surfaceContainer
    val sliderColor = primaryContainer
    val errorColor = errorContainer

    Row(
        modifier
            .clip(CornerShape.full)
            .drawBehind {
                if (value.isValid) {
                    drawRect(containerColor)
                    drawRect(
                        sliderColor,
                        size = size.copy(width = size.width * value.progress)
                    )
                } else {
                    drawRect(errorColor)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val delta = dragDelta(dragAmount) / size.width
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
                if (value.isValid) {
                    Text(
                        value.label,
                        primary
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
