package com.github.synnerz.akutz.engine

import com.caoccao.javet.buddy.interop.proxy.JavetReflectionObjectFactory
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.converters.JavetProxyConverter
import java.io.File

object JSImpl {
    private var v8runtime: V8Runtime? = null

    fun print(msg: Any) {
        println(msg)
    }

    fun setup() {
        v8runtime = V8Host.getV8Instance().createV8Runtime()
        v8runtime!!.setPromiseRejectCallback { jevent, valpromise, value ->
            println("event: $jevent")
            println("valPromise: $valpromise")
            println("value: $value")
        }
        v8runtime!!.setV8ModuleResolver { runtime, resourceName, v8ModuleReferrer ->
            if ("./utils.js" == resourceName) {
                println("v8modulereferrer: ${v8ModuleReferrer.resourceName}")
                return@setV8ModuleResolver runtime.getExecutor("export function test() { return 1; }")
                    .setResourceName(resourceName).compileV8Module()
            } else {
                return@setV8ModuleResolver null
            }
        }
        val javetProxyConverter = JavetProxyConverter()
        javetProxyConverter.config.setReflectionObjectFactory(JavetReflectionObjectFactory.getInstance())

        v8runtime?.setConverter(javetProxyConverter)

        val javetJVMInterceptor = JavetJVMInterceptor(v8runtime)
        javetJVMInterceptor.register(v8runtime?.globalObject)
    }

    // TODO: probably
    fun remove() {}

    fun execute(script: File) {
        v8runtime?.getExecutor(script.readText())?.setResourceName(script.name)?.setModule(true)?.executeVoid() ?: return
    }
}