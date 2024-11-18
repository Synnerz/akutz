package com.github.synnerz.akutz.api.wrappers.world.block

import com.github.synnerz.akutz.api.wrappers.World
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.block.Block as MCBlock

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/world/block/Block.kt)
 */
open class Block(
    val mcBlock: MCBlock,
    val mcPos: BlockPos
) {
    val x: Int get() = mcPos.x
    val y: Int get() = mcPos.y
    val z: Int get() = mcPos.z

    fun getState(): IBlockState = World.getBlockStateAt(mcPos)

    fun getMetadata(): Int = mcBlock.getMetaFromState(getState())

    @JvmField
    val type = object {
        fun getID(): Int = MCBlock.getIdFromBlock(mcBlock)

        fun getRegistryName(): String = mcBlock.registryName

        fun getUnlocalizedName(): String = mcBlock.unlocalizedName

        fun getName(): String = mcBlock.localizedName

        fun getDefaultState(): IBlockState = mcBlock.defaultState

        fun getDefaultStateMetadata(): Int = mcBlock.getMetaFromState(getDefaultState())

        fun isTranslucent(): Boolean = mcBlock.isTranslucent

        override fun toString(): String = "BlockType{name=${getRegistryName()}}"
    }

    override fun toString() = "Block{type=${mcBlock.registryName}, x=$x, y=$y, z=$z}"
}