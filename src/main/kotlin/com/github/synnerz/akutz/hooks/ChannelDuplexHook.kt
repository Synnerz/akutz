package com.github.synnerz.akutz.hooks

import com.github.synnerz.akutz.api.events.Cancelable
import com.github.synnerz.akutz.api.events.EventType
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.Packet

class ChannelDuplexHook : ChannelDuplexHandler() {
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is Packet<*>) {
            val event = Cancelable()
            EventType.PacketReceived.triggerAll(msg, event)

            if (!event.isCanceled()) ctx?.fireChannelRead(msg)
        }
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        if (msg is Packet<*>) {
            val event = Cancelable()
            EventType.PacketSent.triggerAll(msg, event)

            if (!event.isCanceled()) ctx?.write(msg, promise)
        }
    }
}