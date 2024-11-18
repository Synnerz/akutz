package com.github.synnerz.akutz.api.events

import net.minecraft.entity.Entity
import net.minecraft.network.Packet
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntitySign

abstract class ClassFilterEvent(
    method: (args: Array<out Any?>) -> Unit,
    eventType: EventType
) : BaseEvent(method, eventType) {
    private var clazzes: List<Class<*>> = emptyList()

    fun setFilteredClass(clazz: Class<*>) = setFilteredClasses(listOf(clazz))

    fun setFilteredClasses(clz: List<Class<*>>) = apply {
        this.clazzes = clz
    }

    fun setFilteredClasses(vararg clz: Class<*>) = apply {
        this.clazzes = clz.toList()
    }

    override fun trigger(args: Array<out Any?>) {
        val eventClazz = getClassType(args[0]) ?: return
        if (clazzes.isEmpty() || clazzes.any { it.isInstance(eventClazz) })
            callMethod(args)
    }

    abstract fun getClassType(clazz: Any?) : Any?
}

class RenderEntityEvent(
    method: (args: Array<out Any?>) -> Unit,
    eventType: EventType
) : ClassFilterEvent(method, eventType) {
    override fun getClassType(clazz: Any?): Entity? = clazz as? Entity
}

class RenderTileEntityEvent(
    method: (args: Array<out Any?>) -> Unit,
    eventType: EventType
) : ClassFilterEvent(method, eventType) {
    override fun getClassType(clazz: Any?): TileEntity? = clazz as? TileEntitySign
}

class PacketEvent(
    method: (args: Array<out Any?>) -> Unit,
    eventType: EventType
) : ClassFilterEvent(method, eventType) {
    override fun getClassType(clazz: Any?): Packet<*>? = clazz as? Packet<*>
}