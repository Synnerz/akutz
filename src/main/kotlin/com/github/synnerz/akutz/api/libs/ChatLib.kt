package com.github.synnerz.akutz.api.libs

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.api.wrappers.Player
import com.github.synnerz.akutz.api.wrappers.message.Message
import com.github.synnerz.akutz.api.wrappers.message.TextComponent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ChatLine
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/libs/ChatLib.kt)
 */
object ChatLib {
    private val removeCodesRegex = "[\\u00a7&][0-9a-fk-or]".toRegex()
    private val addCodesRegex = "(?<!\\\\\\\\)&(?=[0-9a-fk-or])".toRegex()
    private val replaceCodesRegex = "\\u00a7(?=[0-9a-fk-or])".toRegex()

    @JvmStatic
    fun chat(msg: Any) {
        when (msg) {
            is String -> Message(msg).chat()
            is Message -> msg.chat()
            is TextComponent -> msg.chat()
            else -> Message(msg.toString()).chat()
        }
    }

    @JvmStatic
    fun actionBar(msg: Any) {
        when (msg) {
            is String -> Message(msg).actionBar()
            is Message -> msg.actionBar()
            is TextComponent -> msg.actionBar()
            else -> Message(msg.toString()).actionBar()
        }
    }

    @JvmStatic
    fun simulateChat(msg: Any) {
        when (msg) {
            is String -> Message(msg).setRecursive(true).chat()
            is Message -> msg.setRecursive(true).chat()
            is TextComponent -> Message(msg).setRecursive(true).chat()
            else -> Message(msg.toString()).setRecursive(true).chat()
        }
    }

    @JvmStatic
    fun say(msg: String) {
        Player.getPlayer()?.sendChatMessage(msg)
    }

    @JvmOverloads
    @JvmStatic
    fun command(command: String, clientSide: Boolean = false) {
        if (clientSide) ClientCommandHandler.instance.executeCommand(Player.getPlayer()!!, "/$command")
        else say("/$command")
    }

    @JvmStatic
    fun removeFormatting(str: String): String {
        return str.replace(removeCodesRegex, "")
    }

    @JvmStatic
    fun replaceFormatting(str: String): String {
        return str.replace(replaceCodesRegex, "&")
    }

    @JvmStatic
    fun addColor(str: String): String {
        return str.replace(addCodesRegex, "\u00a7")
    }

    @JvmOverloads
    @JvmStatic
    fun getChatMessage(event: ClientChatReceivedEvent, formatted: Boolean = false): String =
        if (formatted) replaceFormatting(event.message.formattedText) else event.message.unformattedText

    @JvmStatic
    fun relativeTimeToString(t: Double): String {
        var time = t / 1000
        if (time < 60) return "${time.toInt()}s"
        if (time < 60 * 5) {
            val minutes = (time / 60).toInt()
            val seconds = (time % 60).toInt()
            return "${minutes}m ${seconds}s"
        }
        time /= 60
        if (time < 60) return "${time.toInt()}m"
        if (time < 60 * 5) {
            val hours = (time / 60).toInt()
            val minutes = (time % 60).toInt()
            return "${hours}h ${minutes}m"
        }
        time /= 60
        if (time < 24) return "${time.toInt()}h"
        if (time < 48) {
            val hours = (time - 24).toInt()
            return "1d ${hours}h"
        }
        time /= 24
        if (time < 7) return "${time.toInt()}d"
        val years = time / 365
        time %= 365
        val weeks = time / 7
        time %= 7

        val yearStr = if (years >= 1) "${years.toInt()}y " else ""
        val weekStr = if (weeks >= 1) "${weeks.toInt()}w " else ""
        val dayStr = if (time >= 1) "${time.toInt()}d" else ""

        return "$yearStr$weekStr$dayStr".trim()
    }

    @JvmStatic
    @JvmOverloads
    fun addCommas(value: Number, decimalPlaces: Int = 0): String = "%,.${decimalPlaces}f".format(value)

    @JvmStatic
    @JvmOverloads
    fun colorForNumber(value: Double, max: Double = 1.0): String {
        if (value > max * 0.75) return "§2"
        if (value > max * 0.50) return "§e"
        if (value > max * 0.25) return "§6"
        return "§4"
    }

    @JvmStatic
    fun getChatWidth() = Client.getChatGui()?.chatWidth ?: 0

    @JvmOverloads
    @JvmStatic
    fun getChatBreak(separator: String = "-"): String {
        val len = Renderer.getStringWidth(separator) * Minecraft.getMinecraft().gameSettings.chatScale
        val times = getChatWidth() / len

        return separator.repeat(times.toInt())
    }

    @JvmStatic
    fun centerMessage(message: Any): String = when (message) {
        // TODO:
        // is Message ->
        // is TextComponent ->
        is String -> {
            val scale = Minecraft.getMinecraft().gameSettings.chatScale
            val msgWidth = Renderer.getStringWidth(message) * scale
            val margins = getChatWidth() - msgWidth
            val count = margins / Renderer.getStringWidth(" ") / scale
            " ".repeat(count.toInt())
        }

        else -> centerMessage(message.toString())
    }

    @JvmStatic
    fun clearChat(vararg chatLineIds: Int) {
        if (chatLineIds.isEmpty()) {
            Client.getChatGui()?.clearChatMessages()
            return
        }

        chatLineIds.forEach { Client.getChatGui()?.deleteChatLine(it) }
    }

    @JvmStatic
    fun editMessage(replace: String, vararg repl: Message) {
        editChatLineList({
                removeFormatting(it.getChatMessage().unformattedText) == replace
            },
            *repl
        )
    }

    @JvmStatic
    fun editMessage(replace: Message, vararg repl: Message) {
        editChatLineList({
                replace.getChatMessage().formattedText == it.getChatMessage().formattedText.substring(4)
            },
            *repl
        )
    }

    @JvmStatic
    fun editMessage(replace: Int, vararg repl: Message) {
        editChatLineList({
                it.getChatLineId() == replace
            },
            *repl
        )
    }

    private fun editChatLineList(
        toReplace: (Message) -> Boolean,
        vararg replacements: Message
    ) {
        val iter = Client.getChatGui()!!.chatLines.listIterator()

        while (iter.hasNext()) {
            val chatLine = iter.next()
            val result = toReplace(Message(chatLine.chatComponent).setChatLineId(chatLine.chatLineID))
            if (!result) continue

            iter.remove()
            replacements.map {
                val lineId = if (it.getChatLineId() == -1) 0 else it.getChatLineId()

                ChatLine(chatLine.updatedCounter, it.getChatMessage(), lineId)
            }.forEach(iter::add)
        }

        Client.getChatGui()!!.refreshChat()
    }

    @JvmOverloads
    @JvmStatic
    fun addToMessageHistory(index: Int = -1, msg: String) {
        val messages = Client.getChatGui()!!.sentMessages
        if (index == -1) messages.add(msg)
        else messages.add(index, msg)
    }

    @JvmStatic
    fun deleteMessage(vararg delete: String) {
        deleteChatLineList { msg ->
            val unformatted = removeFormatting(msg.getChatMessage().unformattedText)
            delete.any { it == unformatted }
        }
    }

    @JvmStatic
    fun deleteMessage(vararg delete: Message) {
        deleteChatLineList { msg ->
            val comp = msg.getChatMessage().formattedText.substring(4)
            delete.any { it.getChatMessage().formattedText == comp }
        }
    }

    @JvmStatic
    fun deleteMessage(vararg delete: Int) {
        deleteChatLineList { msg ->
            val compId = msg.getChatLineId()
            delete.any { it == compId }
        }
    }

    // Add a way for the user to be able to make their own custom
    // logic, this is to avoid us having to remove certain things.
    // for example patcher's compact mode messages
    @JvmStatic
    fun deleteMessage(method: (Message) -> Boolean) {
        deleteChatLineList(method)
    }

    private fun deleteChatLineList(toDelete: (Message) -> Boolean) {
        val iter = Client.getChatGui()!!.chatLines.listIterator()
        val start = Client.getSystemTime()

        while (iter.hasNext()) {
            if (Client.getSystemTime() - start > 50) break

            val chatLine = iter.next()

            if (toDelete(Message(chatLine.chatComponent).setChatLineId(chatLine.chatLineID)))
                iter.remove()
        }

        Client.getChatGui()!!.refreshChat()
    }

    // TODO: might want to make regex matching for #editMessage & #deleteMessage
}