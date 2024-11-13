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

    object border {
        @JvmStatic
        fun getCenterX(): Double = getWorld()!!.worldBorder.centerX

        @JvmStatic
        fun getCenterZ(): Double = getWorld()!!.worldBorder.centerZ
    }

    object spawn {
        @JvmStatic
        fun getX(): Int = getWorld()!!.spawnPoint.x

        @JvmStatic
        fun getY(): Int = getWorld()!!.spawnPoint.y

        @JvmStatic
        fun getZ(): Int = getWorld()!!.spawnPoint.z
    }
}