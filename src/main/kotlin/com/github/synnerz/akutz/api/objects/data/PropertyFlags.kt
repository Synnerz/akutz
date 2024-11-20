package com.github.synnerz.akutz.api.objects.data

open class PropertyFlags(initialValue: ULong = 0u, val flags: List<String> = listOf()) : AProperty<ULong>(initialValue) {
    protected val flagsMap: Map<String, Int> = flags.associateWith { flags.indexOf(it) }
    override fun parse(value: String): ULong = value.toULong()

    override fun serialize(): String = get().toString()

    override fun validate(value: ULong) {
        val highest = 63 - value.countLeadingZeroBits()
        if (highest >= flags.size) throw IllegalArgumentException("Unknown flag is set at position $highest (0-indexed).")
    }

    override fun clone(): AProperty<ULong> = PropertyFlags(get(), flags)

    open fun getFlag(flag: String): Boolean =
        getFlag(flagsMap[flag] ?: throw IllegalArgumentException("Unknown flag $flag."))

    open fun getFlag(flag: Int): Boolean {
        if (flag >= flags.size) throw IllegalArgumentException("Unknown flag is at position $flag (0-indexed).")
        return (get() and (1u).toULong().shl(flag)) > 0u
    }

    open fun setFlag(flag: String, value: Boolean) = setFlag(flagsMap[flag] ?: throw IllegalArgumentException("Unknown flag $flag."), value)

    open fun setFlag(flag: Int, value: Boolean) {
        if (flag >= flags.size) throw IllegalArgumentException("Unknown flag is at position $flag (0-indexed).")
        val mask = (1u).toULong().shl(flag)
        if (value) set(get() or mask)
        else set(get() and mask.inv())
    }

    open fun getAllFlags(): Map<String, Boolean> = flags.associateWith { getFlag(it) }
}