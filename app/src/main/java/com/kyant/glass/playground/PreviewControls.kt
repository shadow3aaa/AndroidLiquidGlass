package com.kyant.glass.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.Icon
import com.kyant.glass.R
import com.kyant.liquidglass.LiquidGlassProviderState
import kotlin.math.sqrt

@Composable
fun PreviewControls(
    state: PreviewState,
    providerState: LiquidGlassProviderState,
    rect: () -> Rect,
    modifier: Modifier = Modifier
) {
    val layoutDirection = LocalLayoutDirection.current

    BoxWithConstraints(modifier.fillMaxSize()) {
        val containerSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())

        // width
        Icon(
            painterResource(R.drawable.height_24px),
            stringResource(R.string.width),
            onTertiaryContainer,
            Modifier
                .graphicsLayer {
                    val rect = rect()
                    translationX =
                        if (layoutDirection == Ltr) rect.right + 8.dp.toPx()
                        else -containerSize.width + rect.left - 8.dp.toPx()
                    translationY = rect.center.y - size.height / 2
                }
                .border(
                    2.dp,
                    tertiary,
                    CornerShape.full
                )
                .clip(CornerShape.full)
                .background(tertiaryContainer)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val delta = dragAmount.x * 2f * if (layoutDirection == Ltr) 1f else -1f
                        state.size =
                            state.size.copy(
                                width =
                                    (state.size.width + delta.toDp())
                                        .coerceAtLeast(state.minSize.width)
                            )
                    }
                }
                .padding(12.dp)
                .size(24.dp)
                .graphicsLayer {
                    rotationZ = 90f
                }
        )

        // height
        Icon(
            painterResource(R.drawable.height_24px),
            stringResource(R.string.height),
            onTertiaryContainer,
            Modifier
                .graphicsLayer {
                    val rect = rect()
                    translationX =
                        if (layoutDirection == Ltr) rect.center.x - size.width / 2
                        else -containerSize.width + rect.center.x + size.width / 2
                    translationY = rect.bottom + 8.dp.toPx()
                }
                .border(
                    2.dp,
                    tertiary,
                    CornerShape.full
                )
                .clip(CornerShape.full)
                .background(tertiaryContainer)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val delta = dragAmount.y * 2
                        state.size =
                            state.size.copy(
                                height =
                                    (state.size.height + delta.toDp())
                                        .coerceAtLeast(state.minSize.height)
                            )
                    }
                }
                .padding(12.dp)
                .size(24.dp)
        )

        // corner radius
        SliderChip(
            state.cornerRadius,
            null,
            {
                Icon(
                    painterResource(R.drawable.rounded_corner_24px),
                    stringResource(R.string.corner_radius),
                    Modifier.graphicsLayer {
                        rotationX = 180f
                    }
                )
            },
            providerState,
            Modifier
                .graphicsLayer {
                    val rect = rect()
                    val cornerRadiusOffset =
                        (-state.cornerRadius.value.toPx() + 8.dp.toPx()) / (2 * sqrt(2f))
                    translationX =
                        if (layoutDirection == Ltr) rect.right + cornerRadiusOffset
                        else -containerSize.width + rect.left - cornerRadiusOffset
                    translationY = rect.bottom + cornerRadiusOffset
                }
                .border(
                    2.dp,
                    primary,
                    CornerShape.full
                )
                .requiredWidth(IntrinsicSize.Max),
            dragDelta = { dragAmount ->
                -((dragAmount.x * if (layoutDirection == Ltr) 1f else -1f) + dragAmount.y) / sqrt(2f)
            }
        )

        // refraction
        Column(
            Modifier
                .width(IntrinsicSize.Max)
                .graphicsLayer {
                    val rect = rect()
                    translationX =
                        if (layoutDirection == Ltr) rect.center.x - size.width / 2
                        else -containerSize.width + rect.center.x + size.width / 2
                    translationY = rect.top - size.height - 8.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SliderChip(
                state.bleedAmount,
                stringResource(R.string.bleed_amount),
                { Icon(painterResource(R.drawable.center_focus_weak_24px)) },
                providerState,
                Modifier.fillMaxWidth()
            )
            SliderChip(
                state.bleedOpacity,
                stringResource(R.string.bleed_opacity),
                { Icon(painterResource(R.drawable.opacity_24px)) },
                providerState,
                Modifier.fillMaxWidth()
            )
            SliderChip(
                state.refractionAmount,
                stringResource(R.string.refraction_amount),
                { Icon(painterResource(R.drawable.center_focus_weak_24px)) },
                providerState,
                Modifier.fillMaxWidth()
            )
            SliderChip(
                state.refractionHeight,
                stringResource(R.string.refraction_height),
                { Icon(painterResource(R.drawable.water_lux_24px)) },
                providerState,
                Modifier.fillMaxWidth()
            )
        }
    }
}
