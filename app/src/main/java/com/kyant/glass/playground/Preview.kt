package com.kyant.glass.playground

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.util.fastCoerceAtMost
import com.kyant.expressa.prelude.*
import com.kyant.glass.R

@Composable
fun Preview(state: PreviewState) {
    val graphicsLayer = rememberGraphicsLayer()
    val backgroundColor = surfaceContainerLowest
    val containerSize = LocalWindowInfo.current.containerSize

    var rect by remember { mutableStateOf(Rect.Zero) }

    val painter =
        state.imageBitmap?.let { BitmapPainter(it) }
            ?: painterResource(R.drawable.homescreen)

    Box(
        Modifier.fillMaxSize()
    ) {
        // background
        Image(
            painter, null,
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    graphicsLayer.record {
                        drawRect(backgroundColor)
                        this@drawWithContent.drawContent()
                    }
                },
            contentScale = ContentScale.Crop
        )

        // glass
        Box(
            Modifier
                .graphicsLayer { // transform and clip
                    translationX = state.offset.x
                    translationY = state.offset.y
                    clip = true
                    shape = RoundedCornerShape(state.cornerRadius.value)
                }
                .graphicsLayer { // dispersion effect
                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        RuntimeShader(
                            """// This file belongs to Kyant. You must not use it without permission.
    uniform shader image;
    
    uniform float2 size;
    uniform float cornerRadius;
    uniform float dispersionHeight;
    
    float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
    
    float circleMap(float x) {
        return 1.0 - sqrt(1.0 - x * x);
    }
    
    float2 normalToTangent(float2 normal) {
        return float2(normal.y, -normal.x);
    }
    
    float sdRectangle(float2 coord, float2 halfSize) {
        float2 d = abs(coord) - halfSize;
        float outside = length(max(d, 0.0));
        float inside = min(max(d.x, d.y), 0.0);
        return outside + inside;
    }
    
    float sdRoundedRectangle(float2 coord, float2 halfSize, float cornerRadius) {
        float2 innerHalfSize = halfSize - float2(cornerRadius);
        return sdRectangle(coord, innerHalfSize) - cornerRadius;
    }
    
    const float eps = 0.5;
    const float2 epsX = float2(eps, 0.0);
    const float2 epsY = float2(0.0, eps);
    
    float2 gradSdRoundedRectangle(float2 coord, float2 halfSize, float cornerRadius) {
        float dx = sdRoundedRectangle(coord + epsX, halfSize, cornerRadius)
            - sdRoundedRectangle(coord - epsX, halfSize, cornerRadius);
        float dy = sdRoundedRectangle(coord + epsY, halfSize, cornerRadius)
            - sdRoundedRectangle(coord - epsY, halfSize, cornerRadius);
        return normalize(float2(dx, dy));
    }
    
    half4 main(float2 coord) {
        float2 halfSize = size * 0.5;
        float2 centeredCoord = coord - halfSize;
        float sd = sdRoundedRectangle(centeredCoord, halfSize, cornerRadius);
        
        if (sd < 0.0 && -sd < dispersionHeight) {
            float2 normal = gradSdRoundedRectangle(centeredCoord, halfSize, cornerRadius);
            float2 tangent = normalToTangent(normal);
            
            half4 dispersedColor = half4(0.0);
            half4 weight = half4(0.0);
            
            float dispersionFraction = 1.0 - -sd / dispersionHeight;
            float dispersionWidth = dispersionHeight * 2.0 * pow(circleMap(dispersionFraction), 2.0);
            if (dispersionWidth < 2.0) {
                half4 color = image.eval(coord);
                return color;
            }
            float maxI = min(dispersionWidth, 100.0);
            for (float i = 0.0; i < 100.0; i++) {
                float t = i / maxI;
                if (t > 1.0) break;
                half4 color = image.eval(coord + tangent * float2(t - 0.5) * dispersionWidth);
                if (t >= 0.0 && t < 0.5) {
                    dispersedColor.b += color.b;
                    weight.b += 1.0;
                }
                if (t > 0.25 && t < 0.75) {
                    dispersedColor.g += color.g;
                    weight.g += 1.0;
                }
                if (t > 0.5 && t <= 1.0) {
                    dispersedColor.r += color.r;
                    weight.r += 1.0;
                }
            }
            dispersedColor /= weight;
            
            half4 color = image.eval(coord);
            dispersedColor.a = color.a;
            half4 blendedColor = dispersedColor;
            return blendedColor;
        } else {
            half4 color = image.eval(coord);
            return color;
        }
    }"""
                        ).apply {
                            val cornerRadius =
                                state.cornerRadius.value.toPx()
                                    .fastCoerceAtMost(size.minDimension / 2f)

                            setFloatUniform("size", size.width, size.height)
                            setFloatUniform("cornerRadius", cornerRadius)
                            setFloatUniform("dispersionHeight", state.dispersionHeight.value.toPx())
                        },
                        "image"
                    ).asComposeRenderEffect()
                }
                .graphicsLayer { // chroma boost
                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        RuntimeShader(
                            """// This file belongs to Kyant. You must not use it without permission.
        uniform shader image;
        
        uniform float chromaMultiplier;
        
        const half3 rgbToY = half3(0.299, 0.587, 0.114);
        
        half3 saturateColor(half3 color, float amount) {
            half luma = dot(color, rgbToY);
            return clamp(mix(half3(luma), color, amount), 0.0, 1.0);
        }
        
        half4 main(float2 coord) {
            half4 color = image.eval(coord);
            color.rgb = saturateColor(color.rgb, chromaMultiplier);
            return color;
        }
    """
                        ).apply {
                            setFloatUniform("chromaMultiplier", state.chromaMultiplier.value)
                        },
                        "image"
                    ).asComposeRenderEffect()
                }
                .graphicsLayer { // glass effect
                    renderEffect = RenderEffect.createRuntimeShaderEffect(
                        RuntimeShader(
                            """// This file belongs to Kyant. You must not use it without permission.
    uniform shader image;
    
    uniform float2 size;
    uniform float cornerRadius;
    uniform float refractionHeight;
    uniform float refractionAmount;
    uniform float eccentricFactor;
    
    float circleMap(float x) {
        return 1.0 - sqrt(1.0 - x * x);
    }
    
    float sdRectangle(float2 coord, float2 halfSize) {
        float2 d = abs(coord) - halfSize;
        float outside = length(max(d, 0.0));
        float inside = min(max(d.x, d.y), 0.0);
        return outside + inside;
    }
    
    float sdRoundedRectangle(float2 coord, float2 halfSize, float cornerRadius) {
        float2 innerHalfSize = halfSize - float2(cornerRadius);
        return sdRectangle(coord, innerHalfSize) - cornerRadius;
    }
    
    const float eps = 0.5;
    const float2 epsX = float2(eps, 0.0);
    const float2 epsY = float2(0.0, eps);
    
    float2 gradSdRoundedRectangle(float2 coord, float2 halfSize, float cornerRadius) {
        float dx = sdRoundedRectangle(coord + epsX, halfSize, cornerRadius)
            - sdRoundedRectangle(coord - epsX, halfSize, cornerRadius);
        float dy = sdRoundedRectangle(coord + epsY, halfSize, cornerRadius)
            - sdRoundedRectangle(coord - epsY, halfSize, cornerRadius);
        return normalize(float2(dx, dy));
    }
    
    half4 main(float2 coord) {
        float2 halfSize = size * 0.5;
        float2 centeredCoord = coord - halfSize;
        float sd = sdRoundedRectangle(centeredCoord, halfSize, cornerRadius);
        
        if (sd < 0.0 && -sd < refractionHeight) {
            float2 normal = gradSdRoundedRectangle(centeredCoord, halfSize, cornerRadius * 1.5);
            float refractedDistance = circleMap(1.0 - -sd / refractionHeight) * refractionAmount;
            float2 refractedDirection = normalize(normal + eccentricFactor * normalize(centeredCoord));
            float2 refractedCoord = coord + refractedDistance * refractedDirection;
            if (refractedCoord.x < 0.0 || refractedCoord.x >= size.x ||
                refractedCoord.y < 0.0 || refractedCoord.y >= size.y) {
                return half4(0.0, 0.0, 0.0, 1.0);
            }
            half4 refractedColor = image.eval(refractedCoord);
            return refractedColor;
        } else {
            half4 color = image.eval(coord);
            return color;
        }
    }"""
                        ).apply {
                            val cornerRadius =
                                state.cornerRadius.value.toPx()
                                    .fastCoerceAtMost(size.minDimension / 2f)

                            setFloatUniform("size", size.width, size.height)
                            setFloatUniform("cornerRadius", cornerRadius)
                            setFloatUniform("refractionHeight", state.refractionHeight.value.toPx())
                            setFloatUniform("refractionAmount", state.refractionAmount.value.toPx())
                            setFloatUniform("eccentricFactor", state.eccentricFactor.value)
                        },
                        "image"
                    ).asComposeRenderEffect()
                }
                .graphicsLayer { // blur
                    val blurRadiusPx = state.blurRadius.value.toPx()
                    if (blurRadiusPx > 0f) {
                        renderEffect =
                            RenderEffect.createBlurEffect(
                                blurRadiusPx,
                                blurRadiusPx,
                                Shader.TileMode.DECAL
                            ).asComposeRenderEffect()
                    }
                }
                .drawBehind {
                    translate(-rect.left, -rect.top) {
                        drawLayer(graphicsLayer)
                    }
                    drawRect(
                        Color.Black,
                        alpha = state.opacity.value
                    )
                }
                .onGloballyPositioned { layoutCoordinates ->
                    rect = layoutCoordinates.boundsInParent()
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        state.offset += pan
                        state.size = DpSize(
                            (state.size.width * zoom).coerceIn(
                                state.minSize.width,
                                containerSize.width.toDp()
                            ),
                            (state.size.height * zoom).coerceIn(
                                state.minSize.height,
                                containerSize.height.toDp()
                            )
                        )
                    }
                }
                .layout { measurable, constraints ->
                    val size = state.size.toSize().roundToIntSize()
                    val width = size.width.fastCoerceAtMost(constraints.maxWidth)
                    val height = size.height.fastCoerceAtMost(constraints.maxHeight)
                    val placeable = measurable.measure(Constraints.fixed(width, height))

                    layout(width, height) {
                        placeable.place(0, 0)
                    }
                }
                .align(Alignment.Center)
        )

        BlurredAnimatedContent({ state.displayControls }) { visible ->
            if (visible) {
                PreviewControls(
                    state,
                    { rect }
                )
            }
        }

        BlurredAnimatedContent(
            { state.isInConfigurationMode },
            Modifier
                .padding(16.dp)
                .safeDrawingPadding()
                .align(Alignment.BottomCenter)
        ) { isInConfigurationMode ->
            if (!isInConfigurationMode) {
                ButtonGroup(state)
            } else {
                ConfigurationBottomSheet(state)
            }
        }
    }
}
