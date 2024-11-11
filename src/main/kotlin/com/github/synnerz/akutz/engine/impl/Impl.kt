package com.github.synnerz.akutz.engine.impl

import com.caoccao.javet.buddy.interop.proxy.JavetReflectionObjectFactory
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.converters.JavetProxyConverter
import java.io.File

object Impl {
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
        // TODO: fix imports not working properly yet (good enough for testing though)
        v8runtime!!.setV8ModuleResolver { runtime, resourceName, v8ModuleReferrer ->
            // THIS IS TEMPORAL SOLUTION
            val requestedFrom = v8ModuleReferrer.resourceName.replace("index.js", "")
            // TEMPORAL SOLUTION
            val requestedModule = File(requestedFrom, resourceName.replace("./", ""))
            runtime.getExecutor(requestedModule.readText())
                .setResourceName(requestedModule.path)
                .compileV8Module()
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
        v8runtime!!.getExecutor(script.readText()).setResourceName(script.path).setModule(true).executeVoid()
    }
}