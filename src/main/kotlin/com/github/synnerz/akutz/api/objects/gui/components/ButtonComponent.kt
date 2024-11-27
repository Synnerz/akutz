package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.api.objects.render.Display

open class ButtonComponent @JvmOverloads constructor(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    p: BaseComponent? = null,
    var onClick: () -> Unit = {},
    text: String = "",
    var normalColor: Color = Color.WHITE,
    var hoverColor: Color = Color.GRAY,
    var clickColor: Color = Color.DARK_GRAY
) : TextComponent(x, y, w, h, p, Display().also { it.addLine(text) }) {
    private var stateBgColor = 1
    private fun setBgColor(col: BackgroundColor, on: Boolean) {
        stateBgColor = if (on) stateBgColor or col.flag
        else stateBgColor and col.flag.inv()
        if (stateBgColor or BackgroundColor.CLICK.flag > 0) bgColor.set(clickColor)
        else if (stateBgColor or BackgroundColor.HOVER.flag > 0) bgColor.set(hoverColor)
        else bgColor.set(normalColor)
    }

    init {
        bgColor.set(normalColor)
    }

    override fun onMouseEnter(x: Double, y: Double): Boolean = setBgColor(BackgroundColor.HOVER, true).let { false }
    override fun onMouseLeave(x: Double, y: Double): Boolean = setBgColor(BackgroundColor.HOVER, false).let { false }
    override fun onMouseDown(x: Double, y: Double, button: Int): Boolean =
        setBgColor(BackgroundColor.CLICK, true).let { false }

    override fun onMouseUp(x: Double, y: Double, button: Int): Boolean =
        setBgColor(BackgroundColor.CLICK, false).let { false }

    override fun onDragEnd(x0: Double, y0: Double, dx: Double, dy: Double, button: Int): Boolean =
        setBgColor(BackgroundColor.CLICK, false).let { false }

    override fun onMouseClick(x: Double, y: Double, button: Int): Boolean = onClick().let { false }

    private enum class BackgroundColor(val flag: Int) {
        NORMAL(1),
        HOVER(2),
        CLICK(4);
    }
}