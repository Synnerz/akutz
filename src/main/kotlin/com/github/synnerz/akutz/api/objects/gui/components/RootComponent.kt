package com.github.synnerz.akutz.api.objects.gui.components

import org.lwjgl.input.Mouse
import kotlin.math.sign

class RootComponent : BaseComponent(0.0, 0.0, 100.0, 100.0) {
    private val clickPositions = mutableMapOf<Int, Position>()
    private val dragPosition = mutableMapOf<Int, Position>()

    override fun resetMouseValues() {
        super.resetMouseValues()
        clickPositions.clear()
        dragPosition.clear()
    }

    override fun doRender() {
        val x = Mouse.getX()
        val y = Mouse.getY()
        val xd = x.toDouble()
        val yd = y.toDouble()
        val scroll = Mouse.getEventDWheel()

        if (scroll != 0) propagateScroll(xd, yd, scroll.sign)
        propagateMouseMove(xd, yd)
        for (i in 0 until Mouse.getButtonCount()) {
            val state = Mouse.isButtonDown(i)
            val has = i in clickPositions

            if (state xor has) propagateMouseButton(xd, yd, if (state) i.inv() else i)
            if (!has) {
                clickPositions[i] = Position(x, y)
                dragPosition[i] = Position(x, y)
            }
            if (state) {
                if (x - dragPosition[i]!!.x != 0 || y - dragPosition[i]!!.y != 0) {
                    propagateDrag(
                        clickPositions[i]!!.x.toDouble(),
                        clickPositions[i]!!.y.toDouble(),
                        xd - clickPositions[i]!!.x,
                        yd - clickPositions[i]!!.y,
                        i
                    )
                    propagateDragOver(
                        clickPositions[i]!!.x.toDouble(),
                        clickPositions[i]!!.y.toDouble(),
                        xd - clickPositions[i]!!.x,
                        yd - clickPositions[i]!!.y,
                        i
                    )
                    dragPosition[i]!!.x = x
                    dragPosition[i]!!.y = y
                }
            } else {
                if (i in dragPosition) {
                    propagateDrag(
                        clickPositions[i]!!.x.toDouble(),
                        clickPositions[i]!!.y.toDouble(),
                        xd - clickPositions[i]!!.x,
                        yd - clickPositions[i]!!.y,
                        i.inv()
                    )
                    dragPosition.remove(i)
                }
                clickPositions.remove(i)
            }
        }
    }

    private data class Position(var x: Int, var y: Int)
}