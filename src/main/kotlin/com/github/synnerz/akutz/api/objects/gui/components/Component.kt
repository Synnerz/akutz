package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer

abstract class Component @JvmOverloads constructor(
    private var _x: Double,
    private var _y: Double,
    private var _width: Double,
    private var _height: Double,
    var outline: Boolean? = true,
    var parent: Component? = null
) {
    private val children = mutableListOf<Component>()
    private var dirty = false
    private val listeners = null // TODO: finish this later
    var x: Double = 0.0
    var y: Double = 0.0
    var width: Double = 0.0
    var height: Double = 0.0
    // TODO: outline

    init {
        parent?.addChild(this)
    }

    open fun addChild(comp: Component) = apply {
        if (comp.parent !== this) comp.parent = this
        else {
            comp.parent!!.removeChild(comp)
            comp.parent = this
        }
        children.add(comp)
    }

    open fun removeChild(comp: Component): Boolean {
        if (comp.parent == null) return false

        comp.parent = null
        return children.remove(comp)
    }

    open fun markDirty() = apply {
        dirty = true
    }

    open fun onResize() = markDirty()

    open fun update() {
        if (!dirty) return

        val swidth = getScreenWidth()
        val sheight = getScreenHeight()

        x = getPercentPixels(_x, parent?.width ?: swidth) + (parent?.x ?: 0.0)
        y = getPercentPixels(_y, parent?.height ?: sheight) + (parent?.y ?: 0.0)
        width = getPercentPixels(_width, parent?.width ?: swidth)
        height = getPercentPixels(_height, parent?.height ?: sheight)
        // TODO: outline
        dirty = false
    }

    abstract fun preRender()

    open fun postRender() {
        Renderer.translate(x.toFloat(), y.toFloat())
        update()
        children.forEach { it.render() }
        Renderer.finishDraw()
    }

    open fun render() {
        preRender()
        postRender()
    }

    private fun getPercentPixels(percent: Double, total: Double): Double {
        return (percent / 100) * total
    }

    private fun getScreenWidth(): Double = Renderer.sr?.scaledWidth_double ?: 0.0
    private fun getScreenHeight(): Double = Renderer.sr?.scaledHeight_double ?: 0.0
}