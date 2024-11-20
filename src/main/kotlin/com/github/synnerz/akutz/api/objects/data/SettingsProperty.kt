package com.github.synnerz.akutz.api.objects.data

class SettingsProperty<T>(
    val inner: AProperty<T>,
    var category: String,
    var subcategory: String,
    var name: String,
    var description: String
) : AProperty<T>(inner.get()) {
    override fun parse(value: String) = inner.parse(value)
    override fun serialize(): String = inner.serialize()
    override fun clone() = inner.clone()
    override fun validate(value: T) = inner.validate(value)
    override fun get() = inner.get()
    override fun set(v: T) = inner.set(v)
}