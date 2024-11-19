package com.github.synnerz.akutz.engine.impl
import com.github.synnerz.akutz.api.events.*
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/IRegister.kt)
 */
object Register {
    val eventMap = mutableMapOf<String, (((Array<out Any?>) -> Unit) -> BaseEvent)>()

    init {
        // Command
        registerMethod("command", type = MethodType.Command)

        // Normal events
        registerMethod("tick", EventType.Tick)
        registerMethod("worldload", EventType.WorldLoad)
        registerMethod("worldunload", EventType.WorldUnload)
        registerMethod("renderworld", EventType.RenderWorld)
        registerMethod("postguirender", EventType.PostGuiRender)
        registerMethod("guirender", EventType.GuiRender)
        registerMethod("preitemrender", EventType.PreItemRender)
        registerMethod("renderslothighlight", EventType.RenderSlotHighlight)
        registerMethod("guiclosed", EventType.GuiClosed)
        registerMethod("gameload", EventType.GameLoad)
        registerMethod("screenresize", EventType.ScreenResize)
        registerMethod("attackentity", EventType.AttackEntity)
        registerMethod("blockbreak", EventType.BlockBreak)
        registerMethod("serverdisconnect", EventType.ServerDisconnect)
        registerMethod("gameunload", EventType.GameUnload)
        registerMethod("entitydeath", EventType.EntityDeath)
        registerMethod("entitydamage", EventType.EntityDamage)
        registerMethod("clicked", EventType.Clicked)
        registerMethod("scrolled", EventType.Scrolled)
        registerMethod("dragged", EventType.Dragged)

        // Cancelable events
        registerMethod("guiopened", EventType.GuiOpened, MethodType.Cancelable)
        registerMethod("renderoverlay", EventType.RenderOverlay, MethodType.Cancelable)
        registerMethod("renderchat", EventType.RenderChat, MethodType.Cancelable)
        registerMethod("spawnparticle", EventType.SpawnParticle, MethodType.Cancelable)
        registerMethod("messagesent", EventType.MessageSent, MethodType.Cancelable)
        registerMethod("renderscoreboard", EventType.RenderScoreboard, MethodType.Cancelable)
        registerMethod("rendertitle", EventType.RenderTitle, MethodType.Cancelable)
        registerMethod("renderslot", EventType.RenderSlot, MethodType.Cancelable)
        registerMethod("blockhighlight", EventType.BlockHighlight, MethodType.Cancelable)
        registerMethod("playerinteract", EventType.PlayerInteract, MethodType.Cancelable)
        registerMethod("serverconnect", EventType.ServerConnect, MethodType.Cancelable)
        registerMethod("renderitemintogui", EventType.RenderItemIntoGui, MethodType.Cancelable)
        registerMethod("renderitemoverlayintogui", EventType.RenderItemOverlayIntoGui, MethodType.Cancelable)
        registerMethod("tooltip", EventType.Tooltip, MethodType.Cancelable)
        registerMethod("guikey", EventType.GuiKey, MethodType.Cancelable)
        registerMethod("guimouseclick", EventType.GuiMouseClick, MethodType.Cancelable)
        registerMethod("guimouserelease", EventType.GuiMouseRelease, MethodType.Cancelable)

        // SoundPlay
        registerMethod("soundplay", EventType.SoundPlay, MethodType.SoundPlay)

        // Chat
        registerMethod("chat", EventType.Chat, MethodType.Chat)
        registerMethod("actionbar", EventType.ActionBar, MethodType.Chat)

        // Filtered class events
        registerMethod("packetreceived", EventType.PacketReceived, MethodType.Packet)
        registerMethod("packetsent", EventType.PacketSent, MethodType.Packet)
        registerMethod("renderentity", EventType.RenderEntity, MethodType.RenderEntity)
        registerMethod("postrenderentity", EventType.PostRenderEntity, MethodType.RenderEntity)
        registerMethod("rendertileentity", EventType.RenderTileEntity, MethodType.RenderTileEntity)
        registerMethod("postrendertileentity", EventType.PostRenderTileEntity, MethodType.RenderTileEntity)
    }

    fun registerMethod(eventName: String, eventType: EventType? = null, type: MethodType = MethodType.Normal) {
        val cb: (((Array<out Any?>) -> Unit) -> BaseEvent) = when (type) {
            MethodType.Normal -> { mm: (args: Array<out Any?>) -> Unit -> NormalEvent(mm, eventType!!) }
            MethodType.Command -> { mm: (args: Array<out Any?>) -> Unit -> CommandEvent(mm) }
            MethodType.Cancelable -> { mm: (args: Array<out Any?>) -> Unit -> CancelableEvent(mm, eventType!!) }
            MethodType.SoundPlay -> { mm: (args: Array<out Any?>) -> Unit -> SoundPlayEvent(mm) }
            MethodType.Chat -> { mm: (args: Array<out Any?>) -> Unit -> ChatEvent(mm, eventType!!) }
            MethodType.RenderEntity -> { mm: (args: Array<out Any?>) -> Unit -> RenderEntityEvent(mm, eventType!!) }
            MethodType.RenderTileEntity -> { mm: (args: Array<out Any?>) -> Unit -> RenderTileEntityEvent(mm, eventType!!) }
            MethodType.Packet -> { mm: (args: Array<out Any?>) -> Unit -> PacketEvent(mm, eventType!!) }
        }

        eventMap["register$eventName"] = cb
    }

    fun register(eventType: Any, method: (args: Array<out Any?>) -> Unit) : BaseEvent {
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