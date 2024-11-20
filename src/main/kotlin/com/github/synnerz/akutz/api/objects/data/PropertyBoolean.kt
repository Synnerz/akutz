package com.github.synnerz.akutz.api.objects.data

class PropertyBoolean(initialValue: Boolean = false) : AProperty<Boolean>(initialValue) {
    override fun parse(value: String): Boolean = value.toBoolean()

    override fun serialize(): String = get().toString()

    override fun validate(value: Boolean) {}

    override fun clone(): AProperty<Boolean> = PropertyBoolean(get())
}