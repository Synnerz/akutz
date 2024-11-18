package com.github.synnerz.akutz.api.wrappers.world

import com.github.synnerz.akutz.api.wrappers.entity.Entity
import com.github.synnerz.akutz.api.wrappers.entity.TileEntity
import net.minecraft.world.chunk.Chunk as MCChunk

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/world/Chunk.kt)
 */
class Chunk(val chunk: MCChunk) {
    fun getX(): Int = chunk.xPosition

    fun getZ(): Int = chunk.zPosition

    fun getMinBlockX(): Int = getX() * 16

    fun getMinBlockZ(): Int = getZ() * 16

    fun getAllEntities(): List<Entity> {
        return chunk.entityLists.toList().flatten().map(::Entity)
    }

    fun getAllEntitiesOfType(clazz: Class<*>): List<Entity> {
        return getAllEntities().filter { clazz.isInstance(it.entity) }
    }

    fun getAllTileEntities(): List<TileEntity> {
        return chunk.tileEntityMap.values.map(::TileEntity)
    }

    fun getAllTileEntitiesOfType(clazz: Class<*>): List<TileEntity> {
        return getAllTileEntities().filter { clazz.isInstance(it.tileEntity) }
    }

    fun toMC(): MCChunk = chunk
}