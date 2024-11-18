package com.github.synnerz.akutz.api.events

import com.github.synnerz.akutz.engine.impl.Loader

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/triggers/TriggerType.kt)
 */
enum class EventType {
    // Client
    Chat,
    ActionBar,
    Tick,
    Step,
    GameUnload,
    GameLoad,
    Clicked,
    Scrolled,
    Dragged,
    GuiOpened,
    PickupItem,
    DropItem,
    MessageSent,
    Tooltip,
    PlayerInteract,
    AttackEntity,
    GuiRender,
    GuiKey,
    GuiMouseClick,
    GuiMouseRelease,
    GuiMouseDrag,
    PacketSent,
    PacketReceived,
    ServerConnect,
    ServerDisconnect,
    GuiClosed,
    RenderSlot,

    // Rendering
    RenderWorld,
    BlockHighlight,
    RenderOverlay,
    RenderChat,
    RenderScoreboard,
    RenderTitle,
    RenderEntity,
    PostGuiRender,
    PreItemRender,
    RenderItemIntoGui,
    RenderItemOverlayIntoGui,
    RenderSlotHighlight,
    PostRenderEntity,
    RenderTileEntity,
    PostRenderTileEntity,
    ScreenResize,

    // World
    SoundPlay,
    WorldLoad,
    WorldUnload,
    BlockBreak,
    SpawnParticle,
    EntityDeath,
    EntityDamage,

    // Misc
    Forge,
    Command,
    Other;

    fun triggerAll(vararg args: Any?) {
        Loader.execute(this, args)
    }
}