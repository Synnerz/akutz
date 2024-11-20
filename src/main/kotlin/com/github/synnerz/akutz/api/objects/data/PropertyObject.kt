package com.github.synnerz.akutz.api.objects.data

open class PropertyObject(initialValue: Map<String, AProperty<*>> = mutableMapOf()) :
    AProperty<MutableMap<String, AProperty<*>>>(initialValue.toMutableMap()) {
    protected val MAGIC1 = "|"
    protected val MAGIC2 = ":"
    override fun parse(value: String): MutableMap<String, AProperty<*>> =
        (get() + value.split(MAGIC1).map { it.split(MAGIC2).map { decb64(it) } }.filter { get().containsKey(it[0]) }
            .associate { it[0] to get()[it[0]]!!.clone().update(it[1]) }).toMutableMap()

    override fun serialize(): String =
        get().toList().joinToString(separator = MAGIC1) { encb64(it.first) + MAGIC2 + encb64(it.second.serialize()) }

    override fun validate(value: MutableMap<String, AProperty<*>>) =
        get().forEach { (k, v) -> (value[k]!! as AProperty<Any>).validate(v) }

    override fun clone(): AProperty<MutableMap<String, AProperty<*>>> = PropertyObject(get().toMutableMap())

    override fun set(v: MutableMap<String, AProperty<*>>) =
        v.forEach { (k, v) -> (get()[k] as AProperty<Any>?)?.set(v) }

    open fun get(key: String) = get()[key]?.get()
    open fun getArray(key: String) = (get()[key] as PropertyArray<*>?)?.get()
    open fun getBoolean(key: String) = (get()[key] as PropertyBoolean?)?.get()
    open fun getColor(key: String) = (get()[key] as PropertyColor?)?.get()
    open fun getDouble(key: String) = (get()[key] as PropertyDouble?)?.get()
    open fun getFlags(key: String) = (get()[key] as PropertyFlags?)?.get()
    open fun getInteger(key: String) = (get()[key] as PropertyInteger?)?.get()
    open fun getObject(key: String) = (get()[key] as PropertyObject?)?.get()
    open fun getOption(key: String) = (get()[key] as PropertyOption?)?.get()
    open fun getPercent(key: String) = (get()[key] as PropertyPercent?)?.get()
    open fun getString(key: String) = (get()[key] as PropertyString?)?.get()
}