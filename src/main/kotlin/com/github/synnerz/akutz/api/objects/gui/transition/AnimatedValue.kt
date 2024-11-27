package com.github.synnerz.akutz.api.objects.gui.transition

abstract class AnimatedValue<T> @JvmOverloads constructor(
    initialValue: T,
    var animationTimeMS: Long = 0,
    var easingFunction: (x: Double) -> Double = { it }
) {
    protected var oldValue: T = initialValue
    protected var newValue: T = initialValue
    private var startTime: Long = 0
    protected abstract fun lerpValue(mult: Double): T
    open fun get(): T {
        val dt = System.currentTimeMillis() - startTime
        if (dt >= animationTimeMS) return newValue
        if (dt <= 0) return oldValue
        return lerpValue(easingFunction(dt / animationTimeMS.toDouble()))
    }

    open fun set(value: T) {
        if (value == newValue) return
        oldValue = newValue
        newValue = value
        startTime = System.currentTimeMillis()
    }
}