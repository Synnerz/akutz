package com.github.synnerz.akutz.api.objects.gui.transition

import com.github.synnerz.akutz.api.objects.render.Color

class AnimatedColor(initialValue: Color) : AnimatedValue<Color>(initialValue) {
    override fun lerpValue(mult: Double): Color {
        return Color(
            (oldValue.r + (newValue.r - oldValue.r) * mult).toInt(),
            (oldValue.g + (newValue.g - oldValue.g) * mult).toInt(),
            (oldValue.b + (newValue.b - oldValue.b) * mult).toInt(),
            (oldValue.a + (newValue.a - oldValue.a) * mult).toInt()
        )
    }
}