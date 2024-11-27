package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color

open class RectangleComponent @JvmOverloads constructor(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    p: BaseComponent? = null,
    var bgColor: Color = Color.EMPTY,
    var borderRadius: Double = 0.0,
    var outlineWidth: Double = 0.0,
    var outlineStyle: OutlineStyle = OutlineStyle.NONE,
    var outlineColor: Color = bgColor.asShade(0.5)
) : BaseComponent(x, y, w, h, p) {
    override fun doRender() {
        Renderer.beginDraw(outlineColor)

        when (outlineStyle) {
            OutlineStyle.NONE -> {
                if (borderRadius > 0) Renderer.drawRectangle(cx, cy, cw, ch)
                else Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
            }

            OutlineStyle.OUTER -> {
                if (borderRadius > 0) Renderer.drawRectangle(
                    cx - outlineWidth,
                    cy - outlineWidth,
                    cw + outlineWidth * 2,
                    ch + outlineWidth * 2
                )
                else Renderer.drawRoundRectangle(
                    cx - outlineWidth,
                    cy - outlineWidth,
                    cw + outlineWidth * 2,
                    ch + outlineWidth * 2,
                    borderRadius
                )
                Renderer.color(bgColor)
                if (borderRadius > 0) Renderer.drawRectangle(cx, cy, cw, ch)
                else Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
            }

            OutlineStyle.INNER -> {
                if (borderRadius > 0) Renderer.drawRectangle(cx, cy, cw, ch)
                else Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
                Renderer.color(bgColor)
                if (borderRadius > 0) Renderer.drawRectangle(
                    cx + outlineWidth,
                    cy + outlineWidth,
                    cw - outlineWidth * 2,
                    ch - outlineWidth * 2
                )
                else Renderer.drawRoundRectangle(
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