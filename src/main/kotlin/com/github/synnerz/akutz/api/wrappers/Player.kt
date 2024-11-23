package com.github.synnerz.akutz.api.wrappers

import com.github.synnerz.akutz.api.wrappers.entity.Entity
import com.github.synnerz.akutz.api.wrappers.entity.PlayerMP
import com.github.synnerz.akutz.api.wrappers.inventory.Inventory
import com.github.synnerz.akutz.api.wrappers.inventory.Item
import com.github.synnerz.akutz.api.wrappers.world.block.Block
import com.github.synnerz.akutz.api.wrappers.world.block.Sign
import net.minecraft.block.BlockSign
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.MovingObjectPosition
import java.util.UUID

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/Player.kt)
 */
object Player {
    @JvmStatic
    fun getPlayer(): EntityPlayerSP? = Client.getMinecraft().thePlayer

    @JvmStatic
    fun getX(): Double = getPlayer()?.posX ?: 0.0

    @JvmStatic
    fun getY(): Double = getPlayer()?.posY ?: 0.0

    @JvmStatic
    fun getZ(): Double = getPlayer()?.posZ ?: 0.0

    @JvmStatic
    fun getLastX(): Double = getPlayer()?.lastTickPosX ?: 0.0

    @JvmStatic
    fun getLastY(): Double = getPlayer()?.lastTickPosY ?: 0.0

    @JvmStatic
    fun getLastZ(): Double = getPlayer()?.lastTickPosZ ?: 0.0

    @JvmStatic
    fun getName(): String = Client.getMinecraft().session.username

    @JvmStatic
    fun getUUID(): String = getUUIDObj().toString()

    @JvmStatic
    fun getUUIDObj(): UUID = Client.getMinecraft().session.profile.id

    @JvmStatic
    fun getHP(): Float = getPlayer()?.health ?: 0f

    @JvmStatic
    fun isMoving(): Boolean = getPlayer()?.movementInput?.let { it.moveForward != 0F || it.moveStrafe != 0F } ?: false

    @JvmStatic
    fun isSneaking(): Boolean = getPlayer()?.isSneaking ?: false

    @JvmStatic
    fun isSprinting(): Boolean = getPlayer()?.isSprinting ?: false

    @JvmStatic
    fun isFlying(): Boolean = !(getPlayer()?.isPushedByWater ?: true)

    @JvmStatic
    fun lookingAt(): Any? {
        if (!World.isLoaded()) return null
        val obj = Client.getMinecraft().objectMouseOver ?: return null

        return when(obj.typeOfHit) {
            MovingObjectPosition.MovingObjectType.BLOCK -> {
                val block = Block(World.getBlockStateAt(obj.blockPos).block, obj.blockPos)

                if (block.mcBlock is BlockSign) Sign(block) else block
            }
            MovingObjectPosition.MovingObjectType.ENTITY -> Entity(obj.entityHit)
            else -> null
        }
    }

    @JvmStatic
    fun getActivePotionEffects(): List<PotionEffect> {
        return getPlayer()?.activePotionEffects?.map(::PotionEffect) ?: listOf()
    }

    @JvmStatic
    fun getContainer(): Inventory? = getPlayer()?.openContainer?.let(::Inventory)

    @JvmStatic
    fun getInventory(): Inventory? = getPlayer()?.inventory?.let(::Inventory)

    @JvmStatic
    fun asPlayerMP() = getPlayer()?.let(::PlayerMP)

    @JvmField
    val armor = object {
        fun getHelmet(): Item? = getInventory()?.getStackInSlot(39)

        fun getChestplate(): Item? = getInventory()?.getStackInSlot(38)

        fun getLeggings(): Item? = getInventory()?.getStackInSlot(37)

        fun getBoots(): Item? = getInventory()?.getStackInSlot(36)
    }
}