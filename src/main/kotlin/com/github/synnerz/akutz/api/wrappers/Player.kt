package com.github.synnerz.akutz.api.wrappers

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/Player.kt)
 */
object Player {
    @JvmStatic
    fun getPlayer(): EntityPlayerSP? = Minecraft.getMinecraft().thePlayer

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
    fun getName(): String = Minecraft.getMinecraft().session.username
}