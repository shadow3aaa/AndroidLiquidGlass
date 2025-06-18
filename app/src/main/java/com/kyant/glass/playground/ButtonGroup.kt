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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kyant.expressa.components.iconbutton.IconButton
import com.kyant.expressa.components.iconbutton.IconButtonColors
import com.kyant.expressa.components.iconbutton.IconButtonSizes
import com.kyant.expressa.prelude.*
import com.kyant.expressa.ui.Icon
import com.kyant.glass.R
import kotlinx.coroutines.launch

@Composable
fun ButtonGroup(
    state: PreviewState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            { scope.launch { state.reset() } },
            sizes = IconButtonSizes.medium,
            colors = IconButtonColors.tonal(
                containerColor = tertiaryContainer,
                contentColor = onTertiaryContainer
            )
        ) {
            Icon(
                rememberVectorPainter(Icons.Outlined.Restore),
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
                    sizes = IconButtonSizes.medium,
                    colors =
                        if (state.displayControls) IconButtonColors.filled()
                        else IconButtonColors.tonal()
                ) {
                    Icon(
                        rememberVectorPainter(
                            if (state.displayControls) Icons.Outlined.Visibility
                            else Icons.Outlined.VisibilityOff
                        ),
                        stringResource(
                            if (state.displayControls) R.string.hide_controls
                            else R.string.show_controls
                        )
                    )
                }
                IconButton(
                    { state.configurationMode = ConfigurationMode.Color },
                    sizes = IconButtonSizes.medium,
                    colors = IconButtonColors.tonal()
                ) {
                    Icon(
                        rememberVectorPainter(Icons.Outlined.Palette),
                        stringResource(R.string.color)
                    )
                }
                IconButton(
                    { state.configurationMode = ConfigurationMode.Advanced },
                    sizes = IconButtonSizes.medium,
                    colors = IconButtonColors.tonal(
                        containerColor = primaryContainer,
                        contentColor = onPrimaryContainer
                    )
                ) {
                    Icon(
                        rememberVectorPainter(Icons.Outlined.Build),
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
            sizes = IconButtonSizes.medium
        ) {
            Icon(
                rememberVectorPainter(Icons.Outlined.Photo),
                stringResource(R.string.pick_an_image)
            )
        }
    }
}
