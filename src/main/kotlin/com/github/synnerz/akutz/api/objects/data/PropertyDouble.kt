package com.github.synnerz.akutz.api.objects.data

open class PropertyDouble(
    initialValue: Double = 0.0,
    val min: Double = Double.NEGATIVE_INFINITY,
    val max: Double = Double.POSITIVE_INFINITY
) : AProperty<Double>(initialValue) {
    override fun parse(value: String): Double = value.toDouble()

    override fun serialize(): String = get().toString()

    override fun validate(value: Double) {
        if (value < min) throw IllegalArgumentException("Value must be at least $min, found $value.")
        if (value > max) throw IllegalArgumentException("Value must be no more than $max, found $value.")
    }

    override fun clone(): AProperty<Double> = PropertyDouble(get(), min, max)
}