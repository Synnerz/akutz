package com.github.synnerz.akutz.engine.impl

import com.caoccao.javet.buddy.interop.proxy.JavetReflectionObjectFactory
import com.caoccao.javet.interception.jvm.JavetJVMInterceptor
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.converters.JavetObjectConverter
import com.caoccao.javet.interop.converters.JavetProxyConverter
import com.caoccao.javet.interop.engine.IJavetEnginePool
import com.caoccao.javet.interop.engine.JavetEngineConfig
import com.caoccao.javet.interop.engine.JavetEnginePool
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.*
import com.github.synnerz.akutz.Akutz
import com.github.synnerz.akutz.api.commands.Command
import com.github.synnerz.akutz.api.events.ForgeEvent
import com.github.synnerz.akutz.api.libs.FileLib
import com.github.synnerz.akutz.api.objects.render.Image
import com.github.synnerz.akutz.engine.module.ModuleManager
import java.io.File
import java.nio.file.Paths

object Impl {
    private var enginePool: IJavetEnginePool<V8Runtime> = JavetEnginePool(JavetEngineConfig().setGCBeforeEngineClose(true))
    private var v8runtime: V8Runtime? = null
    private var javetJVMInterceptor: JavetJVMInterceptor? = null
    private var javetProxyConverter: JavetProxyConverter? = null
    private var modulesLoaded = mutableListOf<IV8Module>()

    fun loadModuleDynamic(caller: String, path: String, cb: (IV8ValueObject?) -> Unit) {
        v8runtime ?: return cb(null)
        val src = Paths.get(
            Paths.get(Akutz.configLocation.path).toString(),
            Paths.get(caller).parent.resolve(path).normalize().toString()
        ).normalize()
        val resourceName = src.toString()

        val lockF = v8runtime!!.javaClass.getDeclaredField("v8ModuleLock")
        lockF.setAccessible(true)
        val mapF = v8runtime!!.javaClass.getDeclaredField("v8ModuleMap")
        mapF.setAccessible(true)
        synchronized(lockF.get(v8runtime)) {
            val mod = (mapF.get(v8runtime) as Map<*, *>)[resourceName]
            if (mod != null) return cb((mod as IV8Module).namespace as IV8ValueObject)
        }

        val mod = v8runtime?.getExecutor(src)?.compileV8Module()
        val res = mod?.execute<V8ValuePromise>()
        res ?: return cb(null)
        modulesLoaded.add(mod)
        res.register(object : IV8ValuePromise.IListener {
            override fun onCatch(v8Value: V8Value?) {
                cb(null)
            }

            override fun onFulfilled(v8Value: V8Value?) {
                cb(mod.namespace as IV8ValueObject)
            }

            override fun onRejected(v8Value: V8Value?) {
                cb(null)
            }
        })
    }

    fun setup() {
        if (enginePool.releasedEngineCount <= 0) enginePool = JavetEnginePool(JavetEngineConfig().setGCBeforeEngineClose(true))

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
        javetProxyConverter = JavetProxyConverter()
        javetProxyConverter!!.config.setProxyListEnabled(true)
        javetProxyConverter!!.config.setProxyMapEnabled(true)
        javetProxyConverter!!.config.setProxySetEnabled(true)
        javetProxyConverter!!.config.setReflectionObjectFactory(JavetReflectionObjectFactory.getInstance())

        v8runtime!!.setConverter(javetProxyConverter!!)

        javetJVMInterceptor = JavetJVMInterceptor(v8runtime)
        javetJVMInterceptor!!.register(v8runtime!!.globalObject)

        v8runtime!!.getExecutor(
            FileLib.readFromResource("js/providedLibs.js")!!
        ).setResourceName("js/providedLibs.js").setModule(true).executeVoid()
    }

    fun clear() {
        if (!isLoaded()) return

        if (javetJVMInterceptor != null) {
            javetJVMInterceptor!!.unregister(v8runtime!!.globalObject)
            javetJVMInterceptor = null
        }
        if (javetProxyConverter != null) {
            javetProxyConverter!!.config.setReflectionObjectFactory(null)
            javetProxyConverter = null
        }
        for (v8Module in modulesLoaded) {
            v8runtime!!.removeV8Module(v8Module.resourceName)
        }
        modulesLoaded.clear()
        ForgeEvent.unregisterEvents()
        Loader.clearEvents()
        v8runtime!!.lowMemoryNotification()
        enginePool.releaseEngine(enginePool.engine)
        v8runtime!!.close()
        v8runtime = null
    }

    fun execute(script: File) {
        val module = v8runtime!!.getExecutor(script.readText()).setResourceName(script.path).setModule(true).compileV8Module()
        try {
            module.executeVoid()
            modulesLoaded.add(module)
        } catch (e: Exception) {
            v8runtime!!.removeV8Module(module)
            modulesLoaded.remove(module)
            e.printStackTrace()
        }
    }

    fun isLoaded() : Boolean = v8runtime != null

    private val javetObjectConverter = JavetObjectConverter()
    fun forceWrap(obj: Any?) = javetObjectConverter.toV8Value<V8Value>(v8runtime, obj)

    fun shutdown() {
        clear()
        ModuleManager.teardown()
        Loader.clearEvents()
        Command.activeCommands.forEach { (_, cmd) -> cmd.unregister() }
        Image.IMAGES.toList().forEach { it.destroy() }
    }
}