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
    fun getIndex(): Int = if (mcSlot is CreativeSlot) {
        mcSlot.slotIndex
    } else {
        mcSlot.slotNumber
    }

    fun getDisplayX(): Int = mcSlot.xDisplayPosition

    fun getDisplayY(): Int = mcSlot.yDisplayPosition

    fun getInventory(): Inventory = Inventory(mcSlot.inventory)

    fun getItem(): Item? = mcSlot.stack?.let(::Item)

    fun toMC(): MCSlot = mcSlot

    override fun toString(): String = "Slot ${getIndex()} of (${getInventory().getClassName()}: ${getInventory().getName()}): ${getItem()}"
}