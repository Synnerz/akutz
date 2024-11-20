package com.github.synnerz.akutz.api.objects.data

class PropertyArray<T : AProperty<*>>(val clazz: Class<T>, initialValue: List<T> = mutableListOf(), val validator: T?) :
    AProperty<List<T>>(initialValue) {
    private val MAGIC = "OOGABOOGACAVEMANBRAIN"
    override fun parse(value: String): List<T> {
        val o = validator ?: clazz.getConstructor().newInstance()
        return value.split(MAGIC).map { o.clone().update(it) } as List<T>
    }

    override fun serialize(): String = get().joinToString(MAGIC, transform = { it.serialize() })

    override fun validate(value: List<T>) {}

    override fun clone(): AProperty<List<T>> = PropertyArray(clazz, get().toMutableList(), validator)
}