package com.github.synnerz.akutz.api.objects.gui.transition

class AnimatedFloat(initialValue: Float) : AnimatedValue<Float>(initialValue) {
    override fun lerpValue(mult: Double): Float {
        return oldValue + (newValue - oldValue) * mult.toFloat()
    }
}