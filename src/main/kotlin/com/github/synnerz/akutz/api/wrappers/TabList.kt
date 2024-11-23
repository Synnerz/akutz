package com.github.synnerz.akutz.api.wrappers

import com.github.synnerz.akutz.api.wrappers.message.Message
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.scoreboard.Score as MCScore
import net.minecraft.util.IChatComponent
import net.minecraft.world.WorldSettings

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/TabList.kt)
 */
object TabList {
    private val playerComparator = Ordering.from(PlayerComparator())

    @JvmStatic
    fun getNamesByObjectives(): List<String> {
        val scoreboard = Scoreboard.getScoreboard() ?: return emptyList()
        val sidebarObjectives = scoreboard.getObjectiveInDisplaySlot(0) ?: return emptyList()
        val scores: Collection<MCScore> = scoreboard.getSortedScores(sidebarObjectives)

        return scores.map {
            val team = scoreboard.getPlayersTeam(it.playerName)
            ScorePlayerTeam.formatPlayerName(team, it.playerName)
        }
    }

    @JvmStatic
    fun getNames(): List<String> {
        if (Client.getTabGui() == null) return listOf()

        val playerList = playerComparator.sortedCopy(Player.getPlayer()!!.sendQueue.playerInfoMap)

        return playerList.map(Client.getTabGui()!!::getPlayerName)
    }

    @JvmStatic
    fun getUnformattedNames(): List<String> {
        if (Player.getPlayer() == null) return listOf()

        return Client.getConnection()?.playerInfoMap?.let(playerComparator::sortedCopy)?.map { it.gameProfile.name } ?: emptyList()
    }

    @JvmStatic
    fun getHeaderMessage() = Client.getTabGui()?.header?.let(::Message)

    @JvmStatic
    fun getHeader() = Client.getTabGui()?.header?.formattedText

    @JvmStatic
    fun setHeader(header: Any?) {
        when (header) {
            is String -> Client.getTabGui()?.header = Message(header).getChatMessage()
            is Message -> Client.getTabGui()?.header = header.getChatMessage()
            is IChatComponent -> Client.getTabGui()?.header = header
            null -> Client.getTabGui()?.header = header
        }
    }

    @JvmStatic
    fun clearHeader() = setHeader(null)

    @JvmStatic
    fun getFooterMessage() = Client.getTabGui()?.footer?.let(::Message)

    @JvmStatic
    fun getFooter() = Client.getTabGui()?.footer?.formattedText

    @JvmStatic
    fun setFooter(footer: Any?) {
        when (footer) {
            is String -> Client.getTabGui()?.footer = Message(footer).getChatMessage()
            is Message -> Client.getTabGui()?.footer = footer.getChatMessage()
            is IChatComponent -> Client.getTabGui()?.footer = footer
            null -> Client.getTabGui()?.footer = footer
        }
    }

    @JvmStatic
    fun clearFooter() = setFooter(null)

    internal class PlayerComparator internal constructor() : Comparator<NetworkPlayerInfo> {
        override fun compare(playerOne: NetworkPlayerInfo, playerTwo: NetworkPlayerInfo): Int {
            val teamOne = playerOne.playerTeam
            val teamTwo = playerTwo.playerTeam

            return ComparisonChain
                .start()
                .compareTrueFirst(
                    playerOne.gameType != WorldSettings.GameType.SPECTATOR,
                    playerTwo.gameType != WorldSettings.GameType.SPECTATOR
                ).compare(
                    teamOne?.registeredName ?: "",
                    teamTwo?.registeredName ?: ""
                ).compare(
                    playerOne.gameProfile.name,
                    playerTwo.gameProfile.name
                ).result()
        }
    }
}