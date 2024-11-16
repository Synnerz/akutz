package com.github.synnerz.akutz.listeners

import com.github.synnerz.akutz.api.events.EventType
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.util.vector.Vector3f

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
    fun onRenderWorld(event: RenderWorldLastEvent) {
        EventType.RenderWorld.triggerAll(event.partialTicks)
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        EventType.WorldUnload.triggerAll()
    }

    @SubscribeEvent
    fun onSoundPlay(event: PlaySoundEvent) {
        val pos = Vector3f(
            event.sound.xPosF,
            event.sound.yPosF,
            event.sound.zPosF
        )
        val vol = event.sound.volume
        val pitch = event.sound.pitch

        EventType.SoundPlay.triggerAll(
            pos,
            event.name,
            vol,
            pitch,
            event.category ?: event.category?.categoryName,
            event
        )
    }
}