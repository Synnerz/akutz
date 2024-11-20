package com.github.synnerz.akutz.api.objects.data

class PropertyOption(initialValue: String = "", val options: Set<String> = setOf("")) :
    AProperty<String>(initialValue) {
    constructor(initialValue: String, options: List<String>) : this(initialValue, options.toSet())

    override fun parse(value: String): String = value

    override fun serialize(): String = get()

    override fun validate(value: String) {
        if (!options.contains(value)) throw IllegalArgumentException("Value $value is not a valid option")
    }

    override fun clone(): AProperty<String> = PropertyOption(get(), options)
}