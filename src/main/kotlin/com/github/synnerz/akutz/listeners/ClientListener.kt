package com.github.synnerz.akutz.listeners

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.wrappers.World
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/listeners/ClientListener.kt)
 */
object ClientListener {
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase !== TickEvent.Phase.END || !World.isLoaded()) return

        EventType.Tick.triggerAll()
    }
}