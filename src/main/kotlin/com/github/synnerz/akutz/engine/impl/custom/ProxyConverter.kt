package com.github.synnerz.akutz.engine.impl.custom

import com.caoccao.javet.annotations.V8Convert
import com.caoccao.javet.enums.V8ProxyMode
import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.binding.IClassProxyPlugin
import com.caoccao.javet.interop.binding.IClassProxyPluginFunction
import com.caoccao.javet.interop.converters.JavetObjectConverter
import com.caoccao.javet.interop.proxy.*
import com.caoccao.javet.utils.JavetResourceUtils
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueProxy

open class ProxyConverter : JavetObjectConverter() {
    companion object {
        const val DUMMY_FUNCTION_STRING = "(() => {\n" +
                    "  const DummyFunction = function () { };\n" +
                    "  return DummyFunction;\n" +
                    "})();"
    }

    @Throws(JavetException::class)
    protected fun <T : V8Value?> toProxiedV8Value(v8Runtime: V8Runtime, obj: Any): T {
        if (obj is IJavetNonProxy) return v8Runtime.createV8ValueUndefined() as T
        val v8Value: V8Value

        var proxyMode = V8ProxyMode.Object
        val objectClass: Class<*> = obj.javaClass
        if (obj is Class<*> && V8ProxyMode.isClassMode(obj)) {
            proxyMode = V8ProxyMode.Class
        } else if (objectClass.isAnnotationPresent(V8Convert::class.java)) {
            val v8Convert = objectClass.getAnnotation(V8Convert::class.java)
            if (v8Convert.proxyMode == V8ProxyMode.Function) {
                proxyMode = V8ProxyMode.Function
            }
        }

        val v8Scope = v8Runtime.v8Scope
        val v8ValueProxy: V8ValueProxy
        var v8ValueTarget: V8Value? = null

        try {
            when (proxyMode) {
                V8ProxyMode.Class -> v8ValueTarget = ProxyPrototypeStore.createOrGetPrototype(
                    v8Runtime, proxyMode, obj as Class<*>
                )

                V8ProxyMode.Function -> v8ValueTarget = ProxyPrototypeStore.createOrGetPrototype(
                    v8Runtime, proxyMode, objectClass
                )

                else -> if (obj is IJavetDirectProxyHandler<*>) {
                    obj.v8Runtime = v8Runtime
                    v8ValueTarget = obj.createTargetObject()
                } else {
                    v8ValueTarget = config.proxyPlugins.stream()
                        .filter { p: IClassProxyPlugin -> p.isProxyable(objectClass) }
                        .findFirst()
                        .map<IClassProxyPluginFunction<Exception?>> { p: IClassProxyPlugin ->
                            p.getTargetObjectConstructor<Exception?>(objectClass)
                        }
                        .map<V8Value> { f: IClassProxyPluginFunction<Exception?> ->
                            try {
                                return@map f.invoke(v8Runtime, obj)
                            } catch (ignored: Throwable) {
                            }
                            null
                        }
                        .orElseGet {
                            try {
                                val protoval = ProxyPrototypeStore.createOrGetPrototype(
                                    v8Runtime, proxyMode, objectClass
                                )
                                val protoobj = v8Runtime.createV8ValueObject()
                                return@orElseGet EngineCache.builtInObject!!.setPrototypeOf(protoobj, protoval)
                            } catch (ignored: Throwable) {
                            }
                            null
                        }
                }
            }
            v8ValueProxy = v8Scope.createV8ValueProxy(v8ValueTarget)
        } finally {
            JavetResourceUtils.safeClose(v8ValueTarget)
        }

        val valueHandler = v8ValueProxy.handler
        val proxyHandler: IJavetProxyHandler<*, *> = when (proxyMode) {
            V8ProxyMode.Class -> JavetReflectionProxyClassHandler<Class<*>, Exception>(
                v8Runtime,
                obj as Class<*>
            )

            V8ProxyMode.Function -> if (obj is IJavetDirectProxyHandler<*>) {
                JavetDirectProxyFunctionHandler(
                    v8Runtime, obj as IJavetDirectProxyHandler<Exception>
                )
            } else {
                JavetReflectionProxyFunctionHandler(v8Runtime, obj)
            }

            else -> if (obj is IJavetDirectProxyHandler<*>) {
                JavetDirectProxyObjectHandler(
                    v8Runtime, obj as IJavetDirectProxyHandler<Exception>
                )
            } else {
                JavetReflectionProxyObjectHandler(v8Runtime, obj)
            }
        }

        val cbContexts = valueHandler.bind(proxyHandler)
        val cbId = v8Runtime.createV8ValueLong(cbContexts[0].handle)
        valueHandler.setPrivateProperty(
            PRIVATE_PROPERTY_PROXY_TARGET,
            cbId
        )

        v8Value = v8ValueProxy
        v8Scope.setEscapable()

        return v8Value as T
    }

    override fun <T : V8Value?> toV8Value(v8Runtime: V8Runtime?, obj: Any?, depth: Int): T? {
        if (obj is V8Value) {
            return obj as T
        }
        var proxyable = false
        if (obj != null) {
            if (obj is IJavetDirectProxyHandler<*>) {
                proxyable = true
            } else if (obj !is IJavetNonProxy) {
                val objectClass: Class<*> = obj.javaClass
                proxyable = getConfig().proxyPlugins.stream().anyMatch { p: IClassProxyPlugin ->
                    p.isProxyable(
                        objectClass
                    )
                }
            }
        }
        if (!proxyable) {
            val v8Value = super.toV8Value<V8Value>(v8Runtime, obj, depth)
            if (v8Value != null && !(v8Value.isUndefined)) {
                return v8Value as T
            }
        }
        return toProxiedV8Value(v8Runtime!!, obj!!)
    }
}