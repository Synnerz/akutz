package com.github.synnerz.akutz.api.objects.data

import com.github.synnerz.akutz.api.objects.render.Color

open class PropertyColor(initialValue: Color = Color.EMPTY, val allowAlpha: Boolean = true) :
    AProperty<Color>(initialValue) {
    override fun parse(value: String): Color = Color.fromRGBA(value.toLong())

    override fun serialize(): String = get().asRGBA().toString()

    override fun validate(value: Color) {
        if (!allowAlpha && value.a != 255) throw IllegalArgumentException("Alpha values other than 255 are disallowed. Found ${value.a}.")
    }

    override fun clone(): AProperty<Color> = PropertyColor(get(), allowAlpha)
}