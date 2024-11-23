package com.github.synnerz.akutz.api.wrappers

import com.github.synnerz.akutz.api.wrappers.entity.Entity
import com.github.synnerz.akutz.api.wrappers.entity.TileEntity
import com.github.synnerz.akutz.api.wrappers.world.Chunk
import com.github.synnerz.akutz.api.wrappers.world.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import kotlin.math.max
import kotlin.math.min
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

    // Modified version of Mojang's BlockPos method
    // this is to better fit the need of having to wrap
    // the position into a wrapped block
    @JvmStatic
    fun getAllBlocksInBox(start: BlockPos, end: BlockPos): ArrayList<Block> {
        val list = ArrayList<Block>()
        val blockpos = BlockPos(
            min(start.x.toDouble(), end.x.toDouble()),
            min(start.y.toDouble(), end.y.toDouble()),
            min(start.z.toDouble(), end.z.toDouble())
        )
        val blockpos1 = BlockPos(
            max(start.x.toDouble(), end.x.toDouble()),
            max(start.y.toDouble(), end.y.toDouble()),
            max(start.z.toDouble(), end.z.toDouble())
        )
        var lastReturned: BlockPos = blockpos

        while (lastReturned != blockpos1) {
            var x = lastReturned.x
            var y = lastReturned.y
            var z = lastReturned.z

            if (x < blockpos1.x) x++
            else if (y < blockpos1.y) {
                x = blockpos.x
                y++
            }
            else if (z < blockpos1.z) {
                x = blockpos.x
                y = blockpos.y
                z++
            }

            lastReturned = BlockPos(x, y, z)
            list.add(getBlockAt(lastReturned))
        }

        return list
    }

    @JvmStatic
    fun getBlocksIn(start: BlockPos, end: BlockPos): ArrayList<Block> = getAllBlocksInBox(start, end)

    @JvmStatic
    fun getBlocksIn(start: ArrayList<Double>, end: ArrayList<Double>): ArrayList<Block> = getBlocksIn(
        BlockPos(start[0], start[1], start[2]),
        BlockPos(end[0], end[1], end[2])
    )

    @JvmStatic
    fun getBlocksIn(vararg corners: Double): ArrayList<Block> = getBlocksIn(
        BlockPos(corners[0], corners[1], corners[2]),
        BlockPos(corners[3], corners[4], corners[5])
    )

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