package com.github.synnerz.akutz.api.objects.state

open class StateVar<T>(initialValue: T) : IState<T> {
    private var value = initialValue
    internal val hooks: MutableList<(T, T) -> Unit> = mutableListOf()

    override fun get() = value

    override fun set(v: T) {
        if (value == v) return
        val oldValue = value
        value = v
        trigger(oldValue)
    }

    override fun listen(callback: (T, T) -> Unit) {
        hooks.add(callback)
    }

    protected fun trigger(oldValue: T) {
        hooks.forEach { it(value, oldValue) }
    }
}
