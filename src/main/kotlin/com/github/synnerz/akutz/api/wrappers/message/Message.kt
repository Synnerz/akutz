package com.github.synnerz.akutz.api.wrappers.message

import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.api.wrappers.Player
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/objects/message/Message.kt)
 */
class Message {
    private lateinit var chatMessage: IChatComponent
    private val messageParts = mutableListOf<TextComponent>()
    private var chatLineId = -1
    private var recursive = false
    private var formatted = true

    constructor(event: ClientChatReceivedEvent) : this(event.message)

    constructor(comp: IChatComponent) {
        if (comp.siblings.isEmpty()) {
            if (comp !is ChatComponentTranslation) {
                messageParts.add(TextComponent(comp))
                return
            }

            comp.forEach {
                if (it.siblings.isEmpty()) {
                    messageParts.add(TextComponent(it))
                }
            }
        } else {
            val formattedText = comp.formattedText

            val firstComponent = ChatComponentText(
                formattedText.substring(0, formattedText.indexOf(comp.siblings[0].formattedText))
            ).apply { chatStyle = comp.chatStyle }

            messageParts.add(TextComponent(firstComponent))
            messageParts.addAll(comp.siblings.map(::TextComponent))
        }
    }

    constructor(parts: ArrayList<Any>) {
        this.messageParts.addAll(parts.map {
            when (it) {
                is String -> TextComponent(it)
                is TextComponent -> it
                // TODO: add method to item
                // is Item -> it.getChatComponent()
                else -> return
            }
        })
    }

    constructor(vararg parts: Any) : this(ArrayList(parts.asList()))

    fun getChatMessage(): IChatComponent {
        parseMessage()
        return chatMessage
    }

    fun getFormattedText(): String = getChatMessage().formattedText

    fun getUnformattedText(): String = getChatMessage().unformattedText

    fun getMessageParts(): List<TextComponent> = messageParts

    fun getChatLineId(): Int = chatLineId

    fun setChatLineId(id: Int) = apply {
        chatLineId = id
    }

    fun isRecursive(): Boolean = recursive

    fun setRecursive(bool: Boolean) = apply {
        recursive = bool
    }

    fun isFormatted(): Boolean = formatted

    fun setFormatted(bool: Boolean) = apply {
        formatted = bool
    }

    fun setTextComponent(index: Int, comp: Any) = apply {
        when (comp) {
            is String -> messageParts[index] = TextComponent(comp)
            is TextComponent -> messageParts[index] = comp
        }
    }

    fun addTextComponent(comp: Any) = apply {
        when (comp) {
            is String -> messageParts.add(TextComponent(comp))
            is TextComponent -> messageParts.add(comp)
        }
    }

    fun addTextComponent(index: Int, comp: Any) = apply {
        when (comp) {
            is String -> messageParts.add(index, TextComponent(comp))
            is TextComponent -> messageParts.add(index, comp)
        }
    }

    fun copy(): Message {
        val copy = Message(messageParts)
            .setChatLineId(chatLineId)
            .setRecursive(recursive)
            .setFormatted(formatted)

        return copy
    }

    fun clone(): Message = copy()

    // TODO: Impl whenever ChatLib has this method
    // fun edit()

    fun chat() {
        parseMessage()
        if (Player.getPlayer() == null) return

        if (chatLineId != -1) {
            Client.getChatGui()?.printChatMessageWithOptionalDeletion(chatMessage, chatLineId)
            return
        }

        if (recursive) {
            Client.scheduleTask {
                Client.getConnection()?.handleChat(S02PacketChat(chatMessage, 0))
            }
        } else {
            Player.getPlayer()?.addChatMessage(chatMessage)
        }
    }

    fun actionBar() {
        parseMessage()
        if (Player.getPlayer() == null) return

        Client.getConnection()?.handleChat(S02PacketChat(chatMessage, 2))
    }

    // Due to the nature of this object this method will pretty much be
    // doing the same as #getChatMessage
    // key difference here is that this will not parse the components
    fun toMC(): IChatComponent = chatMessage

    private fun parseMessage() {
        chatMessage = ChatComponentText("")

        messageParts.forEach { chatMessage.appendSibling(it.chatComponentText) }
    }

    override fun toString(): String = "Message{formatted=\"$formatted\", recursive=\"$recursive\", chatLineId=\"$chatLineId\", messageParts=\"$messageParts\"}"
}