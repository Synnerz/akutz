package com.github.synnerz.akutz.api.objects.data

open class PropertyString(initialValue: String = "", val minLength: Int = 0, val maxLength: Int = Int.MAX_VALUE) :
    AProperty<String>(initialValue) {
    override fun parse(value: String): String = value

    override fun serialize(): String = get()

    override fun validate(value: String) {
        if (value.length < minLength) throw IllegalArgumentException("Length must be at least $minLength, found ${value.length}.")
        if (value.length > maxLength) throw IllegalArgumentException("Length must be no more than $maxLength, found ${value.length}.")
    }

    override fun clone(): AProperty<String> = PropertyString(get(), minLength, maxLength)
}