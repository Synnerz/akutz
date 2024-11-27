package com.github.synnerz.akutz.api.objects.gui.transition

class AnimatedDouble(initialValue: Double) : AnimatedValue<Double>(initialValue) {
    override fun lerpValue(mult: Double): Double {
        return oldValue + (newValue - oldValue) * mult
    }
}