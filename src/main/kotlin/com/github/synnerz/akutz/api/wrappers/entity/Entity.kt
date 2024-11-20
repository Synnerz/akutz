package com.github.synnerz.akutz.api.wrappers.entity

import com.github.synnerz.akutz.api.wrappers.world.Chunk
import net.minecraft.world.World
import java.util.UUID
import net.minecraft.entity.Entity as MCEntity

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/entity/Entity.kt)
 */
open class Entity(val entity: MCEntity) {
    fun getX(): Double = entity.posX

    fun getY(): Double = entity.posY

    fun getZ(): Double = entity.posZ

    fun getLastX(): Double = entity.lastTickPosX

    fun getLastY(): Double = entity.lastTickPosY

    fun getLastZ(): Double = entity.lastTickPosZ

    fun isDead(): Boolean = entity.isDead

    fun getWidth(): Float = entity.width

    fun getHeight(): Float = entity.height

    fun getEyeHeight(): Float = entity.eyeHeight

    open fun getName(): String = entity.name

    fun getUUID(): String = getUUIDObj().toString()

    fun getUUIDObj(): UUID = entity.uniqueID

    fun distanceTo(other: Entity): Float = distanceTo(other.entity)

    fun distanceTo(other: MCEntity): Float = entity.getDistanceToEntity(other)

    // TODO: blockpos distanceTo

    fun distanceTo(x: Float, y: Float, z: Float): Float = entity.getDistance(
        x.toDouble(), y.toDouble(), z.toDouble()
    ).toFloat()

    fun distanceTo(x: Double, y: Double, z: Double): Double = entity.getDistance(
        x, y, z
    )

    fun isOnGround(): Boolean = entity.onGround

    fun isCollided(): Boolean = entity.isCollided

    fun getTicksExisted(): Int = entity.ticksExisted

    fun isSneaking(): Boolean = entity.isSneaking

    fun isSprinting(): Boolean = entity.isSprinting

    fun getWorld(): World = entity.entityWorld

    fun getChunk(): Chunk = Chunk(
        getWorld().getChunkFromChunkCoords(entity.chunkCoordX, entity.chunkCoordZ)
    )

    fun getID(): Int = entity.entityId

    fun toMC(): MCEntity = entity
}