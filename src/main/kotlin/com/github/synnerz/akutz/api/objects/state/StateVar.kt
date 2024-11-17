package com.github.synnerz.akutz.api.objects.state

open class StateVar<T>(initialValue: T) {
    private var value = initialValue
    private val hooks: MutableList<(newValue: T, oldValue: T) -> Unit> = mutableListOf()

    open fun get() = value

    open fun set(v: T) {
        if (value == v) return
        val oldValue = value
        value = v
        trigger(oldValue)
    }

    open fun listen(callback: () -> Unit) = listen { _, _ -> callback() }
    open fun listen(callback: (newValue: T) -> Unit) = listen { newValue, _ -> callback(newValue) }
    open fun listen(callback: (newValue: T, oldValue: T) -> Unit) {
        hooks.add(callback)
    }

    protected fun trigger(oldValue: T) {
        hooks.forEach { callback ->
            callback(value, oldValue)
        }
    }
}
