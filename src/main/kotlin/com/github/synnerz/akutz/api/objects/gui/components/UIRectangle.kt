package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.gui.transition.AnimatedColor
import com.github.synnerz.akutz.api.objects.render.Color

open class UIRectangle @JvmOverloads constructor(
    p: Component? = null,
    x: Double = 0.0,
    y: Double = 0.0,
    w: Double = 100.0,
    h: Double = 100.0,
    var bgColor: AnimatedColor = AnimatedColor(Color.EMPTY),
    var borderRadius: Double = 0.0,
    var outlineWidth: Double = 0.0,
    var outlineStyle: OutlineStyle = OutlineStyle.NONE,
    var outlineColor: Color = bgColor.get().asShade(0.5)
) : Component(p, x, y, w, h) {
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