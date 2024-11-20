package com.github.synnerz.akutz.api.wrappers.entity

import com.github.synnerz.akutz.api.objects.render.Color
import net.minecraft.client.particle.EntityFX

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/entity/Particle.kt)
 */
class Particle(
    val particleEntity: EntityFX
) : Entity(particleEntity) {
    fun setX(x: Double) = apply { particleEntity.setPosition(x, getY(), getZ()) }

    fun setY(y: Double) = apply { particleEntity.setPosition(getX(), y, getZ()) }

    fun setZ(z: Double) = apply { particleEntity.setPosition(getX(), getY(), z) }

    fun scale(scale: Float) = apply { particleEntity.multipleParticleScaleBy(scale) }

    fun multiplyVelocity(mult: Float) = apply { particleEntity.multiplyVelocity(mult) }

    fun setAlpha(alpha: Float) = apply { particleEntity.setAlphaF(alpha) }

    fun setColor(r: Float, g: Float, b: Float) = apply { particleEntity.setRBGColorF(r, g, b) }

    fun setColor(r: Float, g: Float, b: Float, a: Float) = apply {
        setColor(r, g, b)
        setAlpha(a)
    }

    fun setColor(color: Color) = apply {
        val r = color.getRf().toFloat()
        val g = color.getGf().toFloat()
        val b = color.getBf().toFloat()
        val a = color.getAf().toFloat()
        setColor(r, g, b, a)
    }

    fun getColor(): Color = Color.fromRGBA(
        particleEntity.redColorF.toDouble(),
        particleEntity.greenColorF.toDouble(),
        particleEntity.blueColorF.toDouble(),
        particleEntity.alpha.toDouble()
    )

    fun remove() = apply { particleEntity.setDead() }

    override fun toString(): String = particleEntity.toString()
}