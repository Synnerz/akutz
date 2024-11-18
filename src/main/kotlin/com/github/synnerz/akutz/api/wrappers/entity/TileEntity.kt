package com.github.synnerz.akutz.api.wrappers.entity

import net.minecraft.tileentity.TileEntity as MCTileEntity

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/entity/TileEntity.kt)
 */
// TODO: finish once Block wrapper is done
class TileEntity(val tileEntity: MCTileEntity) {
    fun toMC(): MCTileEntity = tileEntity
}