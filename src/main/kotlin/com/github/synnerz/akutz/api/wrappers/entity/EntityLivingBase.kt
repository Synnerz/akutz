package com.github.synnerz.akutz.api.wrappers.entity

import com.github.synnerz.akutz.api.wrappers.PotionEffect
import net.minecraft.entity.EntityLivingBase as MCEntityLivingBase
import net.minecraft.potion.Potion
import net.minecraft.entity.Entity as MCEntity

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/entity/EntityLivingBase.kt)
 */
open class EntityLivingBase(
    val entityLivingBase: MCEntityLivingBase
) : Entity(entityLivingBase) {
    fun getActivePotionEffects(): List<PotionEffect> = entityLivingBase.activePotionEffects.map(::PotionEffect)

    fun canSeeEntity(other: MCEntity) = entityLivingBase.canEntityBeSeen(other)

    fun canSeeEntity(other: Entity) = entityLivingBase.canEntityBeSeen(other.entity)

    fun getHP(): Float = entityLivingBase.health

    fun getMaxHP(): Float = entityLivingBase.maxHealth

    fun isPotionActive(id: Int): Boolean = entityLivingBase.isPotionActive(id)

    fun isPotionActive(potion: Potion): Boolean = isPotionActive(potion.id)

    fun isPotionActive(potionEffect: PotionEffect): Boolean = isPotionActive(potionEffect.getID())

    override fun toString(): String = "EntityLivingBase{name=\"${getName()}\", entity=\"${super.toString()}\"}"
}