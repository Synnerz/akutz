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
    var outlineWidth: Double = 0.0
) : BaseGuiComponent(x, y, w, h, p) {
    override fun render() {
        Renderer.beginDraw(if (outlineWidth > 0) bgColor.asShade(0.5) else bgColor)
        if (borderRadius > 0) {
            Renderer.drawRectangle(cx, cy, cw, ch)
            if (outlineWidth > 0) {
                Renderer.color(bgColor)
                Renderer.drawRectangle(
                    cx + outlineWidth,
                    cy + outlineWidth,
                    cw - outlineWidth * 2,
                    ch - outlineWidth * 2
                )
            }
        } else {
            Renderer.drawRoundRectangle(cx, cy, cw, ch, borderRadius)
            if (outlineWidth > 0) {
                Renderer.color(bgColor)
                Renderer.drawRoundRectangle(
                    cx + outlineWidth,
                    cy + outlineWidth,
                    cw - outlineWidth * 2,
                    ch - outlineWidth * 2,
                    borderRadius
                )
            }
        }
        finishRender()
    }
}