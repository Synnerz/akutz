package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Display

open class TextComponent @JvmOverloads constructor(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    p: BaseComponent? = null,
    var display: Display = Display()
) : RectangleComponent(x, y, w, h, p) {
    init {
        display.setHorzAlign(Display.HorzAlign.CENTER)
        display.setVertAlign(Display.VertAlign.CENTER)
        display.setScale(100f)
    }
    override fun doRender() {
        super.doRender()
        Renderer.beginDraw()
        display.setX(cx + cw / 2)
        display.setY(cy + ch / 2)
        display.setMaxWidth(cw * 0.8)
        display.setMaxHeight(ch * 0.8)
        display.render()
    }
}