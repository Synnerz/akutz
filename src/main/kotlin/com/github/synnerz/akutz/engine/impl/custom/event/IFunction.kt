package com.github.synnerz.akutz.engine.impl.custom.event

import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.interfaces.IJavetClosable
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.values.V8Value

interface IFunction : IJavetClosable, Runnable {
    fun getEventLoop(): EventLoop

    fun getReferenceId(): Int

    fun getV8Runtime(): V8Runtime {
        return getEventLoop().v8runtime
    }

    @Throws(JavetException::class)
    fun toV8Value(): V8Value?
}
