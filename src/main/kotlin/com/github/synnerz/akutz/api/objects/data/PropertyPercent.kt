package com.github.synnerz.akutz.api.objects.data

open class PropertyPercent(initialValue: Double = 0.0, val min: Double = 0.0, val max: Double = 1.0) :
    AProperty<Double>(initialValue) {
    override fun parse(value: String): Double = value.toInt() / 100.0

    override fun serialize(): String = (get() * 100).toInt().toString()

    override fun validate(value: Double) {
        val v = (value * 100).toInt()
        val l = (min * 100).toInt()
        val r = (max * 100).toInt()
        if (v < l) throw IllegalArgumentException("Value must be at least $l, found $v.")
        if (v > r) throw IllegalArgumentException("Value must be no more than $r, found $v.")
    }

    override fun clone(): AProperty<Double> = PropertyPercent(get(), min, max)
}