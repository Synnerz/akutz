package com.github.synnerz.akutz.api.objects.data

import com.github.synnerz.akutz.api.objects.state.StateVar

abstract class AProperty<T>(initialValue: T) : StateVar<T>(initialValue) {
    abstract fun parse(value: String): T
    abstract fun serialize(): String
    abstract fun validate(value: T)
    abstract fun clone(): AProperty<T>
    open fun update(value: String) = apply {
        val v = parse(value)
        validate(v)
        set(v)
    }
}