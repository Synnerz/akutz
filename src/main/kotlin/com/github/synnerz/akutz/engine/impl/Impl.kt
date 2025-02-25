package com.github.synnerz.akutz.engine.impl

import com.caoccao.javet.buddy.interop.proxy.JavetReflectionObjectFactory
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.callback.IJavetDirectCallable.NoThisAndResult
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.callback.JavetCallbackType
import com.caoccao.javet.interop.converters.JavetObjectConverter
import com.caoccao.javet.interop.engine.IJavetEnginePool
import com.caoccao.javet.interop.engine.JavetEngineConfig
import com.caoccao.javet.interop.engine.JavetEnginePool
import com.caoccao.javet.utils.V8ValueUtils
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.*
import com.github.synnerz.akutz.Akutz
import com.github.synnerz.akutz.api.commands.Command
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.events.ForgeEvent
import com.github.synnerz.akutz.api.libs.FileLib
import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.libs.render.Tessellator
import com.github.synnerz.akutz.api.objects.keybind.Keybind
import com.github.synnerz.akutz.api.objects.render.Image
import com.github.synnerz.akutz.api.wrappers.Client
import com.github.synnerz.akutz.console.Console
import com.github.synnerz.akutz.console.Console.printError
import com.github.synnerz.akutz.engine.impl.custom.EngineCache
import com.github.synnerz.akutz.engine.impl.custom.JVMInterceptor
import com.github.synnerz.akutz.engine.impl.custom.ProxyConverter
import com.github.synnerz.akutz.engine.impl.custom.event.EventLoop
import com.github.synnerz.akutz.engine.impl.custom.event.timers.TimerHandler
import com.github.synnerz.akutz.engine.module.ModuleGui
import com.github.synnerz.akutz.engine.module.ModuleManager
import com.github.synnerz.akutz.gui.Config
import com.github.synnerz.akutz.listeners.MouseListener
import net.minecraft.launchwrapper.Launch
import java.io.File
import java.net.URL
import java.net.URLConnection
import java.nio.file.Paths
import java.security.KeyStore
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.io.path.isDirectory

object Impl {
    private var enginePool: IJavetEnginePool<V8Runtime> =
        JavetEnginePool(JavetEngineConfig().setGCBeforeEngineClose(true))
    private var v8runtime: V8Runtime? = null
    private var eventLoop: EventLoop? = null
    private var timerHandler: TimerHandler? = null
    private var javetJVMInterceptor: JVMInterceptor? = null
    private var javetProxyConverter: ProxyConverter? = null
    private var modulesLoaded = mutableMapOf<String, IV8Module>()
    val inDev = Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false) as Boolean
    var mappings: HashMap<String, Any>? = null
        internal set

    /**
     * * Taken from ChatTriggers under MIT License
     * * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/CTJS.kt)
     */
    val sslContext by lazy {
        try {
            val kstore = KeyStore.getInstance("JKS")
            kstore.load(this::class.java.getResourceAsStream("/azkeystore.jks"), "changeit".toCharArray())
            val keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            keyFactory.init(kstore, null)
            trustFactory.init(kstore)
            val ctx = SSLContext.getInstance("TLS")
            ctx?.init(keyFactory.keyManagers, trustFactory.trustManagers, null)
            ctx
        } catch (e: Exception) {
            printError("Failed to load keystore")
            e.printError()
            e.printStackTrace()
            null
        }
    }

    fun loadModuleDynamic(caller: String, path: String, cb: (IV8ValueObject?) -> Unit) {
        try {
            v8runtime ?: return cb(null)
            val src = Paths.get(Akutz.configLocation.path)
                .resolve(caller).parent
                .resolve(path).normalize()
            val resourceName = src.toFile().canonicalPath

            val cachedModule = modulesLoaded[resourceName]
            if (cachedModule != null) return cb(cachedModule.namespace as IV8ValueObject)

            val mod = v8runtime?.getExecutor(src)?.setResourceName(resourceName)?.compileV8Module()
            val res = mod?.execute<V8ValuePromise>()
            res ?: return cb(null)
            modulesLoaded[resourceName] = mod
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
            v8runtime?.await()
        } catch (e: Exception) {
            e.printError()
            e.printStackTrace()
        }
    }

    fun setup() {
        if (enginePool.releasedEngineCount <= 0) enginePool =
            JavetEnginePool(JavetEngineConfig().setGCBeforeEngineClose(true))

        v8runtime = enginePool.engine.v8Runtime
        EngineCache.load(v8runtime!!)

        v8runtime!!.setV8ModuleResolver { runtime, resourceName, v8ModuleReferrer ->
            val dir = Paths.get(v8ModuleReferrer.resourceName).parent
            val resourcePath = dir.resolve(resourceName)
            val requestedModule =
                if (resourcePath.isDirectory()) resourcePath.resolve("index.js").normalize()
                else dir
                    .resolve("$resourceName${if (resourceName.endsWith(".js")) "" else ".js"}")
                    .normalize()
            val moduleName = requestedModule.toFile().canonicalPath

            val module = modulesLoaded[moduleName]
                ?: runtime.getExecutor(requestedModule)
                    .setResourceName(moduleName)
                    .compileV8Module()

            modulesLoaded["$requestedModule"] = module

            return@setV8ModuleResolver module
        }
        javetProxyConverter = ProxyConverter()
        javetProxyConverter!!.config.setProxyListEnabled(true)
        javetProxyConverter!!.config.setProxyMapEnabled(true)
        javetProxyConverter!!.config.setProxySetEnabled(true)
        javetProxyConverter!!.config.setReflectionObjectFactory(JavetReflectionObjectFactory.getInstance())

        v8runtime!!.setConverter(javetProxyConverter!!)

        javetJVMInterceptor = JVMInterceptor(v8runtime)
        javetJVMInterceptor!!.addCallbackContexts(
            JavetCallbackContext(
                "extend",
                this, JavetCallbackType.DirectCallNoThisAndResult,
                NoThisAndResult<java.lang.Exception?> { v8Values: Array<V8Value?> ->
                    if (v8Values.size >= 2) {
                        val obj: Any = v8runtime!!.toObject(v8Values[0])
                        if (obj is Class<*>) {
                            val v8ValueObject = V8ValueUtils.asV8ValueObject(v8Values, 1)
                            if (v8ValueObject != null) {
                                val childClass = JavetReflectionObjectFactory.getInstance()
                                    .extend(obj, v8ValueObject)
                                return@NoThisAndResult v8runtime!!.toV8Value(childClass)
                            }
                        }
                    }
                    v8runtime!!.createV8ValueUndefined()
                })
        )
        javetJVMInterceptor!!.register(v8runtime!!.globalObject)

        v8runtime!!.getExecutor(
            FileLib.readFromResource("js/providedLibs.js")!!
        ).setResourceName("js/providedLibs.js").setModule(true).executeVoid()
    }

    fun clear() {
        if (!isLoaded()) return

        timerHandler!!.close()
        eventLoop!!.close()

        if (javetJVMInterceptor != null) {
            javetJVMInterceptor!!.unregister(v8runtime!!.globalObject)
            javetJVMInterceptor = null
        }
        if (javetProxyConverter != null) {
            JavetReflectionObjectFactory.getInstance().clear()
            javetProxyConverter!!.config.setReflectionObjectFactory(null)
            javetProxyConverter = null
        }
        for (v8Module in modulesLoaded) {
            v8runtime!!.removeV8Module(v8Module.value.resourceName)
        }
        modulesLoaded.clear()
        ForgeEvent.unregisterEvents()
        Loader.clearEvents()
        v8runtime!!.lowMemoryNotification()
        enginePool.releaseEngine(enginePool.engine)
        v8runtime!!.close()
        EngineCache.clear()
        v8runtime = null
    }

    fun execute(script: File, moduleName: String) {
        val resourceName = script.canonicalPath
        if (modulesLoaded.containsKey(resourceName)) return

        val module = v8runtime!!
            .getExecutor(script.readText())
            .setResourceName(resourceName)
            .setModule(true)
            .compileV8Module()
        try {
            // Scuffed workaround but it works for stacktrace
            val promise = module.execute<V8ValuePromise>()
            v8runtime!!.await()
            val result = (promise as V8ValuePromise).getResult<V8Value>()
            if (result is V8ValueError) {
                printError("Error in module \"$moduleName\" ${module.exception?.stack ?: "No stacktrace"}")
                throw IllegalArgumentException("Error in module \"$moduleName\" ${module.exception?.stack ?: "No stacktrace"}")
            }
            modulesLoaded[module.resourceName] = module
        } catch (e: IllegalArgumentException) {
            v8runtime!!.removeV8Module(module)
            modulesLoaded.remove(module.resourceName)
            e.printStackTrace()
        } catch (e: Exception) {
            v8runtime!!.removeV8Module(module)
            modulesLoaded.remove(module.resourceName)
            printError("Error in module \"$moduleName\"")
            e.printError()
            e.printStackTrace()
        }
    }

    fun isLoaded(): Boolean = v8runtime != null

    private val javetObjectConverter = JavetObjectConverter()
    fun forceWrap(obj: Any?) = javetObjectConverter.toV8Value<V8Value>(v8runtime, obj)

    fun shutdown() {
        EventType.Unload.triggerAll()
        ModuleGui.markDirty()
        Tessellator.pushedMatrix = 0
        Renderer.pushedMatrix = 0
        clear()
        ModuleManager.teardown()
        Loader.clearEvents()
        Command.activeCommands.values.toList().forEach(Command::unregister)
        Command.activeCommands.clear()
        Client.scheduleTask {
            Image.IMAGES.forEach(Image::destroy)
            Image.IMAGES.clear()
        }
        Keybind.clearKeybinds()
        MouseListener.clearListeners()
        Console.clearConsole()
        Config.save()
    }

    internal fun loadMappings() {
        val json = FileLib.readFromResource("mappings.json")
        mappings = Akutz.gson.fromJson(json, HashMap::class.java) as HashMap<String, Any>?
    }

    /**
     * * This acts essentially as the event loop handler
     * that way we can provide it to the "user" although it's more used internally
     * to make <set>Timeout/Interval/Immediate
     */
    fun getTimersHandler() = timerHandler

    internal fun setupEventLoop() {
        eventLoop = EventLoop(v8runtime!!)
        timerHandler = TimerHandler(eventLoop!!)
    }

    @JvmStatic
    fun openConnection(url: String): URLConnection {
        val connection = URL(url).openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
        if (connection is HttpsURLConnection && sslContext != null) {
            connection.sslSocketFactory = sslContext!!.socketFactory
        }
        connection.connectTimeout = 3000
        connection.readTimeout = 3000

        return connection
    }
}