package com.github.synnerz.akutz.api.wrappers

import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraftforge.client.GuiIngameForge
import net.minecraft.scoreboard.Scoreboard as MCScoreboard
import net.minecraft.scoreboard.Score as MCScore

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/wrappers/Scoreboard.kt)
 */
object Scoreboard {
    private var dirty = true
    private var scoreboardNames = mutableListOf<Score>()
    private var scoreboardTitle = ""

    @JvmStatic
    fun getScoreboard(): MCScoreboard? = World.getWorld()?.scoreboard

    @JvmStatic
    fun getSidebar(): ScoreObjective? = getScoreboard()?.getObjectiveInDisplaySlot(1)

    @JvmStatic
    fun getScoreboardTitle(): String = getTitle()

    @JvmStatic
    fun getTitle(): String {
        if (dirty) {
            updateNames()
            dirty = false
        }

        return scoreboardTitle
    }

    @JvmStatic
    fun setTitle(str: String) {
        getSidebar()?.displayName = str
    }

    @JvmOverloads
    @JvmStatic
    fun getLines(descending: Boolean = true): List<Score> {
        if (dirty) {
            updateNames()
            dirty = false
        }

        return if (descending) scoreboardNames else scoreboardNames.asReversed()
    }

    @JvmStatic
    fun getLineByIndex(index: Int): Score = getLines()[index]

    @JvmStatic
    fun getLinesByScore(score: Int): List<Score> = getLines().filter { it.getPoints() == score }

    @JvmOverloads
    @JvmStatic
    fun setLine(score: Int, line: String, override: Boolean = false) {
        val scoreboard = getScoreboard() ?: return
        val sidebarObjectives = getSidebar() ?: return

        val scores: Collection<MCScore> = scoreboard.getSortedScores(sidebarObjectives)

        if (override) {
            scores.filter { it.scorePoints == score }.forEach {
                scoreboard.removeObjectiveFromEntity(it.playerName, sidebarObjectives)
            }
        }

        val theScore = scoreboard.getValueFromObjective(line, sidebarObjectives)!!

        theScore.scorePoints = score
    }

    @JvmStatic
    fun setShouldRender(shouldRender: Boolean) {
        GuiIngameForge.renderObjective = shouldRender
    }

    @JvmStatic
    fun getShouldRender(): Boolean = GuiIngameForge.renderObjective

    @JvmStatic
    fun markDirty() {
        dirty = true
    }

    private fun updateNames() {
        scoreboardNames.clear()
        scoreboardTitle = ""

        val scoreboard = getScoreboard() ?: return
        val sidebarObjective = getSidebar() ?: return
        scoreboardTitle = sidebarObjective.displayName

        val scores: Collection<MCScore> = scoreboard.getSortedScores(sidebarObjective)

        scoreboardNames = scores.map(::Score).toMutableList()
    }

    class Score(val score: MCScore) {
        fun getPoints(): Int = score.scorePoints

        fun getName(): String = ScorePlayerTeam.formatPlayerName(
            getScoreboard()!!.getPlayersTeam(score.playerName),
            score.playerName
        )

        override fun toString(): String = getName()
    }
}