package com.github.synnerz.akutz.api.wrappers.inventory

import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.api.wrappers.Player
import com.github.synnerz.akutz.api.wrappers.entity.Entity
import com.github.synnerz.akutz.api.wrappers.message.TextComponent
import com.github.synnerz.akutz.api.wrappers.world.block.Block
import net.minecraft.entity.item.EntityItem
import net.minecraft.block.Block as MCBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.Item as MCItem

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/inventory/Item.kt)
 */
class Item {
    val item: MCItem
    var itemStack: ItemStack

    constructor(itemStack: ItemStack) {
        item = itemStack.item
        this.itemStack = itemStack
    }

    constructor(itemName: String) {
        item = MCItem.getByNameOrId(itemName) ?: throw IllegalArgumentException("Error itemName: $itemName is not a valid Item Name.")
        itemStack = ItemStack(item)
    }

    constructor(itemID: Int) {
        item = MCItem.getItemById(itemID) ?: throw IllegalArgumentException("Error itemID: $itemID is not a valid Item ID.")
        itemStack = ItemStack(item)
    }

    constructor(block: Block) {
        item = MCItem.getItemFromBlock(block.mcBlock) ?: throw IllegalArgumentException("Error block: $block is not a valid Item.")
        itemStack = ItemStack(item)
    }

    constructor(mcBlock: MCBlock) {
        item = MCItem.getItemFromBlock(mcBlock) ?: throw IllegalArgumentException("Error block: $mcBlock is not a valid Item.")
        itemStack = ItemStack(item)
    }

    constructor(entityItem: EntityItem) {
        item = entityItem.entityItem.item
        itemStack = entityItem.entityItem
    }

    constructor(entity: Entity) {
        require(entity.entity is EntityItem) { "$entity is not a valid EntityItem." }

        item = entity.entity.entityItem.item
        itemStack = entity.entity.entityItem
    }

    fun getRawNBT(): String = itemStack.serializeNBT().toString()

    fun getID(): Int = MCItem.getIdFromItem(item)

    fun getStackSize(): Int = itemStack.stackSize

    fun getUnlocalizedName(): String = item.unlocalizedName

    fun getRegistryName(): String = item.registryName.toString()

    fun getName(): String = itemStack.displayName

    fun setName(name: String) = apply {
        itemStack.setStackDisplayName(ChatLib.addColor(name))
    }

    fun isEnchanted(): Boolean = itemStack.isItemEnchanted

    fun getMetadata(): Int = itemStack.metadata

    fun getDurability(): Int = getMaxDamage() - getDamage()

    fun getDamage(): Int = itemStack.itemDamage

    fun setDamage(dmg: Int) = apply {
        itemStack.itemDamage = dmg
    }

    fun getMaxDamage(): Int = itemStack.maxDamage

    fun getLore(): List<String> = itemStack.getTooltip(Player.getPlayer(), Client.getMinecraft().gameSettings.advancedItemTooltips)

    // TODO
    // fun draw()

    fun getTextComponent() = TextComponent(itemStack.chatComponent)

    override fun equals(other: Any?): Boolean {
        return other is Item &&
                getID() == other.getID() &&
                getStackSize() == other.getStackSize() &&
                getDamage() == other.getDamage()
    }

    override fun hashCode(): Int {
        var result = item.hashCode()
        result = 31 * result + itemStack.hashCode()
        return result
    }

    override fun toString(): String = itemStack.toString()
}