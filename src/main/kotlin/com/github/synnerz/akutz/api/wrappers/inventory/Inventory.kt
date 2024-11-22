package com.github.synnerz.akutz.api.wrappers.inventory

import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/inventory/Inventory.kt)
 */
class Inventory {
    val inventory: IInventory?
    val container: Container?

    constructor(inventory: IInventory) {
        this.inventory = inventory
        container = null
    }

    constructor(container: Container) {
        this.container = container
        inventory = null
    }

    fun getSize(): Int = inventory?.sizeInventory ?: container!!.inventorySlots.size

    fun getStackInSlot(slot: Int): Item? {
        return if (inventory == null)
            container!!.getSlot(slot).stack?.let(::Item)
        else inventory.getStackInSlot(slot)?.let(::Item)
    }

    fun getWindowId(): Int = container?.windowId ?: -1

    fun getItems(): List<Item?> {
        return (0 until getSize()).map(::getStackInSlot)
    }

    fun contains(item: Item): Boolean {
        return getItems().contains(item)
    }

    fun contains(id: Int): Boolean {
        return getItems().any { it?.getID() == id }
    }

    fun indexOf(item: Item): Int {
        return getItems().indexOf(item)
    }

    fun indexOf(id: Int): Int {
        return getItems().indexOfFirst { it?.getID() == id }
    }

    fun isContainer(): Boolean = container != null

    fun getName(): String {
        return when (container) {
            is ContainerChest -> container.lowerChestInventory.name
            else -> inventory?.name ?: "container"
        }
    }

    fun getClassName(): String = inventory?.javaClass?.simpleName ?: container!!.javaClass.simpleName

    fun toMC() = if (isContainer()) container else inventory

    override fun toString(): String = "Inventory{name=\"${getName()}\", size=\"${getSize()}\", type= ${if (isContainer()) "container" else "inventory"}}"
}