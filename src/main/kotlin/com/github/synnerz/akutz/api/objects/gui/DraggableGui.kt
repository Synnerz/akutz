package com.github.synnerz.akutz.api.objects.gui

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.listeners.MouseListener

class DraggableGui @JvmOverloads constructor(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var scale: Double = 1.0
) : Gui() {
    var guiWidth: Double? = null
    var guiHeight: Double? = null

    init {
        onScroll { ls ->
            val dir = ls[2]
            if (dir == 1) scale += 0.02
            else scale -= 0.02
        }

        MouseListener.registerDraggedListener { deltaX, deltaY, _, _, _ ->
            if (!isOpen()) return@registerDraggedListener

            x = (x + deltaX).coerceIn(0.0, getScreenWidth())
            y = (y + deltaY).coerceIn(0.0, getScreenHeight())
        }
    }

    fun setWidth(width: Double) = apply {
        guiWidth = width
    }

    fun setHeight(height: Double) = apply {
        guiHeight = height
    }

    fun setSize(width: Double, height: Double) = apply {
        guiWidth = width
        guiHeight = height
    }

    private fun getScreenWidth(): Double? {
        if (guiWidth == null) return Renderer.sr?.scaledWidth_double

        return Renderer.sr?.scaledWidth_double?.minus(guiWidth!!)
    }

    private fun getScreenHeight(): Double? {
        if (guiHeight == null) return Renderer.sr?.scaledHeight_double

        return Renderer.sr?.scaledHeight_double?.minus(guiHeight!!)
    }

    // TODO: once persistent data api is not shit add auto save and auto load etc
}