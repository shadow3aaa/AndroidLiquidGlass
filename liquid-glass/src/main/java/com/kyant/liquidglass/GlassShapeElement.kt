package com.kyant.liquidglass

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidateLayer
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints

internal class GlassShapeElement(
    val style: () -> GlassStyle
) : ModifierNodeElement<GlassShapeNode>() {

    override fun create(): GlassShapeNode {
        return GlassShapeNode(style)
    }

    override fun update(node: GlassShapeNode) {
        node.update(style)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "glassShape"
        properties["style"] = style
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GlassShapeElement) return false

        if (style != other.style) return false

        return true
    }

    override fun hashCode(): Int {
        return style.hashCode()
    }
}

internal class GlassShapeNode(
    var style: () -> GlassStyle
) : LayoutModifierNode, ObserverModifierNode, Modifier.Node() {

    override val shouldAutoInvalidate: Boolean = false

    private var shape: CornerBasedShape = style().shape
        set(value) {
            if (field != value) {
                field = value
                invalidateLayer()
            }
        }

    private val layerBlock: GraphicsLayerScope.() -> Unit = {
        clip = true
        shape = this@GlassShapeNode.shape
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)

        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0, layerBlock = layerBlock)
        }
    }

    override fun onObservedReadsChanged() {
        updateShape()
    }

    override fun onAttach() {
        updateShape()
    }

    fun update(
        style: () -> GlassStyle
    ) {
        if (this.style != style) {
            this.style = style
            updateShape()
        }
    }

    private fun updateShape() {
        observeReads { shape = style().shape }
    }
}
