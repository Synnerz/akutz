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
}