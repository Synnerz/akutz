package com.github.synnerz.akutz.api.wrappers.entity

import com.github.synnerz.akutz.api.wrappers.world.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.tileentity.TileEntity as MCTileEntity

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/entity/TileEntity.kt)
 */
class TileEntity(val tileEntity: MCTileEntity) {
    fun getX(): Int = tileEntity.pos.x

    fun getY(): Int = tileEntity.pos.y

    fun getZ(): Int = tileEntity.pos.z

    fun getBlock(): Block = Block(tileEntity.blockType, tileEntity.pos)

    fun getBlockPos(): BlockPos = tileEntity.pos

    fun getMetadata(): Int = tileEntity.blockMetadata

    fun toMC(): MCTileEntity = tileEntity

    override fun toString(): String = "TileEntity{x=${getX()}, y=${getY()}, z=${getZ()}, block=${getBlock()}}"
}