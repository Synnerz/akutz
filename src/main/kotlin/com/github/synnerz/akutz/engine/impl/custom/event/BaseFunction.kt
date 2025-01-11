package com.github.synnerz.akutz.engine.impl.custom.event

import com.caoccao.javet.exceptions.JavetError
import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.utils.JavetResourceUtils
import com.caoccao.javet.values.V8Value
import com.github.synnerz.akutz.console.Console.printError
import com.github.synnerz.akutz.engine.impl.custom.event.timers.TimerHandler
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseFunction(
    val timerHandler: TimerHandler
) : BaseCallable(), IFunction {
    val _referenceId: Int = GLOBAL_REFERENCE_ID.incrementAndGet()

    init {
        timerHandler.put(this)
    }

    override fun getEventLoop(): EventLoop {
        return timerHandler.eventLoop
    }

    override fun getReferenceId(): Int {
        return _referenceId
    }

    override fun toV8Value(): V8Value? {
        val v8runtime = getEventLoop().v8runtime
        if (JavetResourceUtils.isClosed(v8runtime)) {
            printError("[toV8Value] failed [V8Runtime] was closed")
            throw JavetException(JavetError.RuntimeAlreadyClosed)
        }

        val v8obj = v8runtime.createV8ValueObject()
        try {
            v8obj.bind(this)
        } catch (e: Exception) {
            e.printError()
            e.printStackTrace()
        }
        v8obj.setPrivateProperty(TIMERS_NAME, _referenceId)
        return v8obj
    }

    companion object {
        val GLOBAL_REFERENCE_ID = AtomicInteger()
        val TIMERS_NAME = "_timers#referenceId"
    }
}
