package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.libs.MathLib
import com.github.synnerz.akutz.api.objects.gui.transition.AnimatedColor
import com.github.synnerz.akutz.api.objects.gui.transition.AnimatedDouble
import com.github.synnerz.akutz.api.objects.render.Color

class UISwitch @JvmOverloads constructor(
    x: Double = 0.0,
    y: Double = 0.0,
    w: Double = 100.0,
    h: Double = 100.0,
    p: Component? = null,
    var knobPosition: AnimatedDouble = AnimatedDouble(0.0),
    var knobOnColor: Color = Color.GREEN,
    var knobOffColor: Color = Color.RED,
    var trailOnColor: Color = knobOnColor.asShade(0.5),
    var trailOffColor: Color = knobOffColor.asShade(0.5),
    var knobOnHoverColor: Color = knobOnColor.asTint(0.8),
    var knobOnClickColor: Color = knobOnColor.asTint(0.4),
    var knobOffHoverColor: Color = knobOffColor.asTint(0.8),
    var knobOffClickColor: Color = knobOffColor.asTint(0.4)
) : UIStateValue<Boolean>(false, x, y, w, h, p) {
    val background = UIRectangle(p = this, bgColor = AnimatedColor(Color.GRAY)).also { it.setPadding(2.0) }
    val trail = UIRectangle(0.0, 5.0, 0.0, 90.0, background, AnimatedColor(trailOffColor))
    val knob = UISwitchKnob(background, knobOffColor, knobOffHoverColor, knobOffClickColor, this)

    init {
        listen(::changeKnob)
    }

    override fun doRender() {
        if (background.getInnerWidth() == 0.0) return
        val pos = knobPosition.get()
        val maxW = background.getInnerWidth() - knob.getOuterWidth()
        val w = MathLib.rescale(pos, 0.0, 1.0, 0.0, 100 * maxW / background.getInnerWidth())
        knob.setX(w)
        trail.setW(w)
    }

    private fun changeKnob(v: Boolean) {
        trail.bgColor.set(if (v) trailOnColor else trailOffColor)

        knob.normalColor.set(if (v) knobOnColor else knobOffColor)
        knob.hoverColor.set(if (v) knobOnHoverColor else knobOffHoverColor)
        knob.clickColor.set(if (v) knobOnClickColor else knobOffClickColor)
        knob.updateColor()
    }

    override fun onMouseClick(x: Double, y: Double, button: Int): Boolean {
        set(!get())
        knobPosition.set(
            MathLib.rescale(
                knob.getOuterX() - background.getInnerX(),
                0.0,
                background.getInnerWidth() - knob.getOuterWidth(),
                0.0,
                1.0
            )
        )
        knobPosition.set(if (get()) 1.0 else 0.0)
        return true
    }
}

class UISwitchKnob(
    p: Component,
    normalColor: Color,
    hoverColor: Color,
    clickColor: Color,
    private val switch: UISwitch
) : UIButton(
    0.0,
    0.0,
    20.0,
    100.0,
    p,
    {},
    AnimatedColor(normalColor),
    AnimatedColor(hoverColor),
    AnimatedColor(clickColor)
) {
    private fun getPos(dx: Double): Double {
        val f = 100 / switch.background.getW()
        val x = ((if (switch.get()) 1.0 else 0.0) + dx * f).coerceIn(0.0..1.0)
        return MathLib.rescale(
            x,
            0.0,
            1.0,
            0.0,
            (switch.background.getW() - switch.knob.getW()) / switch.background.getW()
        )
    }

    override fun onMouseClick(x: Double, y: Double, button: Int): Boolean =
        super.onMouseClick(x, y, button).let { false }
//    override fun onDrag(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean {
//        setX(getPos(dx) * 100)
//        return true
//    }
//
//    override fun onDragEnd(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean {
//        val p = getPos(dx)
//        switch.set(p > 0.5)
//        switch.knobPosition.set(p)
//        switch.knobPosition.set(if (p > 0.5) 1.0 else 0.0)
//        return true
//    }
}