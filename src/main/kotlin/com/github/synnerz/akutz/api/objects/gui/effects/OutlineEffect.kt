package com.github.synnerz.akutz.api.objects.gui.effects

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.gui.components2.UIBase
import com.github.synnerz.akutz.api.objects.render.Color

open class OutlineEffect(
    var width: Double = 1.0,
    var color: Color = Color.EMPTY,
    var radius: Double = 0.0,
    comp: UIBase
) : Effect(comp) {
    override fun preDraw() {
        if (color == Color.EMPTY || width == 0.0) return

        Renderer.color(color)
        if (radius == 0.0) {
            Renderer.drawRect(
                comp.x - width,
                comp.y - width,
                comp.width + width * 2,
                comp.height + width * 2
            )
            return
        }

        Renderer.drawRoundRect(
            comp.x - width,
            comp.y - width,
            comp.width + width * 2,
            comp.height + width * 2,
            radius,
            lineWidth = width
        )
    }

    override fun postDraw() {
        Renderer.color(Color.EMPTY)
    }
}