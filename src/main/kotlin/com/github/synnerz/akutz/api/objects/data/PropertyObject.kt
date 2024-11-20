package com.github.synnerz.akutz.api.objects.data

open class PropertyObject(initialValue: Map<String, AProperty<Any>> = mutableMapOf()) :
    AProperty<MutableMap<String, AProperty<Any>>>(initialValue.toMutableMap()) {
    protected val MAGIC1 = "TREEMANSAIDTHISCODEISSMELLY"
    protected val MAGIC2 = "IWOULDHAVETOAGREEWITHHIMTHISSHITSUCKS"
    override fun parse(value: String): MutableMap<String, AProperty<Any>> =
        (get() + value.split(MAGIC1).map { it.split(MAGIC2) }.filter { get().containsKey(it[0]) }
            .associate { it[0] to get()[it[0]]!!.clone().update(it[1]) }).toMutableMap()

    override fun serialize(): String =
        get().toList().joinToString(separator = MAGIC1) { it.first + MAGIC2 + it.second.serialize() }

    override fun validate(value: MutableMap<String, AProperty<Any>>) =
        get().forEach { (k, v) -> value[k]!!.validate(v) }

    override fun clone(): AProperty<MutableMap<String, AProperty<Any>>> = PropertyObject(get().toMutableMap())

    override fun set(v: MutableMap<String, AProperty<Any>>) = v.forEach { (k, v) -> get()[k]?.set(v) }

    open fun getArray(key: String) = get()[key] as PropertyArray<*>
    open fun getBoolean(key: String) = get()[key] as PropertyBoolean
    open fun getColor(key: String) = get()[key] as PropertyColor
    open fun getDouble(key: String) = get()[key] as PropertyDouble
    open fun getFlags(key: String) = get()[key] as PropertyFlags
    open fun getInteger(key: String) = get()[key] as PropertyInteger
    open fun getObject(key: String) = get()[key] as PropertyObject
    open fun getOption(key: String) = get()[key] as PropertyOption
    open fun getPercent(key: String) = get()[key] as PropertyPercent
    open fun getString(key: String) = get()[key] as PropertyString
}