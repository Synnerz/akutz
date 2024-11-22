package com.github.synnerz.akutz.api.wrappers

import com.github.synnerz.akutz.api.wrappers.entity.Entity
import com.github.synnerz.akutz.api.wrappers.entity.TileEntity
import com.github.synnerz.akutz.api.wrappers.world.Chunk
import com.github.synnerz.akutz.api.wrappers.world.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.entity.Entity as MCEntity

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/World.kt)
 */
object World {
    @JvmStatic
    fun getWorld(): WorldClient? = Client.getMinecraft().theWorld

    @JvmStatic
    fun isLoaded(): Boolean = getWorld() != null

    @JvmStatic
    fun getTime(): Long = getWorld()?.worldTime ?: -1L

    @JvmStatic
    fun getBlockAt(x: Number, y: Number, z: Number): Block = getBlockAt(
        BlockPos(x.toDouble(), y.toDouble(), z.toDouble())
    )

    @JvmStatic
    fun getBlockAt(pos: BlockPos): Block {
        return Block(getBlockStateAt(pos).block, pos)
    }

    @JvmStatic
    fun getBlockStateAt(pos: BlockPos): IBlockState {
        return getWorld()!!.getBlockState(pos)
    }

    @JvmStatic
    fun getChunkAt(x: Int, y: Int, z: Int): Chunk = Chunk(
        getWorld()!!.getChunkFromBlockCoords(BlockPos(x, y, z))
    )

    @JvmStatic
    fun getAllEntities(): List<Entity> {
        return getWorld()?.loadedEntityList?.map(::Entity) ?: listOf()
    }

    @JvmStatic
    fun getAllEntitiesOfType(clazz: Class<*>): List<Entity> {
        return getAllEntities().filter { clazz.isInstance(it.entity) }
    }

    @JvmStatic
    fun getAllTileEntities(): List<TileEntity> {
        return getWorld()?.loadedTileEntityList?.map(::TileEntity) ?: listOf()
    }

    @JvmStatic
    fun getAllTileEntitiesOfType(clazz: Class<*>): List<TileEntity> {
        return getAllTileEntities().filter { clazz.isInstance(it.tileEntity) }
    }

    @JvmStatic
    fun <T : MCEntity> getAllEntitiesInAABB(clazz: Class<T>, bb: AxisAlignedBB): List<T>? {
        return getWorld()?.getEntitiesWithinAABB(clazz, bb)
    }

    @JvmStatic
    fun <T : MCEntity> getAllEntitiesInAABB(clazz: Class<T>, vararg bb: Double): List<T>? {
        val aa = AxisAlignedBB(
            bb[0], bb[1], bb[2],
            bb[3], bb[4], bb[5]
        )
        return getAllEntitiesInAABB(clazz, aa)
    }

    @JvmStatic
    fun <T : MCEntity> getAllEntitiesInAABB(clazz: Class<T>, min: BlockPos, max: BlockPos): List<T>? {
        val aa = AxisAlignedBB(min, max)
        return getAllEntitiesInAABB(clazz, aa)
    }

    @JvmStatic
    fun <T : MCEntity> getEntitiesWithinAABB(clazz: Class<T>, bb: AxisAlignedBB): List<T>? =
        getAllEntitiesInAABB(clazz, bb)

    @JvmStatic
    fun <T : MCEntity> getEntitiesWithinAABB(clazz: Class<T>, vararg bb: Double): List<T>? =
        getAllEntitiesInAABB(clazz, *bb)

    @JvmStatic
    fun <T : MCEntity> getEntitiesWithinAABB(clazz: Class<T>, min: BlockPos, max: BlockPos): List<T>? =
        getAllEntitiesInAABB(clazz, min, max)

    @JvmStatic
    fun playSound(name: String, vol: Float, pitch: Float) {
        Client.scheduleTask { Player.getPlayer()?.playSound(name, vol, pitch) }
    }

    @JvmStatic
    fun playRecord(name: String, x: Double, y: Double, z: Double) {
        Client.scheduleTask { getWorld()?.playRecord(BlockPos(x, y, z), name) }
    }

    @JvmStatic
    fun stopAllSounds() {
        Client.getMinecraft().soundHandler.stopSounds()
    }

    @JvmField
    val border = object {
        fun getCenterX(): Double = getWorld()!!.worldBorder.centerX

        fun getCenterZ(): Double = getWorld()!!.worldBorder.centerZ
    }

    @JvmField
    val spawn = object {
        fun getX(): Int = getWorld()!!.spawnPoint.x

        fun getY(): Int = getWorld()!!.spawnPoint.y

        fun getZ(): Int = getWorld()!!.spawnPoint.z
    }
}