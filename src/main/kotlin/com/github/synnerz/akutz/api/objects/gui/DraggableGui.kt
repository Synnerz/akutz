package com.github.synnerz.akutz.api.objects.gui

import com.github.synnerz.akutz.listeners.MouseListener

class DraggableGui @JvmOverloads constructor(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var scale: Double = 1.0
) : Gui() {
    init {
        onScroll { ls ->
            val dir = ls[2]
            if (dir == 1) scale += 0.02
            else scale -= 0.02
        }

        MouseListener.registerDraggedListener { deltaX, deltaY, _, _, _ ->
            if (!isOpen()) return@registerDraggedListener
            x += deltaX
            y += deltaY
        }
    }

    // TODO: once persistent data api is not shit add auto save and auto load etc
}