package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.state.IState
import com.github.synnerz.akutz.api.objects.state.StateVar

abstract class UIStateValue<T>(
    initialValue: T,
    p: Component? = null,
    x: Double,
    y: Double,
    w: Double,
    h: Double
) : Component(p, x, y, w, h), IState<T> {
    private val state = StateVar(initialValue)
    override fun get(): T = state.get()
    override fun listen(callback: (newValue: T, oldValue: T) -> Unit) = state.listen(callback)
    override fun set(v: T) = state.set(v)
}