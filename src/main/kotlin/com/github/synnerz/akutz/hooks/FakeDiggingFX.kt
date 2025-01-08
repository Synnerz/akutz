package com.github.synnerz.akutz.hooks

import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World

/**
 * * Currently only used by [EffectRendererHook] to make a "fake" entity fx so the player can
 * cancel and/or get the data of the digging particle entity
 */
class FakeDiggingFX(
    worldObj: World,
    x1: Double,
    y1: Double,
    z1: Double,
    x2: Double,
    y2: Double,
    z2: Double
) : EntityFX(worldObj, x1, y1, z1, x2, y2, z2)