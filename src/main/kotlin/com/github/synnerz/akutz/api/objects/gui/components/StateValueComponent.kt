package com.github.synnerz.akutz.api.objects.gui.components

import com.github.synnerz.akutz.api.objects.state.IState
import com.github.synnerz.akutz.api.objects.state.StateVar

abstract class StateValueComponent<T>(
    initialValue: T,
    _x: Double,
    _y: Double,
    _w: Double,
    _h: Double,
    p: BaseComponent? = null
) : BaseComponent(_x, _y, _w, _h, p), IState<T> {
    private val state = StateVar(initialValue)
    override fun get(): T = state.get()
    override fun listen(callback: (newValue: T, oldValue: T) -> Unit) = state.listen(callback)
    override fun set(v: T) = state.set(v)
}