package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.render.Color

abstract class Component @JvmOverloads constructor(
    private var _x: Double,
    private var _y: Double,
    private var _width: Double,
    private var _height: Double,
    var parent: Component? = null
) {
    protected val children = mutableListOf<Component>()
    protected var dirty = true
    protected val listeners = null // TODO: finish this later
    var x: Double = 0.0
    var y: Double = 0.0
    var width: Double = 0.0
    var height: Double = 0.0
    // Outline
    protected var outlineWidth = 0.5
    protected var outlineColor: Color? = null
    protected var outlineStyle: OutlineStyle = OutlineStyle.OUTTER

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
        dirty = false
    }

    fun setOutline(width: Double, style: OutlineStyle = OutlineStyle.OUTTER, color: Color) = apply {
        outlineWidth = width
        outlineStyle = style
        outlineColor = color
    }

    fun setOutline(width: Double, style: String, color: Color) = apply {
        outlineWidth = width
        outlineStyle = OutlineStyle.getByName(style)
        outlineColor = color
    }

    fun setOutline(width: Double, style: OutlineStyle) = apply {
        outlineWidth = width
        outlineStyle = style
    }

    fun setOutline(width: Double, style: String) = apply {
        outlineWidth = width
        outlineStyle = OutlineStyle.getByName(style)
    }

    abstract fun preRender()

    open fun postRender() {
        if (outlineWidth != 0.0 && outlineColor != null && outlineColor?.a != 0) {
            Renderer.color(outlineColor!!)
            if (outlineStyle == OutlineStyle.OUTTER) {
                Renderer.drawRectangle(
                    x - outlineWidth,
                    y - outlineWidth,
                    width + outlineWidth * 2,
                    height + outlineWidth * 2,
                    false
                )
            } else if (outlineStyle == OutlineStyle.INNER) {
                Renderer.drawRectangle(
                    x + outlineWidth,
                    y + outlineWidth,
                    width - outlineWidth * 2,
                    height - outlineWidth * 2,
                    false
                )
            }
        }
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

    enum class OutlineStyle {
        OUTTER,
        INNER;

        companion object {
            @JvmStatic
            fun getByName(name: String) =
                OutlineStyle.entries.find { it.name == name.uppercase() } ?: throw IllegalArgumentException("Unknown parameter: $name")
        }
    }
}