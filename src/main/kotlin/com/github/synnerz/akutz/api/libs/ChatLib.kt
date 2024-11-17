package com.github.synnerz.akutz.api.libs

import com.github.synnerz.akutz.api.wrappers.Player
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/libs/ChatLib.kt)
 */
object ChatLib {
    private val removeCodesRegex = "[\\u00a7&][0-9a-fk-or]".toRegex()
    private val addCodesRegex = "(?<!\\\\\\\\)&(?![^0-9a-fk-or]|\$)".toRegex()
    private val replaceCodesRegex = "\\u00a7(?![^0-9a-fk-or]|\$)".toRegex()

    @JvmStatic
    fun chat(msg: String) {
        // TODO: we'll probably implement a Wrapper for things like this later on don't forget to change
        Player.getPlayer()?.addChatMessage(ChatComponentText(msg))
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
        return str.replace(addCodesRegex, "\\u00a7")
    }

    @JvmOverloads
    @JvmStatic
    fun getChatMessage(event: ClientChatReceivedEvent, formatted: Boolean = false): String =
        if (formatted) replaceFormatting(event.message.formattedText) else event.message.unformattedText
}