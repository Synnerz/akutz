package com.github.synnerz.akutz.api.wrappers

import com.github.synnerz.akutz.api.libs.ChatLib
import net.minecraft.scoreboard.ScorePlayerTeam

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/entity/Team.kt)
 */
class Team(
    val team: ScorePlayerTeam
) {
    fun getRegisteredName(): String = team.registeredName

    fun getName(): String = team.teamName

    fun setName(name: String) = apply {
        team.teamName = ChatLib.addColor(name)
    }

    fun getMembers(): List<String> = team.membershipCollection.toList()

    fun getPrefix(): String = team.colorPrefix

    fun setPrefix(prefix: String) = apply {
        team.setNamePrefix(ChatLib.addColor(prefix))
    }

    fun getSuffix(): String = team.colorSuffix

    fun setSuffix(suffix: String) = apply {
        team.setNameSuffix(ChatLib.addColor(suffix))
    }

    fun getFriendlyFire(): Boolean = team.allowFriendlyFire

    fun canSeeInvisibleTeammates(): Boolean = team.seeFriendlyInvisiblesEnabled

    fun getNameTagVisibility(): String = team.nameTagVisibility.internalName

    fun getDeathMessageVisibility(): String = team.deathMessageVisibility.internalName

    override fun toString(): String = "Team{name=\"${getName()}\", registeredName=\"${getRegisteredName()}\", members=\"${getMembers()}\"}"
}