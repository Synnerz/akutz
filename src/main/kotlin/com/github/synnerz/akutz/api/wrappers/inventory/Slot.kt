package com.github.synnerz.akutz.api.wrappers.inventory

import net.minecraft.client.gui.inventory.GuiContainerCreative.CreativeSlot
import net.minecraft.inventory.Slot as MCSlot

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/inventory/Slot.kt)
 */
class Slot(
    val mcSlot: MCSlot
) {
    private val creativeSlot: Boolean = mcSlot is CreativeSlot

    fun getIndex(): Int = if (creativeSlot) {
        mcSlot.slotIndex
    } else {
        mcSlot.slotNumber
    }

    fun getDisplayX(): Int = mcSlot.xDisplayPosition

    fun getDisplayY(): Int = mcSlot.yDisplayPosition

    fun getInventory(): Inventory = Inventory(mcSlot.inventory)

    fun getItem(): Item? = mcSlot.stack?.let(::Item)

    fun isCreativeSlot() = creativeSlot

    fun toMC(): MCSlot = mcSlot

    override fun toString(): String = "Slot ${getIndex()} of (${getInventory().getClassName()}: ${getInventory().getName()}): ${getItem()}"
}