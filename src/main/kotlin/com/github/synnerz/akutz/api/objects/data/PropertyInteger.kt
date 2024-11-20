package com.github.synnerz.akutz.api.objects.data

open class PropertyInteger(initialValue: Int = 0, val min: Int = Int.MIN_VALUE, val max: Int = Int.MAX_VALUE) :
    AProperty<Int>(initialValue) {
    override fun parse(value: String): Int = value.toInt()

    override fun serialize(): String = get().toString()

    override fun validate(value: Int) {
        if (value < min) throw IllegalArgumentException("Value must be at least $min, found $value.")
        if (value > max) throw IllegalArgumentException("Value must be no more than $max, found $value.")
    }

    override fun clone(): AProperty<Int> = PropertyInteger(get(), min, max)
}