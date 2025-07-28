package com.kyant.glass.playground

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kyant.expressa.components.iconbutton.IconButton
import com.kyant.expressa.components.iconbutton.IconButtonColors
import com.kyant.expressa.components.iconbutton.IconButtonSizes
import com.kyant.expressa.m3.shape.CornerShape
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.Icon
import com.kyant.glass.R
import com.kyant.liquidglass.GlassStyle
import com.kyant.liquidglass.LiquidGlassProviderState
import com.kyant.liquidglass.liquidGlass
import com.kyant.liquidglass.material.GlassMaterial
import kotlinx.coroutines.launch

@Composable
fun PreviewToolbar(
    state: PreviewState,
    providerState: LiquidGlassProviderState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val iconButtonGlassStyle = remember {
        GlassStyle(
            CornerShape.full,
            material = GlassMaterial(
                brush = SolidColor(Color.Black.copy(alpha = 0.3f))
            )
        )
    }

    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            { scope.launch { state.reset() } },
            Modifier.liquidGlass(providerState, iconButtonGlassStyle),
            sizes = IconButtonSizes.medium,
            colors = IconButtonColors.tonal(
                containerColor = primary.copy(alpha = 0.6f),
                contentColor = onPrimary
            )
        ) {
            Icon(
                painterResource(R.drawable.reset_settings_24px),
                stringResource(R.string.reset),
            )
        }

        Box(Modifier.weight(1f)) {
            Row(
                modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    { state.displayControls = !state.displayControls },
                    Modifier.liquidGlass(providerState, iconButtonGlassStyle),
                    sizes = IconButtonSizes.medium,
                    colors = IconButtonColors.tonal(
                        containerColor = Color.Transparent,
                        contentColor = if (state.displayControls) Color.Black else Color.White
                    )
                ) {
                    Icon(
                        painterResource(
                            if (state.displayControls) R.drawable.visibility_24px
                            else R.drawable.visibility_off_24px
                        ),
                        stringResource(
                            if (state.displayControls) R.string.hide_controls
                            else R.string.show_controls
                        )
                    )
                }
                IconButton(
                    { state.configurationMode = ConfigurationMode.Colors },
                    Modifier.liquidGlass(providerState, iconButtonGlassStyle),
                    sizes = IconButtonSizes.medium,
                    colors = IconButtonColors.tonal(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.colors_24px),
                        stringResource(R.string.colors)
                    )
                }
                IconButton(
                    { state.configurationMode = ConfigurationMode.Advanced },
                    Modifier.liquidGlass(providerState, iconButtonGlassStyle),
                    sizes = IconButtonSizes.medium,
                    colors = IconButtonColors.tonal(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.build_24px),
                        stringResource(R.string.advanced_settings)
                    )
                }
            }
        }

        val context = LocalContext.current
        val pickMedia = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                state.loadImage(context, uri)
            }
        }
        IconButton(
            {
                pickMedia.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            Modifier.liquidGlass(providerState, iconButtonGlassStyle),
            sizes = IconButtonSizes.medium,
            colors = IconButtonColors.tonal(
                containerColor = primary.copy(alpha = 0.6f),
                contentColor = onPrimary
            )
        ) {
            Icon(
                painterResource(R.drawable.add_photo_alternate_24px),
                stringResource(R.string.pick_an_image)
            )
        }
    }
}
