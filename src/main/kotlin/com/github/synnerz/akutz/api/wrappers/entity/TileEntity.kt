package com.github.synnerz.akutz.api.wrappers.entity

import net.minecraft.tileentity.TileEntity as MCTileEntity

// TODO: finish once Block wrapper is done
class TileEntity(val tileEntity: MCTileEntity) {
    fun toMC(): MCTileEntity = tileEntity
}