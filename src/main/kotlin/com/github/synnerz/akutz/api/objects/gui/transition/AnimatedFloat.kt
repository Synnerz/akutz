package com.github.synnerz.akutz.api.objects.gui.transition

class AnimatedFloat @JvmOverloads constructor(
    initialValue: Float,
    animationTimeMS: Long = 0,
    easingFunction: (x: Double) -> Double = { it }
) : AnimatedValue<Float>(initialValue, animationTimeMS, easingFunction) {
    override fun lerpValue(mult: Double): Float {
        return oldValue + (newValue - oldValue) * mult.toFloat()
    }
}