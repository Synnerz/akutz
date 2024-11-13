package com.github.synnerz.akutz.api.libs

import com.github.synnerz.akutz.api.wrappers.Player
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/libs/ChatLib.kt)
 */
object ChatLib {
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
        say("/$command")
    }
}