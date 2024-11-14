package com.github.synnerz.akutz.api.wrappers

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/World.kt)
 */
object World {
    @JvmStatic
    fun getWorld(): WorldClient? = Minecraft.getMinecraft().theWorld

    @JvmStatic
    fun isLoaded(): Boolean = getWorld() != null

    @JvmStatic
    fun getTime(): Long = getWorld()?.worldTime ?: -1L

    @JvmField
    val border = object {
        fun getCenterX(): Double = getWorld()!!.worldBorder.centerX

        fun getCenterZ(): Double = getWorld()!!.worldBorder.centerZ
    }

    @JvmField
    val spawn = object {
        fun getX(): Int = getWorld()!!.spawnPoint.x

        fun getY(): Int = getWorld()!!.spawnPoint.y

        fun getZ(): Int = getWorld()!!.spawnPoint.z
    }
}