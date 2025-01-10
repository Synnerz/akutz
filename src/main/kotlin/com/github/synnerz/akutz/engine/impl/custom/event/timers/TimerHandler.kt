package com.github.synnerz.akutz.engine.impl.custom.event.timers

import com.caoccao.javet.utils.JavetResourceUtils
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueFunction
import com.caoccao.javet.values.reference.V8ValueObject
import com.github.synnerz.akutz.console.Console
import com.github.synnerz.akutz.engine.impl.custom.event.BaseCallable
import com.github.synnerz.akutz.engine.impl.custom.event.BaseFunction
import com.github.synnerz.akutz.engine.impl.custom.event.EventLoop
import java.util.concurrent.locks.ReentrantReadWriteLock

class TimerHandler(
    var eventLoop: EventLoop
) : BaseCallable() {
    private var closed: Boolean = false
    val readWriteLock = ReentrantReadWriteLock()
    val functionMap: MutableMap<Int, BaseFunction> = mutableMapOf()

    /**
     * * Puts the specified function into the [functionMap]
     */
    fun put(function: BaseFunction) {
        val writeLock = readWriteLock.writeLock()
        try {
            writeLock.lock()
            functionMap[function.getReferenceId()] = function
        } finally {
            writeLock.unlock()
        }
    }

    /**
     * * Gets the function by its ReferenceId from the [functionMap]
     * * Note: if it does not exist it will return `null`
     */
    fun get(refId: Int): BaseFunction? {
        val readLock = readWriteLock.readLock()
        try {
            readLock.lock()
            return functionMap[refId]
        } finally {
            readLock.unlock()
        }
    }

    fun get(value: V8ValueObject): BaseFunction? {
        return get(value.getPrivatePropertyInteger(BaseFunction.TIMERS_NAME))
    }

    override fun close() {
        if (isClosed) return
        val writeLock = readWriteLock.writeLock()
        try {
            writeLock.lock()
            functionMap.values.removeAll {
                JavetResourceUtils.safeClose(it)
                return@removeAll true
            }
            super.close()
        } finally {
            writeLock.unlock()
            closed = true
        }
    }

    override fun isClosed(): Boolean {
        return closed
    }

    /**
     * * Checks whether the given [callback] is valid or not
     * * if it is not it will throw an error
     */
    fun isValidCallback(callback: V8Value) {
        if (callback is V8ValueFunction) return
        Console.printError("provided callback \"$callback\" is not a valid Function")
        throw IllegalArgumentException("provided callback \"$callback\" is not a valid Function")
    }

    fun cancel(value: V8Value, name: String) {
        if (value !is V8ValueObject) {
            Console.printError("Argument $name is invalid.")
            throw IllegalArgumentException("Argument $name is invalid.")
        }

        val timerfn = get(value)
        if (timerfn == null) {
            Console.printError("Argument $name is invalid")
            throw IllegalArgumentException("Argument $name is invalid")
        }

        (timerfn as BaseTimersFunction).cancel()
    }

    fun clearImmediate(value: V8Value) {
        cancel(value, "immediate")
    }

    fun clearInterval(value: V8Value) {
        cancel(value, "interval")
    }

    fun clearTimeout(value: V8Value) {
        cancel(value, "timeout")
    }

    fun isValidDelay(delay: Int) {
        if (delay <= 0) {
            Console.printError("delay \"$delay\" must be greater or equal to \"1\"")
            throw IllegalArgumentException("delay \"$delay\" must be greater or equal to \"1\"")
        }
    }

    fun setImmediate(callback: V8Value): V8Value? {
        isValidCallback(callback)

        val timer = Immediate(this, callback as V8ValueFunction)
        timer.run()

        return timer.toV8Value()
    }

    fun setInterval(callback: V8Value, delay: Int): V8Value? {
        isValidCallback(callback)
        isValidDelay(delay)
        val timer = Timeout(this, true, callback as V8ValueFunction, delay)
        timer.run()

        return timer.toV8Value()
    }

    fun setTimeout(callback: V8Value, delay: Int): V8Value? {
        isValidCallback(callback)
        isValidDelay(delay)
        val timer = Timeout(this, false, callback as V8ValueFunction, delay)
        timer.run()

        return timer.toV8Value()
    }

    // TODO: maybe actually do this (useless)
    // TODO: "this" refers to actually allowing for stuff like `setTimeout(function, ms, [...function params here])`
    // Note: setTimeout and setInterval start at idx 2, setImmediate starts at idx 1
    // companion object {
    //     fun extractArgs(args: Array<V8Value>, startIdx: Int): Array<V8Value> {
    //         if (args.size > startIdx) return Arrays.copyOfRange(args, startIdx, args.size)
    //         return arrayOf()
    //     }
    // }
}
