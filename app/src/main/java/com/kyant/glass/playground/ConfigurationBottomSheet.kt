package com.kyant.glass.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kyant.expressa.components.iconbutton.IconButton
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.Icon
import com.kyant.expressa.ui.Text
import com.kyant.glass.R

@Composable
fun ConfigurationBottomSheet(
    state: PreviewState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .shadow(
                elevation = 8.dp,
                shape = CornerShape.extraLarge,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(surface)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when (state.configurationMode) {
                    ConfigurationMode.Color -> stringResource(R.string.color)
                    ConfigurationMode.Advanced -> stringResource(R.string.advanced_settings)
                    null -> ""
                },
                titleMedium,
                Modifier.weight(1f)
            )

            IconButton(
                { state.configurationMode = null }
            ) {
                Icon(
                    rememberVectorPainter(Icons.Outlined.Close),
                    stringResource(R.string.close)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val retainedConfigurationMode = remember {
                Snapshot.withoutReadObservation { state.configurationMode }
            }
            when (retainedConfigurationMode) {
                ConfigurationMode.Color -> {
                    SliderChip(
                        state.blurRadius,
                        stringResource(R.string.blur_radius),
                        { Icon(rememberVectorPainter(Icons.Outlined.BlurOn)) },
                        Modifier.fillMaxWidth()
                    )
                    SliderChip(
                        state.opacity,
                        stringResource(R.string.opacity),
                        { Icon(rememberVectorPainter(Icons.Outlined.Opacity)) },
                        Modifier.fillMaxWidth()
                    )
                    SliderChip(
                        state.chromaMultiplier,
                        stringResource(R.string.chroma_multiplier),
                        { Icon(rememberVectorPainter(Icons.Outlined.ColorLens)) },
                        Modifier.fillMaxWidth()
                    )
                }

                ConfigurationMode.Advanced -> {
                    SliderChip(
                        state.eccentricFactor,
                        stringResource(R.string.eccentric_factor),
                        { Icon(rememberVectorPainter(Icons.Outlined.Radar)) },
                        Modifier.fillMaxWidth()
                    )
                }

                null -> {}
            }
        }
    }
}
