package com.kyant.liquidglass.refraction

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp

/**
 * The inner refraction effect of the liquid glass.
 *
 * @param height
 * The height of the refraction effect.
 * This value is used to determine how far the refraction effect extends from the edge of the glass.
 * It is a **positive** value.
 *
 * @param amount
 * The amount of refraction applied to the content.
 * It is a **negative** value that determines how much the content is distorted.
 * To create a full refraction effect, use `RefractionValue.Full`.
 *
 * @param eccentricFactor
 * The shift factor for the refraction effect.
 * 0 means no shift, 1 will shift the content towards the corners.
 */
@Immutable
data class InnerRefraction(
    override val height: RefractionHeight,
    override val amount: RefractionAmount,
    @param:FloatRange(from = 0.0, to = 1.0) val eccentricFactor: Float = 1f
) : Refraction {

    companion object {

        @Stable
        val Default: InnerRefraction =
            InnerRefraction(
                height = RefractionHeight(8.dp),
                amount = RefractionAmount((-16).dp),
                eccentricFactor = 1f
            )
    }
}
