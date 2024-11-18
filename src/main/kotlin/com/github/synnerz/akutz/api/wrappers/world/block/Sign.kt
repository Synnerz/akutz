package com.github.synnerz.akutz.api.wrappers.world.block

import com.github.synnerz.akutz.api.wrappers.World
import net.minecraft.tileentity.TileEntitySign

class Sign(
    block: Block
) : Block(block.mcBlock, block.mcPos) {
    val sign: TileEntitySign = World.getWorld()!!.getTileEntity(block.mcPos) as TileEntitySign

    // TODO: whenever Message wrapper is done
    // fun getLines()

    fun getFormattedLines(): List<String> = sign.signText.map { it?.formattedText ?: "" }

    fun getUnformattedLines(): List<String> = sign.signText.map { it?.unformattedText ?: "" }

    override fun toString(): String = "Sign{lines=TODO, name=${mcBlock.registryName}, x=$x, y=$y, z=$z}"
}