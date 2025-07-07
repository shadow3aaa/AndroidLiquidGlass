package com.kyant.glass.playground

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kyant.expressa.components.button.Button
import com.kyant.expressa.components.button.ButtonColors
import com.kyant.expressa.components.iconbutton.IconButton
import com.kyant.expressa.components.iconbutton.IconButtonColors
import com.kyant.expressa.m3.LocalColorScheme
import com.kyant.expressa.m3.motion.MotionScheme
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.Icon
import com.kyant.expressa.ui.Text
import com.kyant.glass.R
import com.kyant.liquidglass.GlassMaterial
import com.kyant.liquidglass.InnerRefraction
import com.kyant.liquidglass.LiquidGlassStyle
import com.kyant.liquidglass.RefractionValue
import com.kyant.liquidglass.liquidGlass

@Composable
fun ConfigurationBottomSheet(
    state: PreviewState,
    modifier: Modifier = Modifier
) {
    // val providerState = rememberLiquidGlassProviderState()
    CompositionLocalProvider(
        // LocalLiquidGlassProviderState provides providerState
    ) {
        val isDark = LocalColorScheme.current.isDark
        Column(
            modifier
                // .liquidGlassProvider(providerState)
                .liquidGlass(
                    LiquidGlassStyle(
                        shape = CornerShape.extraLarge,
                        innerRefraction = InnerRefraction(
                            height = RefractionValue(24.dp),
                            amount = RefractionValue((-64).dp)
                        ),
                        material = GlassMaterial(
                            blurRadius = 8.dp,
                            tint = surface.copy(alpha = 0.6f),
                            whitePoint = if (isDark) -0.25f else 0.25f,
                            chromaMultiplier = 1.5f
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures {}
                }
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when (state.configurationMode) {
                        ConfigurationMode.Colors -> stringResource(R.string.colors)
                        ConfigurationMode.Advanced -> stringResource(R.string.advanced_settings)
                        null -> ""
                    },
                    titleMedium,
                    Modifier.weight(1f)
                )

                IconButton(
                    { state.configurationMode = null },
                    Modifier.liquidGlass(LiquidGlassStyle(CornerShape.full)),
                    colors = IconButtonColors.tonal(
                        containerColor = primaryContainer.copy(alpha = 0.85f)
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.close_24px),
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
                    ConfigurationMode.Colors -> {
                        SliderChip(
                            state.blurRadius,
                            stringResource(R.string.blur_radius),
                            { Icon(painterResource(R.drawable.lens_blur_24px)) },
                            Modifier.fillMaxWidth()
                        )
                        SliderChip(
                            state.contrast,
                            stringResource(R.string.contrast),
                            { Icon(painterResource(R.drawable.contrast_24px)) },
                            Modifier.fillMaxWidth()
                        )
                        SliderChip(
                            state.whitePoint,
                            stringResource(R.string.white_point),
                            { Icon(painterResource(R.drawable.wb_sunny_24px)) },
                            Modifier.fillMaxWidth()
                        )
                        SliderChip(
                            state.chromaMultiplier,
                            stringResource(R.string.chroma_multiplier),
                            { Icon(painterResource(R.drawable.palette_24px)) },
                            Modifier.fillMaxWidth()
                        )
                    }

                    ConfigurationMode.Advanced -> {
                        Button(
                            { state.unsafeMode = !state.unsafeMode },
                            Modifier.liquidGlass(LiquidGlassStyle(CornerShape.full)),
                            colors = ButtonColors.filled(
                                containerColor = primary.copy(alpha = 0.85f)
                            )
                        ) {
                            Text(
                                if (state.unsafeMode) stringResource(R.string.disable_unsafe_mode)
                                else stringResource(R.string.enable_unsafe_mode),
                                Modifier.animateContentSize(MotionScheme.fastSpatial())
                            )
                        }
                        SliderChip(
                            state.bleedBlurRadius,
                            stringResource(R.string.bleed_blur_radius),
                            { Icon(painterResource(R.drawable.lens_blur_24px)) },
                            Modifier.fillMaxWidth()
                        )
                        SliderChip(
                            state.eccentricFactor,
                            stringResource(R.string.eccentric_factor),
                            { Icon(painterResource(R.drawable.all_out_24px)) },
                            Modifier.fillMaxWidth()
                        )
                        SliderChip(
                            state.dispersionHeight,
                            stringResource(R.string.dispersion_height),
                            { Icon(painterResource(R.drawable.star_shine_24px)) },
                            Modifier.fillMaxWidth()
                        )
                    }

                    null -> {}
                }
            }
        }
    }
}
