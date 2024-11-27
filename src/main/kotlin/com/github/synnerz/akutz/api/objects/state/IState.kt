package com.github.synnerz.akutz.api.objects.state

interface IState<T> {
    fun get(): T
    fun set(v: T)
    fun listen(callback: () -> Unit) = listen { _, _ -> callback() }
    fun listen(callback: (newValue: T) -> Unit) = listen { newValue, _ -> callback(newValue) }
    fun listen(callback: (newValue: T, oldValue: T) -> Unit)
}