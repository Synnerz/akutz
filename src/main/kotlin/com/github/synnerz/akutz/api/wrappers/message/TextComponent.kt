package com.github.synnerz.akutz.api.wrappers.message

import com.github.synnerz.akutz.api.libs.ChatLib
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import java.net.URI
import java.net.URISyntaxException

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/objects/message/TextComponent.kt)
 */
class TextComponent {
    lateinit var chatComponentText: IChatComponent
    private var text: String
    private var formatted = true
    private var clickAction: String? = null
    private var clickValue: String? = null
    private var hoverAction: String? = "show_text"
    private var hoverValue: String? = null

    constructor(str: String) {
        this.text = str
        reInstance()
    }

    constructor(comp: IChatComponent) {
        chatComponentText = comp
        text = comp.formattedText

        val chatStyle = chatComponentText.chatStyle

        val clickEvent = chatStyle.chatClickEvent
        clickAction = clickEvent?.action?.canonicalName
        clickValue = clickEvent?.value

        val hoverEvent = chatStyle.chatHoverEvent
        hoverAction = hoverEvent?.action?.canonicalName
        hoverValue = hoverEvent?.value?.formattedText
    }

    fun getText(): String = text

    fun setText(str: String) = apply {
        this.text = str
        reInstance()
    }

    fun isFormatted(): Boolean = formatted

    fun setFormatted(formatted: Boolean) = apply {
        this.formatted = formatted
        reInstance()
    }

    fun setClick(action: String, value: String) = apply {
        clickAction = action
        clickValue = value
        reInstanceClick()
    }

    fun getClickAction(): String? = clickAction

    fun setClickAction(action: String) = apply {
        clickAction = action
        reInstanceClick()
    }

    fun getClickValue(): String? = clickValue

    fun setClickValue(value: String) = apply {
        clickValue = value
        reInstanceClick()
    }

    fun setHover(action: String, value: String) = apply {
        hoverAction = action
        hoverValue = value
        reInstanceHover()
    }

    fun getHoverAction(): String? = hoverAction

    fun setHoverAction(action: String) = apply {
        hoverAction = action
        reInstanceHover()
    }

    fun getHoverValue(): String? =  hoverValue

    fun setHoverValue(value: String) = apply {
        hoverValue = value
        reInstanceHover()
    }

    fun chat() = Message(this).chat()

    fun actionBar() = Message(this).actionBar()

    fun reInstance() {
        val str = if (formatted) ChatLib.addColor(text) else text

        chatComponentText = stringToComponent(str)

        reInstanceClick()
        reInstanceHover()
    }

    private fun reInstanceClick() {
        if (clickAction == null || clickValue == null) return

        chatComponentText.chatStyle
            .chatClickEvent = ClickEvent(
                ClickEvent.Action.getValueByCanonicalName(clickAction),
                if (formatted) ChatLib.addColor(clickValue!!)
                else clickValue
            )
    }

    private fun reInstanceHover() {
        if (hoverAction == null || hoverValue == null) return

        chatComponentText.chatStyle
            .chatHoverEvent = HoverEvent(
                HoverEvent.Action.getValueByCanonicalName(hoverAction),
                ChatComponentText(
                    if (formatted) ChatLib.addColor(hoverValue!!)
                    else hoverValue
                )
            )
    }

    private fun stringToComponent(string: String): ChatComponentText {
        val buffer = StringBuilder()
        val comp = ChatComponentText("")
        var style = ChatStyle()
        var i = 0
        while (i < string.length) {
            if (i < string.length - 1 && formatRegex.matches(string.substring(i, i + 1))) {
                // add the previous component as a sibling of the base component
                val prevText = ChatComponentText(buffer.toString()).apply { chatStyle = style.createDeepCopy() }
                buffer.clear()
                comp.appendSibling(prevText)

                // Update the style local var
                i++
                when {
                    string[i] in '0'..'f' -> style.color = EnumChatFormatting.entries.toTypedArray()[string[i].digitToInt(16)]
                    string[i] == 'k' -> style.obfuscated = true
                    string[i] == 'l' -> style.bold = true
                    string[i] == 'm' -> style.strikethrough = true
                    string[i] == 'n' -> style.underlined = true
                    string[i] == 'o' -> style.italic = true
                    string[i] == 'r' -> style = ChatStyle()
                }
            } else if (URL_REGEX.matchesAt(string, i)) {
                // add the previous component as a sibling of the base component
                val prevText = ChatComponentText(buffer.toString()).apply { chatStyle = style.createDeepCopy() }
                buffer.clear()
                comp.appendSibling(prevText)

                val ( format, link ) = URL_REGEX.matchAt(string, i)!!.destructured
                i += if (format.isNotEmpty()) format.length + link.length - 1 else link.length - 1

                // add the link with previous styles
                val linkComponent = ChatComponentText(link).apply {
                    chatStyle = style.createDeepCopy()

                    try {
                        chatStyle.chatClickEvent = ClickEvent(
                            ClickEvent.Action.OPEN_URL, if (URI(link).scheme == null) {
                                "http://$link"
                            } else {
                                link
                            }
                        )
                    } catch (ignored: URISyntaxException) {
                        // this will throw if there is bad uri syntax (e.g. a : with no port)
                    }
                }

                // scuffed workaround :(
                val linkStyle = linkComponent.chatStyle
                when (val form =  format.replace("\u00A7", "")) {
                    in "0".."f" -> linkStyle.color = EnumChatFormatting.entries.toTypedArray()[form[0].digitToInt(16)]
                    "k" -> linkStyle.obfuscated = true
                    "l" -> linkStyle.bold = true
                    "m" -> linkStyle.strikethrough = true
                    "n" -> linkStyle.underlined = true
                    "o" -> linkStyle.italic = true
                    "r" -> linkComponent.chatStyle = ChatStyle()
                }
                // Force underlined style due to it being a link
                linkStyle.underlined = true

                comp.appendSibling(linkComponent)
            } else {
                // store this char for later use
                buffer.append(string[i])
            }
            i++
        }

        if (buffer.isNotEmpty()) {
            // add the leftover parts of the string
            comp.appendSibling(ChatComponentText(buffer.toString()).apply { chatStyle = style.createDeepCopy() })
        }
        return comp
    }

    companion object {
        private val URL_REGEX =
        // a modified version of ForgeHooks.URL_PATTERN disallowing connecting to IPs
        //           schema             namespace            port   path         ends
            //   |-------------|  |------------------|    |-------| |--|   |---------------|
            "(\\u00A7[\\da-fk-or])?((?:[a-z\\d]{2,}://)?[-\\w.]+\\.[a-z]{2,}?(?::\\d{1,5})?.*?(?=[!\"\\u00A7 \n]|$))".toRegex()
            // ^ "Fixed" version of ChatTriggers' regex, this one however only accepts a single formatting color code as its chat style

        private val formatRegex = "[\u00A7&][\\da-fk-or]".toRegex()
    }

    fun toMC(): IChatComponent = chatComponentText

    override fun toString(): String = "TextComponent{text=\"$text\", formatted=\"$formatted\", clickAction=\"$clickAction\", clickValue=\"$clickValue\", hoverAction=\"$hoverAction\", hoverValue=\"$hoverValue\"}"
}