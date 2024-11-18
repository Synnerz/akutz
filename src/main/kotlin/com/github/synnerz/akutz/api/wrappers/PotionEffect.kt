package com.github.synnerz.akutz.api.wrappers

import net.minecraft.client.resources.I18n
import net.minecraft.potion.PotionEffect as MCPotionEffect

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/world/PotionEffect.kt)
 */
class PotionEffect(
    val effect: MCPotionEffect
) {
    fun getName(): String = effect.effectName

    fun getLocalizedName(): String = I18n.format(getName(), "%s")

    fun getAmplifier(): Int = effect.amplifier

    fun getDuration(): Int = effect.duration

    fun getID(): Int = effect.potionID

    fun isDurationMax(): Boolean = effect.isPotionDurationMax

    fun showsParticles(): Boolean = effect.isShowParticles

    override fun toString(): String = effect.toString()
}