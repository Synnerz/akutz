package com.github.synnerz.akutz.engine.impl

import com.caoccao.javet.buddy.interop.proxy.JavetReflectionObjectFactory
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.converters.JavetProxyConverter
import com.caoccao.javet.interop.engine.IJavetEnginePool
import com.caoccao.javet.interop.engine.JavetEnginePool
import com.caoccao.javet.values.reference.V8Module
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Paths

object Impl {
    private val enginePool: IJavetEnginePool<V8Runtime> = JavetEnginePool()
    private var v8runtime: V8Runtime? = null
    private var javetJVMInterceptor: JavetJVMInterceptor? = null
    private var modulesLoaded = mutableListOf<V8Module>()

    fun print(msg: Any) {
        println(msg)
    }

    fun setup() {
        v8runtime = enginePool.engine.v8Runtime
        v8runtime!!.setPromiseRejectCallback { jevent, valpromise, value ->
            println("event: $jevent")
            println("valPromise: $valpromise")
            println("value: $value")
        }
        v8runtime!!.setV8ModuleResolver { runtime, resourceName, v8ModuleReferrer ->
            val requestedModule = Paths.get(v8ModuleReferrer.resourceName)
                .parent
                ?.resolve("$resourceName${if (resourceName.endsWith(".js")) "" else ".js"}")
                ?.normalize()
            val module = runtime.getExecutor(requestedModule!!)
                ?.compileV8Module()

            modulesLoaded.add(module!!)

            return@setV8ModuleResolver module
        }
        val javetProxyConverter = JavetProxyConverter()
        javetProxyConverter.config.setReflectionObjectFactory(JavetReflectionObjectFactory.getInstance())

        v8runtime!!.setConverter(javetProxyConverter)

        javetJVMInterceptor = JavetJVMInterceptor(v8runtime)
        javetJVMInterceptor!!.register(v8runtime!!.globalObject)

        v8runtime!!.getExecutor(
            Impl::class.java.classLoader.getResourceAsStream("javascript/providedLibs.js")!!
                .bufferedReader(Charset.defaultCharset()).use { it.readText() }
        ).executeVoid()
    }

    fun clear() {
        if (javetJVMInterceptor != null) {
            javetJVMInterceptor!!.register(v8runtime!!.globalObject)
        }
        if (v8runtime == null) return
        for (v8Module in modulesLoaded) {
            v8runtime!!.removeV8Module(v8Module.resourceName)
            println("removed module: ${v8Module.resourceName}")
        }
        modulesLoaded.clear()
        v8runtime!!.lowMemoryNotification()
        v8runtime = null
    }

    fun execute(script: File) {
        v8runtime!!.getExecutor(script.readText()).setResourceName(script.path).setModule(true).executeVoid()
    }
}