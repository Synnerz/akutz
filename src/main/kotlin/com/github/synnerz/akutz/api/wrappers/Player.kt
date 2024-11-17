package com.github.synnerz.akutz.api.wrappers

import net.minecraft.client.entity.EntityPlayerSP
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
}