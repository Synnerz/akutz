package com.github.synnerz.akutz.api.objects.gui

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color

open class GuiComponentBackground @JvmOverloads constructor(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    p: BaseGuiComponent? = null,
    var bgColor: Color = Color.EMPTY,
    var borderRadius: Double = 0.0,
    var outlineWidth: Double = 0.0,
    var outlineStyle: OutlineStyle = OutlineStyle.OUTER,
    var outlineColor: Color = bgColor.asShade(0.5)
) : BaseGuiComponent(x, y, w, h, p) {
    override fun doRender() {
        Renderer.beginDraw(if (outlineWidth > 0) outlineColor else bgColor)

        if (outlineWidth > 0) {
            val x = when (outlineStyle) {
                OutlineStyle.OUTER -> cx - outlineWidth
                OutlineStyle.INNER -> cx + outlineWidth
            }
            val y = when (outlineStyle) {
                OutlineStyle.OUTER -> cy - outlineWidth
                OutlineStyle.INNER -> cy + outlineWidth
            }
            val w = when (outlineStyle) {
                OutlineStyle.OUTER -> cw + outlineWidth * 2
                OutlineStyle.INNER -> cw - outlineWidth * 2
            }
            val h = when (outlineStyle) {
                OutlineStyle.OUTER -> ch + outlineWidth * 2
                OutlineStyle.INNER -> ch - outlineWidth * 2
            }

            if (borderRadius > 0) Renderer.drawRectangle(x, y, w, h)
            else Renderer.drawRoundRectangle(x, y, w, h, borderRadius)

            Renderer.color(bgColor)
        }

        if (borderRadius > 0) Renderer.drawRectangle(cx, cy, cw, ch)
        else Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
    }

    enum class OutlineStyle {
        OUTER,
        INNER;

        companion object {
            @JvmStatic
            fun getByName(name: String) =
                OutlineStyle.entries.find { it.name == name.uppercase() }
                    ?: throw IllegalArgumentException("Unknown parameter: $name")
        }
    }
}