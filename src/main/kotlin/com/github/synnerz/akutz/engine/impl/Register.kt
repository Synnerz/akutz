package com.github.synnerz.akutz.engine.impl
import com.github.synnerz.akutz.api.events.*
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/IRegister.kt)
 */
object Register {
    val eventMap = mutableMapOf<String, (((Array<out Any?>) -> Unit) -> EventTrigger)>()

    init {
        // Command
        registerMethod("command", type = MethodType.Command)

        // Normal events
        registerMethod("tick", EventType.Tick)
        registerMethod("worldload", EventType.WorldUnload)
        registerMethod("worldunload", EventType.WorldUnload)
        registerMethod("renderworld", EventType.RenderWorld)
        registerMethod("postguirender", EventType.PostGuiRender)
        registerMethod("guirender", EventType.GuiRender)

        // Cancellable events
        registerMethod("guiopened", EventType.GuiOpened, MethodType.Cancellable)
        registerMethod("renderoverlay", EventType.RenderOverlay, MethodType.Cancellable)
        registerMethod("renderchat", EventType.RenderChat, MethodType.Cancellable)

        // SoundPlay
        registerMethod("soundplay", EventType.SoundPlay, MethodType.SoundPlay)
    }

    fun registerMethod(eventName: String, eventType: EventType? = null, type: MethodType = MethodType.Normal) {
        val cb: (((Array<out Any?>) -> Unit) -> EventTrigger) = when (type) {
            MethodType.Normal -> { mm: (args: Array<out Any?>) -> Unit -> NormalTrigger(mm, eventType!!) }
            MethodType.Command -> { mm: (args: Array<out Any?>) -> Unit -> CommandEvent(mm) }
            MethodType.Cancellable -> { mm: (args: Array<out Any?>) -> Unit -> CancelableEvent(mm, eventType!!) }
            MethodType.SoundPlay -> { mm: (args: Array<out Any?>) -> Unit -> SoundPlayEvent(mm) }
        }

        eventMap["register$eventName"] = cb
    }

    fun register(eventType: Any, method: (args: Array<out Any?>) -> Unit) : EventTrigger {
        if (eventType is Class<*>)
            return ForgeEvent(method, eventType)

        require(eventType is String) {
            "register() expects a String or Java Class as its first argument"
        }

        val name = eventType.lowercase()
        val cb = eventMap["register$name"] ?: throw NoSuchMethodException("No EventType with type $eventType was found.")

        return cb.invoke(method)
    }

    @JvmStatic
    fun cancel(event: Any) {
        when (event) {
            is PlaySoundEvent -> event.result = null
            is Cancelable -> event.setCanceled(true)
            is Event -> if (event.isCancelable) {
                event.isCanceled = true
            } else throw IllegalArgumentException("Attempt to cancel non-cancelable event ${event.javaClass.name}")
            else -> throw IllegalArgumentException("cancel() expects an Event but received ${event.javaClass.name}")
        }
    }
}