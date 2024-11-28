package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.api.objects.render.Display

class UITextButton(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    p: Component? = null,
    onClick: () -> Unit = {},
    text: String = "",
    normalColor: Color = Color.WHITE,
    hoverColor: Color = Color.GRAY,
    clickColor: Color = Color.DARK_GRAY
) : UIButton(x, y, w, h, p, onClick, normalColor, hoverColor, clickColor) {
    val text = UIText(0.0, 0.0, 100.0, 100.0, this, Display().also { it.addLine(text) })
}