package com.github.synnerz.akutz.api.objects.render

import com.github.synnerz.akutz.api.libs.render.Renderer
import java.awt.Graphics2D

class DisplayLine(private val isBuffered: Boolean) {
    private val text: Text = if (isBuffered) BufferedText() else NormalText()
    private var x: Int = 0
    private var y: Int = 0
    private var scale: Float = 1f
    private var shadow: Boolean = false
    private var resolution: Float = 24f

    fun getText() = text
    fun getString() = text.getText()
    fun setString(s: String) = apply { text.setText(s) }
    fun getX() = x
    fun getY() = y
    fun getScale() = scale
    fun setScale(s: Float) = apply { scale = s }
    fun getShadow() = shadow
    fun setShadow(s: Boolean) = apply {
        shadow = s
        if (isBuffered) (text as BufferedText).setShadow(s)
    }

    fun getResolution() = resolution
    fun setResolution(r: Float) = apply { resolution = r }

    fun getWidth() = if (isBuffered) text.getWidth() * scale * 10 / resolution else text.getWidth() * scale
    fun getHeight() = if (isBuffered) text.getHeight() * scale * 10 / resolution else text.getHeight() * scale
    fun getVisibleWidth() = if (isBuffered) text.getVisibleWidth() * scale * 10 / resolution else text.getVisibleWidth() * scale
    fun getVisibleHeight() = if (isBuffered) text.getVisibleHeight() * scale * 10 / resolution else text.getVisibleHeight() * scale

    fun update() {
        if (isBuffered) (text as BufferedText).update()
    }

    fun render(x: Float, y: Float) {
        this.x = x.toInt()
        this.y = y.toInt()
        Renderer.drawString(getString(), x, y, shadow)
    }

    fun render(x: Int, y: Int, graphics: Graphics2D) {
        this.x = x
        this.y = y
        val t = text as BufferedText
        t.render(graphics, x, y)
    }
}