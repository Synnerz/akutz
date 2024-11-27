package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color

open class Rectangle @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    outline: Boolean? = true,
    var bgColor: Color = Color(255, 255, 255),
    parent: Component? = null
) : Component(_x, _y, _width, _height, outline, parent) {
    override fun preRender() {
        Renderer.color(bgColor)
        Renderer.drawRectangle(x, y, width, height)
        // TODO: outline
    }
}