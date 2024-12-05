package com.github.synnerz.akutz.engine.impl.custom

import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.utils.JavetResourceUtils
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.primitive.V8ValueString
import com.caoccao.javet.values.reference.V8ValueGlobalObject
import com.caoccao.javet.values.reference.V8ValueObject
import com.caoccao.javet.values.reference.builtin.V8ValueBuiltInObject
import com.caoccao.javet.values.reference.builtin.V8ValueBuiltInReflect

/**
 * This object represent an "EngineCache" which is used to avoid doing so many JNI calls
 * this is to better improve the interoperability performance
 */
object EngineCache {
    var v8runtime: V8Runtime? = null
        internal set
    var globalObject: V8ValueGlobalObject? = null
        internal set
    var builtInObject: V8ValueBuiltInObject? = null
        internal set
    var builtInReflect: V8ValueBuiltInReflect? = null
        internal set
    val privateProperties = mutableListOf<String>()

    internal fun loadProps() {
        val props = globalObject?.propertyNames ?: return
        if (privateProperties.size == props.length) return

        for (i in 0..props.length) {
            val v = props.get<V8Value>(i)
            if (v !is V8ValueString) continue
            val p = v.toPrimitive()

            privateProperties.add(p)
        }
    }

    internal fun loadBuiltins() {
        val builtObj = v8runtime!!.getExecutor(V8ValueBuiltInObject.NAME).execute<V8Value>()
        if (builtObj is V8ValueObject) {
            builtInObject = V8ValueBuiltInObject(v8runtime, builtObj.handle)
            return
        }

        JavetResourceUtils.safeClose(builtObj)
        println("possible error while loading built ins")
    }
}