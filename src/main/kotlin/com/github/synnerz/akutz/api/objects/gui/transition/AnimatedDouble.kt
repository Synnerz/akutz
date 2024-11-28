package com.github.synnerz.akutz.api.objects.gui.transition

class AnimatedDouble @JvmOverloads constructor(
    initialValue: Double,
    animationTimeMS: Long = 0,
    easingFunction: (x: Double) -> Double = { it }
) : AnimatedValue<Double>(initialValue, animationTimeMS, easingFunction) {
    override fun lerpValue(mult: Double): Double {
        return oldValue + (newValue - oldValue) * mult
    }
}