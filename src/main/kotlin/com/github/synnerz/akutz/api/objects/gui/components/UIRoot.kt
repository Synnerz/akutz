package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import org.lwjgl.input.Mouse
import kotlin.math.sign

class UIRoot : Component() {
    private val clickPositions = mutableMapOf<Int, Position>()
    private val dragPosition = mutableMapOf<Int, Position>()

    override fun resetMouseValues() {
        super.resetMouseValues()
        clickPositions.clear()
        dragPosition.clear()
    }

    override fun doRender() {
        val x = Mouse.getX() / (Renderer.sr?.scaleFactor ?: 1)
        val y = Mouse.getY() / (Renderer.sr?.scaleFactor ?: 1)
        val xd = Mouse.getX().toDouble() / (Renderer.sr?.scaleFactor ?: 1)
        val yd = Mouse.getY().toDouble() / (Renderer.sr?.scaleFactor ?: 1)
        val scroll = Mouse.getEventDWheel()

        if (scroll != 0) propagateScroll(xd, yd, scroll.sign)
        propagateMouseMove(xd, yd)
        for (i in 0 until Mouse.getButtonCount()) {
            val state = Mouse.isButtonDown(i)
            val has = i in clickPositions

            if (state xor has) propagateMouseButton(xd, yd, if (state) i.inv() else i)
            if (state) {
                if (!has) {
                    clickPositions[i] = Position(x, y)
                    dragPosition[i] = Position(x, y)
                }
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