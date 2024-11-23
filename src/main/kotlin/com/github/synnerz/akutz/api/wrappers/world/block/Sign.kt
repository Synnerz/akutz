package com.github.synnerz.akutz.api.wrappers.world.block

import com.github.synnerz.akutz.api.wrappers.World
import com.github.synnerz.akutz.api.wrappers.message.Message
import net.minecraft.tileentity.TileEntitySign

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/world/block/Sign.kt)
 */
class Sign(
    block: Block
) : Block(block.mcBlock, block.mcPos) {
    val sign: TileEntitySign = World.getWorld()!!.getTileEntity(block.mcPos) as TileEntitySign

    fun getLines(): List<Message> = sign.signText.map { it?.let(::Message) ?: Message("") }

    fun getFormattedLines(): List<String> = sign.signText.map { it?.formattedText ?: "" }

    fun getUnformattedLines(): List<String> = sign.signText.map { it?.unformattedText ?: "" }

    override fun toString(): String = "Sign{lines=TODO, name=${mcBlock.registryName}, x=$x, y=$y, z=$z}"
}