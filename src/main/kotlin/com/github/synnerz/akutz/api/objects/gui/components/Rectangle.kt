package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color

open class Rectangle @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var bgColor: Color = Color(255, 255, 255),
    parent: Component? = null
) : Component(_x, _y, _width, _height, parent) {

    override fun preRender() {
        Renderer.color(bgColor)
        Renderer.drawRectangle(x, y, width, height)
    }

    open fun setOutline(width: Double, color: Color? = null) {
        if (color == null) {
            super.setOutline(width, outlineStyle, Color.fromAWTColor(bgColor.asAWTColor().brighter()))
            return
        }

        super.setOutline(width, outlineStyle, color)
    }
}