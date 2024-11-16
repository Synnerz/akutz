package com.github.synnerz.akutz.listeners

import com.github.synnerz.akutz.api.events.EventType
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/listeners/WorldListener.kt)
 */
object WorldListener {
    private var triggerWorldLoad: Boolean = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        triggerWorldLoad = true
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Pre) {
        if (!triggerWorldLoad) return

        EventType.WorldLoad.triggerAll()
        triggerWorldLoad = false
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        EventType.WorldUnload.triggerAll()
    }
}