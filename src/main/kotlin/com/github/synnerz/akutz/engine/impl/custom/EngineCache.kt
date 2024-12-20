package com.github.synnerz.akutz.engine.impl.custom

import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.primitive.V8ValueString

/**
 * This object represent an "EngineCache" which is used to avoid doing so many JNI calls
 * this is to better improve the interoperability performance
 */
object EngineCache {
    var v8runtime: V8Runtime? = null
        internal set
    val privateProperties = mutableListOf<String>()

    internal fun loadProps() {
        v8runtime!!.globalObject.use {
            val props = it.propertyNames
            if (privateProperties.size == props.length) return

            for (i in 0..props.length) {
                val v = props.get<V8Value>(i)
                if (v !is V8ValueString) continue
                val p = v.toPrimitive()

                privateProperties.add(p)
            }
        }
    }

    internal fun load(runtime: V8Runtime) {
        v8runtime = runtime
        loadProps()
    }

    internal fun clear() {
        v8runtime = null
        privateProperties.clear()
    }
}