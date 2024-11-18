package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.World
import com.github.synnerz.akutz.api.wrappers.entity.Entity
import net.minecraft.util.BlockPos
import net.minecraft.entity.Entity as MCEntity

object PlayerControllerMPHook {
    fun triggerAttackEntity(entity: MCEntity) {
        EventType.AttackEntity.triggerAll(Entity(entity))
    }

    fun triggerBlocKBreak(blockPos: BlockPos) {
        EventType.BlockBreak.triggerAll(World.getBlockAt(blockPos))
    }
}