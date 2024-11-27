package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer

open class Component @JvmOverloads constructor(
    protected var _x: Double,
    protected var _y: Double,
    protected var _w: Double,
    protected var _h: Double,
    protected var p: Component? = null
) {
    protected val c = mutableListOf<Component>()
    protected var d = true

    protected var cx = 0.0
    protected var cy = 0.0
    protected var cw = 0.0
    protected var ch = 0.0

    init {
        p?.addChild(this)
    }

    fun getX() = cx
    fun getY() = cy
    fun getW() = cw
    fun getH() = ch
    fun getWidth() = cw
    fun getHeight() = ch
    fun getP() = p
    fun getParent() = p
    fun setX(x: Double) = apply { mark()._x = x }
    fun setY(y: Double) = apply { mark()._y = y }
    fun setW(w: Double) = apply { mark()._w = w }
    fun setH(h: Double) = apply { mark()._h = h }
    fun setWidth(width: Double) = apply { mark()._w = width }
    fun setHeight(height: Double) = apply { mark()._h = height }

    open fun addChild(child: Component) = apply {
        if (child.p != null) {
            if (child.p == this) return@apply
            child.p!!.removeChild(child)
        }
        c.add(child)
        child.p = this
    }

    open fun removeChild(child: Component): Boolean {
        if (child.p != this) return false
        child.p = null
        return c.remove(child)
    }

    open fun remove(): Boolean = p?.removeChild(this) ?: false

    open fun clearChildren() = apply {
        c.forEach{ it.p = null }
        c.clear()
    }

    protected open fun update() {
        if (!d) return
        d = false
        cx = _x / 100 * (p?.cw ?: Renderer.sr?.scaledWidth_double ?: 0.0)
        cy = _y / 100 * (p?.ch ?: Renderer.sr?.scaledHeight_double ?: 0.0)
        cw = _w / 100 * (p?.cw ?: Renderer.sr?.scaledWidth_double ?: 0.0)
        ch = _h / 100 * (p?.ch ?: Renderer.sr?.scaledHeight_double ?: 0.0)
    }

    protected open fun mark() = apply { d = true }

    protected fun finishRender() {
        Renderer.translate(cx.toFloat(), cy.toFloat())
        update()
        c.forEach { it.render() }
        Renderer.finishDraw()
    }

    protected open fun doRender() {}
    open fun render() {
        doRender()
        finishRender()
    }

    protected var isMouseIn = false
    protected open fun onMouseOver(x: Double, y: Double): Boolean = false
    protected open fun onMouseEnter(x: Double, y: Double): Boolean = false
    protected open fun onMouseLeave(x: Double, y: Double): Boolean = false

    // call when mouse position changes
    protected fun propagateMouseMove(x: Double, y: Double): Boolean {
        val x = x - cx
        val y = y - cy
        var v = c.fold(false) { a, v -> v.propagateMouseMove(x, y) || a }
        val isInside = x in 0.0..cw && y in 0.0..ch
        if (!v) {
            if (isInside) {
                if (!isMouseIn) v = onMouseEnter(x, y)
                v = onMouseOver(x, y) || v
            } else if (isMouseIn) v = onMouseLeave(x, y)
        }
        isMouseIn = isInside
        return v
    }

    protected val buttonState = mutableSetOf<Int>()

    protected open fun onMouseDown(x: Double, y: Double, button: Int): Boolean = false
    protected open fun onMouseUp(x: Double, y: Double, button: Int): Boolean = false
    protected open fun onMouseClick(x: Double, y: Double, button: Int): Boolean = false

    // call when state of a button changes, pass in the changed button
    protected fun propagateMouseButton(x: Double, y: Double, button: Int): Boolean {
        val x = x - cx
        val y = y - cy
        val isDown = button < 0
        val realButton = if (isDown) button.inv() else button
        if (!(x in 0.0..cw && y in 0.0..ch)) {
            buttonState.remove(realButton)
            return false
        }
        val canceled = c.fold(false) { a, v -> a || v.propagateMouseButton(x, y, button) }

        var v = false
        if (isDown) {
            if (realButton !in buttonState) {
                if (!canceled) v = onMouseDown(x, y, realButton)
                buttonState.add(realButton)
            }
        } else {
            if (realButton in buttonState) {
                buttonState.remove(realButton)
                if (!canceled) v = onMouseClick(x, y, realButton)
            }
            if (!canceled) v = onMouseUp(x, y, realButton) || v
        }

        return canceled || v
    }

    protected val dragState = mutableSetOf<Int>()
    protected open fun onDrag(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean = false
    protected open fun onDragStart(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean = false
    protected open fun onDragEnd(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean = false

    // call when is dragged, validates starting mouse position
    protected fun propagateDrag(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean {
        val x0 = x0 - cx
        val y0 = y0 - cy
        if (!(x0 in 0.0..cw && y0 in 0.0..ch)) return false

        var v = c.fold(false) { a, v -> v.propagateDrag(x0, y0, dx, dy, button) || a }
        if (!v) {
            if (button < 0) {
                v = onDragEnd(x0, y0, dx, dy, button.inv())
            } else{
                if (button !in dragState) v = onDragStart(x0, y0, dx, dy, button)
                v = onDrag(x0, y0, dx, dy, button) || v
            }
        }

        if (button < 0) dragState.remove(button.inv())
        else dragState.add(button)

        return v
    }

    protected open fun onDragOver(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean = false

    // call when is dragged, validates current mouse position
    protected fun propagateDragOver(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean {
        val x0 = x0 - cx
        val y0 = y0 - cy
        val x = x0 + dx
        val y = y0 + dy
        if (!(x in 0.0..cw && y in 0.0..ch)) return false
        return c.fold(false) { a, v -> v.propagateDragOver(x0, y0, dx, dy, button) || a } ||
                onDragOver(x0, y0, dx, dy, button)
    }

    protected open fun onScroll(x: Double, y: Double, delta: Int): Boolean = false

    // call when scroll
    protected fun propagateScroll(x: Double, y: Double, delta: Int): Boolean {
        val x = x - cx
        val y = y - cy
        if (!(x in 0.0..cw && y in 0.0..ch)) return false
        return c.fold(false) { a, v -> v.propagateScroll(x, y, delta) || a } ||
                onScroll(x, y, delta)
    }

    open fun resetMouseValues() {
        isMouseIn = false
        buttonState.clear()
    }
}