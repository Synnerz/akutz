package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.state.IState
import com.github.synnerz.akutz.api.objects.state.StateVar

abstract class UIStateValue<T> @JvmOverloads constructor(
    initialValue: T,
    x: Double = 0.0,
    y: Double = 0.0,
    w: Double = 100.0,
    h: Double = 100.0,
    p: Component? = null
) : Component(x, y, w, h, p), IState<T> {
    private val state = StateVar(initialValue)
    override fun get(): T = state.get()
    override fun listen(callback: (newValue: T, oldValue: T) -> Unit) = state.listen(callback)
    override fun set(v: T) = state.set(v)
}