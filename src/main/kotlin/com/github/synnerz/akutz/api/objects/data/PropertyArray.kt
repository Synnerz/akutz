package com.github.synnerz.akutz.api.objects.data

/**
 * validator cannot be used when the T is of type PropertyArray or PropertyObject
 */
open class PropertyArray<T : AProperty<Any>>(
    val clazz: Class<T>,
    val validator: T?,
    initialValue: List<T> = mutableListOf()
) :
    AProperty<MutableList<T>>(initialValue.toMutableList()) {
    protected val MAGIC = "OOGABOOGACAVEMANBRAIN"
    override fun parse(value: String): MutableList<T> {
        val o = validator ?: clazz.getConstructor().newInstance()
        return (value.split(MAGIC).map { o.parse(it) as T }).toMutableList()
    }

    override fun serialize(): String = get().joinToString(MAGIC, transform = { it.serialize() })

    override fun validate(value: MutableList<T>) {
        validator ?: return
        value.forEach{ validator.validate(it) }
    }

    override fun clone(): AProperty<MutableList<T>> = PropertyArray(clazz, validator, get().toMutableList())

    override fun set(v: MutableList<T>) {
        if (v.size < get().size) get().dropLast(get().size - v.size)
        for (i in 0 until get().size) {
            get()[i].set(v[i])
        }
        for (i in get().size until v.size) {
            get().add(v[i])
        }
    }

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