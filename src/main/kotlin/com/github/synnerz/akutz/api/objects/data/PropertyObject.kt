package com.github.synnerz.akutz.api.objects.data

class PropertyObject(initialValue: Map<String, AProperty<*>> = mutableMapOf()) :
    AProperty<Map<String, AProperty<*>>>(initialValue) {
    private val MAGIC1 = "TREEMANSAIDTHISCODEISSMELLY"
    private val MAGIC2 = "IWOULDHAVETOAGREEWITHHIMTHISSHITSUCKS"
    override fun parse(value: String): Map<String, AProperty<*>> = value.split(MAGIC1).associate {
        val split = it.split(MAGIC2)
        split[0] to get()[split[0]]!!.clone().update(split[1])
    }

    override fun serialize(): String = get().toList().joinToString { it.first + MAGIC2 + it.second.serialize() }

    override fun validate(value: Map<String, AProperty<*>>) {
        value.forEach { k, v -> (get()[k]!! as AProperty<Any>).validate(v) }
    }

    override fun clone(): AProperty<Map<String, AProperty<*>>> = PropertyObject(get().toMutableMap())
}