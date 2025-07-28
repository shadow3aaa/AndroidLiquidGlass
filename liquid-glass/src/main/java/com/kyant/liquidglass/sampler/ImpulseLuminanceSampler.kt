package com.kyant.liquidglass.sampler

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalLuminanceSamplerApi
class ImpulseLuminanceSampler(
    initialLuminance: Float = 0.5f,
    override val sampleIntervalMillis: Long = 300L,
    val precision: Float = 0.25f,
    val scaledSize: IntSize = IntSize(5, 5)
) : LuminanceSampler {

    private var lastSampleTimeMillis = Long.MIN_VALUE

    override var luminance: Float by mutableFloatStateOf(initialLuminance)
        private set

    @Suppress("UseKtx")
    override suspend fun sample(graphicsLayer: GraphicsLayer): Float {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastSampleTimeMillis < sampleIntervalMillis &&
            lastSampleTimeMillis != Long.MIN_VALUE
        ) {
            return luminance
        }
        lastSampleTimeMillis = currentTimeMillis

        val scaledWidth = scaledSize.width
        val scaledHeight = scaledSize.height

        if (!graphicsLayer.isReleased && graphicsLayer.size.width > 0 && graphicsLayer.size.height > 0) {
            withContext(Dispatchers.IO) {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                val scaledBitmap =
                    Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
                        .copy(Bitmap.Config.ARGB_8888, false)

                var sumLuminance = 0f
                val totalPixels = scaledWidth * scaledHeight

                for (y in 0 until scaledHeight) {
                    for (x in 0 until scaledWidth) {
                        val pixel = scaledBitmap.getPixel(x, y)
                        val red = (pixel shr 16) and 0xFF
                        val green = (pixel shr 8) and 0xFF
                        val blue = pixel and 0xFF

                        val pixelLuminance = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f
                        sumLuminance += pixelLuminance
                    }
                }

                val newLuminance =
                    ((sumLuminance / totalPixels / precision).fastRoundToInt() * precision).fastCoerceIn(0f, 1f)
                if (!newLuminance.isNaN()) {
                    withContext(Dispatchers.Main) {
                        luminance = newLuminance
                    }
                }

                scaledBitmap.recycle()
                bitmap.recycle()
            }
        }

        return luminance
    }
}
