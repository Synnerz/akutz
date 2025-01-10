package com.github.synnerz.akutz.engine.impl.custom.event.timers

import com.caoccao.javet.utils.JavetResourceUtils
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueFunction
import com.github.synnerz.akutz.console.Console.printError
import com.github.synnerz.akutz.engine.impl.custom.event.BaseFunction
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseTimersFunction(
    timerHandler: TimerHandler,
    val recurrent: Boolean,
    v8func: V8ValueFunction?,
    idelay: Int
) : BaseFunction(timerHandler) {
    var callback: V8ValueFunction? = v8func!!.toClone()
    val delay: Long = idelay.toLong()
    val active: AtomicBoolean = AtomicBoolean(false)
    var timerId: Int = -1

    fun cancel() {
        if (!hasRef()) return

        active.set(false)
        getEventLoop().vertx.cancelTimer(timerId.toLong())
        timerId = -1
        if (!recurrent) {
            getEventLoop().eventCount.decrementAndGet()
        }
    }

    override fun close() {
        if (isClosed) return

        cancel()
        JavetResourceUtils.safeClose(callback)
        callback = null
    }

    fun hasRef(): Boolean = active.get()

    fun hasRef(vararg values: V8Value): V8Value {
        return getV8Runtime().createV8ValueBoolean(hasRef())
    }

    override fun isClosed(): Boolean {
        return JavetResourceUtils.isClosed(callback)
    }

    fun ref(thisObject: V8Value, vararg values: V8Value): V8Value {
        return thisObject
    }

    fun unref(thisObject: V8Value, vararg values: V8Value): V8Value {
        cancel()
        return thisObject
    }

    override fun run() {
        active.set(true)
        if (recurrent) {
            timerId = getEventLoop().vertx.setPeriodic(delay) {
                if (isClosed) return@setPeriodic
                try {
                    callback?.call<V8Value?>(null, null)
                } catch (e: Exception) {
                    e.printError()
                    e.printStackTrace()
                }
            }.toInt()
            return
        }

        getEventLoop().eventCount.incrementAndGet()
        timerId = getEventLoop().vertx.setTimer(delay) {
            if (isClosed) return@setTimer
            try {
                callback?.call<V8Value?>(null, null)
            } catch (e: Exception) {
                e.printError()
                e.printStackTrace()
            } finally {
                active.set(false)
                getEventLoop().eventCount.decrementAndGet()
            }
        }.toInt()
    }
}