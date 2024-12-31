package com.github.synnerz.akutz.api.objects.gui.components2

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color

open class UIRect @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override fun render() {
        if (bgColor == Color.EMPTY) return

        Renderer.color(bgColor)

        if (radius == 0.0) {
            Renderer.drawRect(x, y, width, height)
            return
        }

        Renderer.drawRoundRect(x, y, width, height, radius)
    }

    @JvmOverloads
    open fun setColor(r: Double, g: Double, b: Double, a: Double = 255.0) = apply {
        bgColor = Color(
            r.toInt(),
            g.toInt(),
            b.toInt(),
            a.toInt()
        )
    }
}