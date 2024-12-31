package com.github.synnerz.akutz.api.objects.gui.components2

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.objects.gui.effects.Effect
import com.github.synnerz.akutz.api.objects.gui.effects.OutlineEffect
import com.github.synnerz.akutz.api.objects.gui.effects.ScissorEffect
import com.github.synnerz.akutz.api.objects.render.Color

open class UIRect @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var radius: Double = 0.0,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    var effect: Effect? = null

    override fun preDraw() {
        if (effect == null) return
        effect?.preDraw()
    }

    override fun render() {
        if (bgColor == Color.EMPTY) return

        Renderer.color(bgColor)

        if (radius == 0.0) {
            Renderer.drawRect(x, y, width, height)
            return
        }

        Renderer.drawRoundRect(x, y, width, height, radius)
    }

    override fun postDraw() {
        if (effect == null) return
        effect?.postDraw()
    }

    @JvmOverloads
    open fun setColor(r: Double, g: Double, b: Double, a: Double = 255.0) = apply {
        bgColor = Color(
            r.toInt(),
            g.toInt(),
            b.toInt(),
            a.toInt()
        )
    }

    @JvmOverloads
    open fun addOutlineEffect(width: Double, color: Color, radius: Double = 0.0) = apply {
        effect = OutlineEffect(width, color, radius, this)
    }

    @JvmOverloads
    open fun addScissorEffect(comp: UIBase? = null) = apply {
        val c = comp ?: parent
        effect = ScissorEffect(c!!)
    }

    open fun addEffect(effect: Effect) = apply {
        this.effect = effect
    }
}