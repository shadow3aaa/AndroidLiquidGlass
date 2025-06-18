package com.kyant.glass.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Lens
import androidx.compose.material.icons.outlined.RoundedCorner
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Window
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.Icon
import com.kyant.glass.R
import kotlin.math.sqrt

@Composable
fun PreviewControls(
    state: PreviewState,
    rect: () -> Rect,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        // width
        Icon(
            rememberVectorPainter(Icons.Outlined.Height),
            stringResource(R.string.width),
            onTertiaryContainer,
            Modifier
                .graphicsLayer {
                    val rect = rect()
                    translationX = rect.right + 8.dp.toPx()
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
                        val delta = dragAmount.x * 2
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
            rememberVectorPainter(Icons.Outlined.Height),
            stringResource(R.string.height),
            onTertiaryContainer,
            Modifier
                .graphicsLayer {
                    val rect = rect()
                    translationX = rect.center.x - size.width / 2
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
                    rememberVectorPainter(Icons.Outlined.RoundedCorner),
                    stringResource(R.string.corner_radius),
                    Modifier.graphicsLayer {
                        rotationX = 180f
                    }
                )
            },
            Modifier
                .graphicsLayer {
                    val rect = rect()
                    val cornerRadiusOffset =
                        (-state.cornerRadius.value.toPx() + 8.dp.toPx()) / (2 * sqrt(2f))
                    translationX = rect.right + cornerRadiusOffset
                    translationY = rect.bottom + cornerRadiusOffset
                }
                .border(
                    2.dp,
                    primary,
                    CornerShape.full
                ),
            dragDelta = { dragAmount -> (-dragAmount.x - dragAmount.y) / sqrt(2f) }
        )

        // refraction
        Column(
            Modifier
                .width(IntrinsicSize.Max)
                .graphicsLayer {
                    val rect = rect()
                    translationX = rect.center.x - size.width / 2
                    translationY = rect.top - size.height - 8.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SliderChip(
                state.dispersionHeight,
                stringResource(R.string.dispersion_height),
                { Icon(rememberVectorPainter(Icons.Outlined.Lens)) },
                Modifier.fillMaxWidth()
            )
            SliderChip(
                state.refractionAmount,
                stringResource(R.string.refraction_amount),
                { Icon(rememberVectorPainter(Icons.Outlined.WaterDrop)) },
                Modifier.fillMaxWidth()
            )
            SliderChip(
                state.refractionHeight,
                stringResource(R.string.refraction_height),
                { Icon(rememberVectorPainter(Icons.Outlined.Window)) },
                Modifier.fillMaxWidth()
            )
        }
    }
}
