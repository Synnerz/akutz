package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.api.objects.render.Display

class UITextButton(
    p: Component? = null,
    x: Double = 0.0,
    y: Double = 0.0,
    w: Double = 100.0,
    h: Double = 100.0,
    onClick: () -> Unit = {},
    text: String = "",
    normalColor: Color = Color.WHITE,
    hoverColor: Color = Color.GRAY,
    clickColor: Color = Color.DARK_GRAY
) : UIButton(p, x, y, w, h, onClick, normalColor, hoverColor, clickColor) {
    val text = UIText(this, 0.0, 0.0, 100.0, 100.0, Display().also { it.addLine(text) })
}