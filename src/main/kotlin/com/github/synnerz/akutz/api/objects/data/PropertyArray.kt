package com.github.synnerz.akutz.api.objects.data

open class PropertyArray<T : AProperty<*>>(val clazz: Class<T>, val validator: T?, initialValue: List<T> = mutableListOf()) :
    AProperty<MutableList<T>>(initialValue.toMutableList()) {
    protected val MAGIC = "OOGABOOGACAVEMANBRAIN"
    override fun parse(value: String): MutableList<T> {
        val o = validator ?: clazz.getConstructor().newInstance()
        return value.split(MAGIC).map { o.clone().update(it) } as MutableList<T>
    }

    override fun serialize(): String = get().joinToString(MAGIC, transform = { it.serialize() })

    override fun validate(value: MutableList<T>) {}

    override fun clone(): AProperty<MutableList<T>> = PropertyArray(clazz, validator, get().toMutableList())

    open fun getArray(key: Int) = get()[key] as PropertyArray<*>
    open fun getBoolean(key: Int) = get()[key] as PropertyBoolean
    open fun getColor(key: Int) = get()[key] as PropertyColor
    open fun getDouble(key: Int) = get()[key] as PropertyDouble
    open fun getFlags(key: Int) = get()[key] as PropertyFlags
    open fun getInteger(key: Int) = get()[key] as PropertyInteger
    open fun getObject(key: Int) = get()[key] as PropertyObject
    open fun getOption(key: Int) = get()[key] as PropertyOption
    open fun getPercent(key: Int) = get()[key] as PropertyPercent
    open fun getString(key: Int) = get()[key] as PropertyString
}