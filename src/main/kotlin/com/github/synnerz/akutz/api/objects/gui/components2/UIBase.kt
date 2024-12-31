package com.github.synnerz.akutz.api.objects.gui.components2

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color

open class UIBase @JvmOverloads constructor(
    var _x: Double,
    var _y: Double,
    var _width: Double,
    var _height: Double,
    var parent: UIBase? = null
) {
    private val children = mutableListOf<UIBase>()
    private var dirty: Boolean = true
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
    }

    open fun preRender() {}

    open fun postRender() {
        if (dirty) update()
        children.forEach { it.render() }
    }

    open fun render() {
        Renderer.beginDraw(bgColor, false)
        preRender()
        postRender()
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
}