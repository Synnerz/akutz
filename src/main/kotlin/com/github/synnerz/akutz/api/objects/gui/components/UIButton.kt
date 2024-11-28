package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.gui.transition.AnimatedColor
import com.github.synnerz.akutz.api.objects.render.Color

open class UIButton @JvmOverloads constructor(
    x: Double = 0.0,
    y: Double = 0.0,
    w: Double = 100.0,
    h: Double = 100.0,
    p: Component? = null,
    var onClick: () -> Unit = {},
    var normalColor: AnimatedColor = AnimatedColor(Color.WHITE),
    var hoverColor: AnimatedColor = AnimatedColor(Color.GRAY),
    var clickColor: AnimatedColor = AnimatedColor(Color.DARK_GRAY)
) : UIRectangle(x, y, w, h, p) {
    private var stateBgColor = 1
    private fun setBgColor(col: BackgroundColor, on: Boolean) {
        stateBgColor = if (on) stateBgColor or col.flag
        else stateBgColor and col.flag.inv()
        if (stateBgColor and BackgroundColor.CLICK.flag > 0) bgColor.set(clickColor.get())
        else if (stateBgColor and BackgroundColor.HOVER.flag > 0) bgColor.set(hoverColor.get())
        else bgColor.set(normalColor.get())
    }
    fun updateColor() {
        setBgColor(BackgroundColor.NORMAL, true)
    }

    init {
        bgColor.set(normalColor.get())
    }

    override fun onMouseEnter(x: Double, y: Double): Boolean = setBgColor(BackgroundColor.HOVER, true).let { false }
    override fun onMouseLeave(x: Double, y: Double): Boolean = setBgColor(BackgroundColor.HOVER, false).let { false }
    override fun onMouseDown(x: Double, y: Double, button: Int): Boolean =
        setBgColor(BackgroundColor.CLICK, true).let { false }

    override fun onMouseUp(x: Double, y: Double, button: Int): Boolean =
        setBgColor(BackgroundColor.CLICK, false).let { false }

    override fun onDragEnd(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean =
        setBgColor(BackgroundColor.CLICK, false).let { false }

    override fun onMouseClick(x: Double, y: Double, button: Int): Boolean = onClick().let { true }

    private enum class BackgroundColor(val flag: Int) {
        NORMAL(1),
        HOVER(2),
        CLICK(4);
    }
}