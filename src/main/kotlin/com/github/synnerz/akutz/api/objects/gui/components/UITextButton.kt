package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.gui.transition.AnimatedColor
import com.github.synnerz.akutz.api.objects.render.Color
import com.github.synnerz.akutz.api.objects.render.Display

class UITextButton(
    x: Double = 0.0,
    y: Double = 0.0,
    w: Double = 100.0,
    h: Double = 100.0,
    p: Component? = null,
    onClick: () -> Unit = {},
    text: String = "",
    normalColor: Color = Color.WHITE,
    hoverColor: Color = Color.GRAY,
    clickColor: Color = Color.DARK_GRAY
) : UIButton(x, y, w, h, p, onClick, AnimatedColor(normalColor), AnimatedColor(hoverColor), AnimatedColor(clickColor)) {
    val text = UIText(p = this, display = Display().also { it.addLine(text) })
}