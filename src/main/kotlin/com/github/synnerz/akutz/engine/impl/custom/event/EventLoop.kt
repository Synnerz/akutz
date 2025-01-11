package com.github.synnerz.akutz.engine.impl.custom.event

import com.caoccao.javet.exceptions.JavetError
import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.interfaces.IJavetClosable
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.utils.SimpleMap
import io.vertx.core.Vertx
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * * This event loop is based on [Javenode]'s implementation
 */
open class EventLoop(
    var v8runtime: V8Runtime
) : IJavetClosable {
    var awaitTimeUnit: TimeUnit = DEFAULT_AWAIT_TIME_UNIT
    var awaitTimeOut: Int = DEFAULT_AWAIT_TIMEOUT
    var eventCount: AtomicInteger = AtomicInteger()
    var closed: Boolean = false
    var opts: EventLoopOptions = EventLoopOptions()
    var vertx: Vertx = Vertx.vertx(opts.vopts)

    fun await(): Boolean = await(awaitTimeOut, awaitTimeUnit)

    fun await(timeout: Int, timeUnit: TimeUnit): Boolean {
        if (closed) return false
        if (timeout <= 0) return false

        val total = TimeUnit.MILLISECONDS.convert(timeout.toLong(), timeUnit)
        val start = System.currentTimeMillis()
        v8runtime.await()

        while (eventCount.get() > 0) {
            if (System.currentTimeMillis() - start >= total) return false
            TimeUnit.MILLISECONDS.sleep(AWAIT_SLEEP_INTERVAL.toLong())
            v8runtime.await()
        }

        return true
    }

    @Synchronized
    override fun close() {
        if (closed) return

        try {
            if (!await()) throw JavetException(
                JavetError.RuntimeCloseFailure,
                SimpleMap.of<String, Any>(JavetError.PARAMETER_MESSAGE, "Failed to shutdown the event loop")
            )
        } catch (e: JavetException) {
            throw e
        } catch (e: InterruptedException) {
            throw JavetException(
                JavetError.RuntimeCloseFailure,
                SimpleMap.of<String, Any>(JavetError.PARAMETER_MESSAGE, "Event loop shutdown was interrupted")
            )
        } finally {
            closed = true
        }

        if (!closed) return

        if (!opts.pooled) vertx.close()
        if (opts.gcBeforeClose) v8runtime.lowMemoryNotification()
    }

    override fun isClosed(): Boolean {
        return closed
    }

    companion object {
        /** * In milliseconds */
        const val AWAIT_SLEEP_INTERVAL = 1
        const val DEFAULT_AWAIT_TIMEOUT = 60
        val DEFAULT_AWAIT_TIME_UNIT = TimeUnit.SECONDS
    }
}
