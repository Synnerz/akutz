package com.github.synnerz.akutz.api.objects.gui.components2

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.listeners.MouseListener

open class UIBase @JvmOverloads constructor(
    var _x: Double,
    var _y: Double,
    var _width: Double,
    var _height: Double,
    var parent: UIBase? = null
) {
    private val children = mutableListOf<UIBase>()
    // We need to keep track of the listeners we add, so we can remove them
    // whenever this component gets a parent because the parent should have them
    private val listeners = object {
        var onScroll: ((x: Double, y: Double, delta: Int) -> Unit)? = null
        var onClick: ((x: Double, y: Double, button: Int, pressed: Boolean) -> Unit)? = null
        var onDragged: ((deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) -> Unit)? = null
    }
    private var dirty: Boolean = true
    private val hooks = object {
        var onScroll: ((Double, Double, Int) -> Unit)? = null
        var onClick: ((Double, Double, Int, Boolean) -> Unit)? = null
        var onDragged: ((Double, Double, Double, Double, Int) -> Unit)? = null
    }
    var x: Double = 0.0
        set(value) {
            field = value
            markDirty()
        }
    var y: Double = 0.0
        set(value) {
            field = value
            markDirty()
        }
    var width: Double = 0.0
        set(value) {
            field = value
            markDirty()
        }
    var height: Double = 0.0
        set(value) {
            field = value
            markDirty()
        }
    var bgColor: Color = Color.EMPTY
    var bounds: Boundaries = Boundaries(0.0, 0.0, 0.0, 0.0)

    init {
        parent?.children?.add(this)
    }

    @JvmOverloads
    open fun setDirty(bool: Boolean = true): UIBase = apply {
        dirty = bool
        children.forEach { it.setDirty(bool) }
    }

    open fun markDirty(): UIBase = apply {
        dirty = true
        children.forEach { it.setDirty(true) }
    }

    open fun addChild(child: UIBase) = apply {
        val oldParent = child.parent
        if (oldParent != null) {
            if (oldParent == this) return@apply
            oldParent.removeChild(child)
        }
        children.add(child)
        child.parent = this
        markDirty()
    }

    open fun removeChild(child: UIBase): Boolean {
        val removed = children.remove(child)
        markDirty()
        return removed
    }

    open fun remove(): Boolean = parent?.removeChild(this) ?: false

    open fun clearChildren() = apply {
        children.forEach {
            it.parent = null
            it.markDirty()
        }
        children.clear()
        markDirty()
    }

    open fun update() = apply {
        val parentX = parent?.x ?: 0.0
        val parentY = parent?.y ?: 0.0
        val parentWidth = parent?.width ?: Renderer.sr?.scaledWidth_double ?: 0.0
        val parentHeight = parent?.height ?: Renderer.sr?.scaledHeight_double ?: 0.0

        dirty = false
        x = _x / 100 * parentWidth + parentX
        y = _y / 100 * parentHeight + parentY
        width = _width / 100 * parentWidth
        height = _height / 100 * parentHeight
        bounds = Boundaries(x, y, x + width, y + height)

        if (parent == null && listeners.onClick == null) {
            listeners.onClick = { x, y, button, pressed -> handleClick(x, y, button, pressed) }
            listeners.onScroll = { x, y, delta -> handleScroll(x, y, delta) }
            listeners.onDragged = { deltaX, deltaY, x, y, button -> handleDragged(deltaX, deltaY, x, y, button) }

            MouseListener.onClick(listeners.onClick!!)
            MouseListener.onScroll(listeners.onScroll!!)
            MouseListener.onDragged(listeners.onDragged!!)
        } else if (parent != null) {
            MouseListener.onClickList.remove(listeners.onClick)
            MouseListener.onScrollList.remove(listeners.onScroll)
            MouseListener.onDraggedList.remove(listeners.onDragged)
        }
    }

    data class Boundaries(val x1: Double, val y1: Double, val x2: Double, val y2: Double)

    open fun preDraw() {}

    open fun render() {}

    // TODO: probably make this less doc-like
    open fun drawChild() {
        if (dirty) update()
        children.forEach { it.draw() }
    }

    open fun postDraw() {}

    open fun draw() {
        // TODO: make the mouse calculations in here instead of relying on MouseListener
        // so these don't get triggered whenever the components are not being rendered (as it currently does)
        Renderer.beginDraw(bgColor, false)
        preDraw()
        render()
        drawChild()
        postDraw()
        Renderer.finishDraw()
    }

    open fun setColor(color: Color) = apply {
        bgColor = color
    }

    open fun setChildOf(parent: UIBase) = apply {
        if (parent.hasChild(this)) return@apply
        parent.addChild(this)
    }

    open fun hasChild(child: UIBase): Boolean = children.contains(child)

    // These set the location/size in pixels not percent and does not call markDirty
    open fun setPX(x: Double) = apply { this.x = x }
    open fun setPY(y: Double) = apply { this.y = y }
    open fun setPWidth(width: Double) = apply { this.width = width }
    open fun setPHeight(height: Double) = apply { this.height = height }

    // Mouse events
    open fun handleClick(x: Double, y: Double, button: Int, pressed: Boolean) {
        val ( x1, y1, x2, y2 ) = bounds
        if (x !in x1..x2 || y !in y1..y2) return

        onClick(x, y, button, pressed)
        hooks.onClick?.invoke(x, y, button, pressed)
        // Propagate the click to the children
        children.forEach { it.handleClick(x, y, button, pressed) }
    }
    open fun handleScroll(x: Double, y: Double, delta: Int) {
        val ( x1, y1, x2, y2 ) = bounds
        if (x !in x1..x2 || y !in y1..y2) return

        onScroll(x, y, delta)
        hooks.onScroll?.invoke(x, y, delta)
        // Propagate the scroll to the children
        children.forEach { it.handleScroll(x, y, delta) }
    }
    open fun handleDragged(deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) {
        val ( x1, y1, x2, y2 ) = bounds
        if (x !in x1..x2 || y !in y1..y2) return

        onDragged(deltaX, deltaY, x, y, button)
        hooks.onDragged?.invoke(deltaX, deltaY, x, y, button)
        // Propagate the drag to the children
        children.forEach { it.handleDragged(deltaX, deltaY, x, y, button) }
    }
    open fun onClick(x: Double, y: Double, button: Int, pressed: Boolean) = apply {}
    open fun onClick(cb: (Double, Double, Int, Boolean) -> Unit) = apply {
        hooks.onClick = cb
    }
    open fun onScroll(x: Double, y: Double, delta: Int) = apply {}
    open fun onScroll(cb: (Double, Double, Int) -> Unit) = apply {
        hooks.onScroll = cb
    }
    open fun onDragged(deltaX: Double, deltaY: Double, x: Double, y: Double, button: Int) = apply {}
    open fun onDragged(cb: (Double, Double, Double, Double, Int) -> Unit) = apply {
        hooks.onDragged = cb
    }
}