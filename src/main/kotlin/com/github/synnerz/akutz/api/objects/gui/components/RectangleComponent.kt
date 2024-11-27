package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.gui.transition.AnimatedColor
import com.github.synnerz.akutz.api.objects.render.Color

open class RectangleComponent @JvmOverloads constructor(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    p: BaseComponent? = null,
    var bgColor: AnimatedColor = AnimatedColor(Color.EMPTY),
    var borderRadius: Double = 0.0,
    var outlineWidth: Double = 0.0,
    var outlineStyle: OutlineStyle = OutlineStyle.NONE,
    var outlineColor: Color = bgColor.get().asShade(0.5)
) : BaseComponent(x, y, w, h, p) {
    override fun doRender() {
        Renderer.beginDraw(if (outlineStyle == OutlineStyle.NONE) bgColor.get() else outlineColor)

        when (outlineStyle) {
            OutlineStyle.NONE -> {
                Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
            }

            OutlineStyle.OUTER -> {
                Renderer.drawRoundRectangle(
                    cx - outlineWidth,
                    cy - outlineWidth,
                    cw + outlineWidth * 2,
                    ch + outlineWidth * 2,
                    borderRadius
                )
                Renderer.color(bgColor.get())
                Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
            }

            OutlineStyle.INNER -> {
                Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
                Renderer.color(bgColor.get())
                Renderer.drawRoundRectangle(
                    cx + outlineWidth,
                    cy + outlineWidth,
                    cw - outlineWidth * 2,
                    ch - outlineWidth * 2,
                    borderRadius
                )
            }
        }
    }

    enum class OutlineStyle {
        OUTER,
        INNER,
        NONE;

        companion object {
            @JvmStatic
            fun getByName(name: String) =
                OutlineStyle.entries.find { it.name == name.uppercase() }
                    ?: throw IllegalArgumentException("Unknown parameter: $name")
        }
    }
}