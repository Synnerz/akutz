package com.github.synnerz.akutz.engine.impl

import com.caoccao.javet.buddy.interop.proxy.JavetReflectionObjectFactory
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.converters.JavetProxyConverter
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

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
        v8runtime!!.setV8ModuleResolver { runtime, resourceName, v8ModuleReferrer ->
            val requestedFrom = Paths.get(v8ModuleReferrer.resourceName).parent
            val requestedModule = requestedFrom.resolve(if (resourceName.endsWith(".js")) resourceName else "$resourceName.js").normalize()
            val source = String(Files.readAllBytes(requestedModule), StandardCharsets.UTF_8)
            runtime.getExecutor(source)
                .setResourceName(requestedModule.toString())
                .compileV8Module()
        }
        val javetProxyConverter = JavetProxyConverter()
        javetProxyConverter.config.setReflectionObjectFactory(JavetReflectionObjectFactory.getInstance())

        v8runtime!!.setConverter(javetProxyConverter)

        val javetJVMInterceptor = JavetJVMInterceptor(v8runtime)
        javetJVMInterceptor.register(v8runtime!!.globalObject)

        v8runtime!!.getExecutor("globalThis.Java = { type: (clazz) => javet.package[clazz] }\n"
                + "const impl = Java.type(\"com.github.synnerz.akutz.engine.impl.Impl\").INSTANCE\n"
                + "globalThis.print = (msg) => impl.print(msg)"
        ).executeVoid()
    }

    // TODO: probably
    fun remove() {}

    fun execute(script: File) {
        v8runtime!!.getExecutor(script.readText()).setResourceName(script.path).setModule(true).executeVoid()
    }
}