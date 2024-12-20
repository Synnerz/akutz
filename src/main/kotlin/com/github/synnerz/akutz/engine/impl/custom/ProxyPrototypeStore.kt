package com.github.synnerz.akutz.engine.impl.custom

import com.caoccao.javet.enums.V8ProxyMode
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueObject

object ProxyPrototypeStore {
    private const val DUMMY_FUNCTION_STRING = "(() => {\n" +
            "  const DummyFunction = function () { };\n" +
            "  return DummyFunction;\n" +
            "})();"
    private const val PREFIX = ".proxy.prototype."

    fun createOrGetPrototype(
        v8Runtime: V8Runtime, v8ProxyMode: V8ProxyMode, clazz: Class<*>
    ): V8ValueObject? {
        val k = "${v8ProxyMode.name}${PREFIX}${clazz.name}"

        if (EngineCache.privateProperties.contains(k)) {
            return EngineCache.globalObject!!.getPrivateProperty(k)
        }

        val v8ValueObject: V8ValueObject
        v8Runtime.v8Scope.use { v8Scope ->
            when (v8ProxyMode) {
                V8ProxyMode.Class, V8ProxyMode.Function -> {
                    v8ValueObject = v8Scope.createV8ValueFunction(DUMMY_FUNCTION_STRING)
                    createOrGetPrototype(v8Runtime, V8ProxyMode.Object, clazz).use { proto ->
                        v8ValueObject.setPrototype(proto)
                    }
                }
                V8ProxyMode.Object -> v8ValueObject = v8Scope.createV8ValueObject()
            }

            EngineCache.globalObject!!.setPrivateProperty(k, v8ValueObject)
            EngineCache.privateProperties.add(k)
            v8Scope.setEscapable()
            return v8ValueObject
        }
    }

    fun getPrototype(
        v8Runtime: V8Runtime, v8ProxyMode: V8ProxyMode, clazz: Class<*>
    ): V8Value? {
        val k = "${v8ProxyMode.name}${PREFIX}${clazz.name}"

        return if (EngineCache.privateProperties.contains(k)) EngineCache.globalObject!!.getPrivateProperty(k) else null
    }
}