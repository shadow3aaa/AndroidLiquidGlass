package com.kyant.glass.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*

@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    var maxExpandedHeight by remember { mutableFloatStateOf(0f) }
    val peekHeight = with(density) { 44.dp.toPx() }
    var offset by rememberSaveable { mutableFloatStateOf(with(density) { -256.dp.toPx() }) }

    Box(
        modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Layout(
            {
                Box(
                    Modifier
                        .draggable(
                            rememberDraggableState { delta ->
                                offset = (offset + delta).fastCoerceIn(-maxExpandedHeight, -peekHeight)
                            },
                            Orientation.Vertical
                        )
                        .padding(vertical = 12.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .clip(CornerShape.full)
                            .background(primary)
                            .size(48.dp, 6.dp)
                    )
                }

                Box(
                    Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = CornerShape.extraLarge,
                            ambientColor = Color.Black.copy(alpha = 0.3f),
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .background(surface)
                ) {
                    content()
                }
            }
        ) { measurables, constraints ->
            val dragHandlePlaceable = measurables[0].measure(constraints)
            val dragHandleHeight = dragHandlePlaceable.height

            val sheetHeight = (peekHeight - offset).fastRoundToInt()
            val sheetPlaceable = measurables[1].measure(
                constraints.copy(
                    minHeight = sheetHeight,
                    maxHeight = sheetHeight
                )
            )

            maxExpandedHeight = constraints.maxHeight - peekHeight - dragHandleHeight

            layout(constraints.maxWidth, constraints.maxHeight) {
                dragHandlePlaceable.place(0, constraints.maxHeight - sheetHeight - dragHandleHeight)
                sheetPlaceable.place(0, constraints.maxHeight - sheetHeight)
            }
        }
    }
}
