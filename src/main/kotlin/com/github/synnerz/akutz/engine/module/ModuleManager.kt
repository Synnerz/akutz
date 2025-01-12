package com.github.synnerz.akutz.engine.module

import com.github.synnerz.akutz.Akutz
import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.console.Console
import com.github.synnerz.akutz.console.Console.printError
import com.github.synnerz.akutz.engine.impl.Impl
import com.github.synnerz.akutz.gui.Config
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.concurrent.thread

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/module/ModuleMetadata.kt)
 */
object ModuleManager {
    private var installedModules: List<ModuleMetadata>? = null
    // private var classLoader: ModifiedURLClassLoader? = null

    fun setup() {
        Akutz.configLocation.mkdirs()

        installedModules = getFoldersInDir(Akutz.configLocation).map(ModuleManager::parseModule).distinctBy {
            it.name?.lowercase()
        }

        // classLoader = ModifiedURLClassLoader()
        installedModules!!.forEach {
            if (importRequires(it)) {
                teardown()
                setup()
                return
            }
            if (Config.get("autoUpdate") && Config.get("threadLoading")) {
                // TODO: just change this to be cached and only do website request after 5mins if it was cached
                thread {
                    val serverMeta = ModuleUpdater.getMetadata(it.moduleName!!)
                    if (serverMeta != null) {
                        if (ModuleUpdater.compareVersion(serverMeta.version!!, it.version!!) > 0) {
                            Console.println("Updating module ${it.moduleName!!} from version ${it.version} to ${serverMeta.version}")
                            ModuleUpdater.downloadModule(it.moduleName!!)
                        }
                    } else printError("Failed to get metadata for module ${it.moduleName!!}")
                }
            }
            // classLoader!!.addAllURLs(it.jars!!.map { File(it).toURI().toURL() })
        }
        Impl.setupEventLoop()
    }

    fun teardown() {
        // classLoader?.close()
        // classLoader = null
    }

    fun start() {
        for (module in installedModules!!) {
            // Avoid loading modules that don't have entry or name
            // if a module does not have a name it will fail when attempting to find it inside
            // the cached modules [installedModules]
            if (module.entry == null || module.name == null) continue
            Impl.execute(File(module.directory, module.entry!!), module.moduleName!!)
        }
        EventType.Load.triggerAll()
    }

    fun importRequires(metadata: ModuleMetadata): Boolean {
        if (metadata.requires == null || metadata.requires!!.isEmpty()) return false

        var importedModule = false

        for (name in metadata.requires!!) {
            if (isModuleInstalled(name)) continue
            ModuleUpdater.downloadModule(name)
            importedModule = true
        }

        return importedModule
    }

    fun import(module: String): Boolean {
        if (isModuleInstalled(module)) return false
        thread {
            if (ModuleUpdater.downloadModule(module)) {
                teardown()
                setup()
            }
        }
        return true
    }

    fun deleteModule(moduleName: String): Boolean {
        val module = installedModules?.find { it.name?.lowercase() == moduleName.lowercase() } ?: return false
        val file = module.directory ?: return false
        if (!file.exists()) {
            printError("Module directory for module \"${module.name}\" does not exist")
            return false
        }

        try {
            // classLoader?.close()
            if (file.deleteRecursively()) {
                Impl.shutdown()
                Impl.setup()
                setup()
                start()
                return true
            }
        } catch (e: Exception) {
            e.printError()
            e.printStackTrace()
        }

        return false
    }

    fun isModuleInstalled(name: String):
            Boolean = installedModules?.any { it.name?.lowercase() == name.lowercase() } ?: false

    private fun parseModule(dir: File): ModuleMetadata {
        val mfile = File(dir, "metadata.json")
        var metadata = ModuleMetadata()

        if (mfile.exists()) {
            try {
                metadata = Akutz.gson.fromJson(mfile.readText(), ModuleMetadata::class.java)
                // TODO: maybe change this in the future for a better "api"
                metadata.moduleName = dir.name
                metadata.directory = dir
                // TODO: currently disabled
                // metadata.jars = (metadata.jars ?: emptyList()).map { File(dir, it).path }
            } catch (exception: Exception) {
                printError("Module ${dir.name} has invalid metadata.json")
                exception.printError()
            }
        }

        return metadata
    }

    private fun getFoldersInDir(dir: File): List<File> {
        if (!dir.isDirectory) return emptyList()

        return dir.listFiles()?.filter {
            it.isDirectory && File(it, "metadata.json").exists()
        } ?: listOf()
    }

    private class ModifiedURLClassLoader : URLClassLoader(arrayOf(), javaClass.classLoader) {
        val sources = mutableSetOf<URL>()

        fun addAllURLs(urls: List<URL>) {
            (urls - sources).forEach(::addURL)
        }

        public override fun addURL(url: URL) {
            super.addURL(url)
            sources.add(url)
        }
    }

    internal fun getCrashList(): MutableList<String> {
        val mut = mutableListOf<String>()

        installedModules?.forEach {
            mut.add("Module{name=${it.name}, version=${it.version}}")
        }

        return mut
    }

    internal fun getInstalledModules(): List<ModuleMetadata>? = installedModules
}