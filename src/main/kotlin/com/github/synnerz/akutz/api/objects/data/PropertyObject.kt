package com.github.synnerz.akutz.api.objects.data

open class PropertyObject(initialValue: Map<String, AProperty<*>> = mutableMapOf()) :
    AProperty<MutableMap<String, AProperty<*>>>(initialValue.toMutableMap()) {
    protected val MAGIC1 = "TREEMANSAIDTHISCODEISSMELLY"
    protected val MAGIC2 = "IWOULDHAVETOAGREEWITHHIMTHISSHITSUCKS"
    override fun parse(value: String): MutableMap<String, AProperty<*>> = (get() + value.split(MAGIC1).associate {
        val split = it.split(MAGIC2)
        split[0] to get()[split[0]]!!.clone().update(split[1])
    }).toMutableMap()

    override fun serialize(): String = get().toList().joinToString { it.first + MAGIC2 + it.second.serialize() }

    override fun validate(value: MutableMap<String, AProperty<*>>) {
        value.forEach { (k, v) -> (get()[k]!! as AProperty<Any>).validate(v) }
    }

    override fun clone(): AProperty<MutableMap<String, AProperty<*>>> = PropertyObject(get().toMutableMap())

    override fun set(v: MutableMap<String, AProperty<*>>) = throw UnsupportedOperationException("cant set on an object sorry :(")
}