package com.github.synnerz.akutz.api.objects.gui

import com.github.synnerz.akutz.api.libs.render.Renderer

abstract class BaseGuiComponent @JvmOverloads constructor(
    protected var x: Double,
    protected var y: Double,
    protected var w: Double,
    protected var h: Double,
    protected var p: BaseGuiComponent? = null
) {
    protected val c = mutableListOf<BaseGuiComponent>()
    protected var d = true

    protected var cx = 0.0
    protected var cy = 0.0
    protected var cw = 0.0
    protected var ch = 0.0

    init {
        p?.addChild(this)
    }

    open fun addChild(child: BaseGuiComponent) = apply {
        if (child.p != null) {
            if (child.p == this) return@apply
            child.p!!.removeChild(child)
        }
        c.add(child)
        child.p = this
    }

    open fun removeChild(child: BaseGuiComponent): Boolean {
        if (child.p != this) return false
        child.p = null
        return c.remove(child)
    }

    open fun remove(): Boolean = p?.removeChild(this) ?: false

    protected open fun update() {
        if (!d) return
        d = false
        cx = x / 100 * (p?.cw ?: Renderer.sr?.scaledWidth_double ?: 0.0)
        cy = y / 100 * (p?.ch ?: Renderer.sr?.scaledHeight_double ?: 0.0)
        cw = w / 100 * (p?.cw ?: Renderer.sr?.scaledWidth_double ?: 0.0)
        ch = h / 100 * (p?.ch ?: Renderer.sr?.scaledHeight_double ?: 0.0)
    }

    protected open fun mark() = apply { d = true }

    fun getX() = cx
    fun getY() = cy
    fun getW() = cw
    fun getH() = ch
    fun setX(x: Double) = apply { mark().x = x }
    fun setY(y: Double) = apply { mark().y = y }
    fun setW(w: Double) = apply { mark().w = w }
    fun setH(h: Double) = apply { mark().h = h }

    protected fun finishRender() {
        Renderer.translate(cx.toFloat(), cy.toFloat())
        update()
        c.forEach { it.render() }
        Renderer.finishDraw()
    }

    protected abstract fun doRender()
    open fun render() {
        doRender()
        finishRender()
    }
}