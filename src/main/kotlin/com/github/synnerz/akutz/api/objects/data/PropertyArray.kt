package com.github.synnerz.akutz.api.objects.data

/**
 * validator cannot be used when the T is of type PropertyArray or PropertyObject
 */
open class PropertyArray<T : AProperty<*>>(
    val clazz: Class<T>,
    val validator: AProperty<Any>?,
    initialValue: List<T> = mutableListOf()
) :
    AProperty<MutableList<T>>(initialValue.toMutableList()) {
    protected val MAGIC = ","
    override fun parse(value: String): MutableList<T> {
        val o = validator ?: clazz.getConstructor().newInstance()
        return (value.split(MAGIC).map { o.parse(decb64(it)) as T }).toMutableList()
    }

    override fun serialize(): String = get().joinToString(MAGIC, transform = { encb64(it.serialize()) })

    override fun validate(value: MutableList<T>) {
        validator ?: return
        value.forEach { validator.validate(it) }
    }

    override fun clone(): AProperty<MutableList<T>> = PropertyArray(clazz, validator, get().toMutableList())

    override fun set(v: MutableList<T>) {
        if (v.size < get().size) get().dropLast(get().size - v.size)
        for (i in 0 until get().size) {
            (get()[i] as AProperty<Any>).set(v[i])
        }
        for (i in get().size until v.size) {
            get().add(v[i])
        }
    }

    open fun get(key: Int) = (get().getOrNull(key) as AProperty<*>?)?.get()
    open fun getArray(key: Int) = (get()[key] as PropertyArray<*>?)?.get()
    open fun getBoolean(key: Int) = (get()[key] as PropertyBoolean?)?.get()
    open fun getColor(key: Int) = (get()[key] as PropertyColor?)?.get()
    open fun getDouble(key: Int) = (get()[key] as PropertyDouble?)?.get()
    open fun getFlags(key: Int) = (get()[key] as PropertyFlags?)?.get()
    open fun getInteger(key: Int) = (get()[key] as PropertyInteger?)?.get()
    open fun getObject(key: Int) = (get()[key] as PropertyObject?)?.get()
    open fun getOption(key: Int) = (get()[key] as PropertyOption?)?.get()
    open fun getPercent(key: Int) = (get()[key] as PropertyPercent?)?.get()
    open fun getString(key: Int) = (get()[key] as PropertyString?)?.get()
}