package com.github.synnerz.akutz.api.wrappers.entity

import com.github.synnerz.akutz.api.libs.ChatLib
import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.api.wrappers.Team
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.ChatComponentText

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/entity/EntityLivingBase.kt)
 */
class PlayerMP(
    val playerMP: EntityPlayer
) : EntityLivingBase(playerMP) {
    fun isSpectator(): Boolean = playerMP.isSpectator

     fun getTeam(): Team? = getPlayerInfo()?.playerTeam?.let(::Team)

    fun getDisplayName(): String = getPlayerName(getPlayerInfo())

    fun setDisplayName(name: String) = apply {
        getPlayerInfo()?.displayName = ChatComponentText(ChatLib.addColor(name))
    }

    // TODO
    // fun draw()

    override fun getName(): String = playerMP.name

    private fun getPlayerName(networkPlayerInfoIn: NetworkPlayerInfo?): String {
        return networkPlayerInfoIn?.displayName?.formattedText
            ?: ScorePlayerTeam.formatPlayerName(
                networkPlayerInfoIn?.playerTeam,
                networkPlayerInfoIn?.gameProfile?.name
            ) ?: ""
    }

    private fun getPlayerInfo(): NetworkPlayerInfo? = Client.getConnection()?.getPlayerInfo(playerMP.uniqueID)

    override fun toString(): String = "PlayerMP{name=\"${getName()}\", entityLivingBase=\"${super.toString()}\"}"
}