package com.github.synnerz.akutz.engine.module

import com.github.synnerz.akutz.Akutz
import com.github.synnerz.akutz.engine.impl.Impl
import java.io.File
import java.net.URL
import java.net.URLClassLoader

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/module/ModuleMetadata.kt)
 */
object ModuleManager {
    private var installedModules: List<ModuleMetadata>? = null
    private var classLoader: ModifiedURLClassLoader? = null

    fun setup() {
        Akutz.configLocation.mkdirs()

        installedModules = getFoldersInDir(Akutz.configLocation).map(ModuleManager::parseModule).distinctBy {
            it.name?.lowercase()
        }

        classLoader = ModifiedURLClassLoader()
        installedModules!!.forEach {
            var lmaoNotScuffed = false
            it.requires?.forEach { r ->
                if (installedModules!!.any { v -> v.moduleName == r }) return
                import(r)
                lmaoNotScuffed = true
            }
            if (lmaoNotScuffed) return@forEach
//            val serverMeta = ModuleUpdater.getMetadata(it.moduleName!!)
//            if (serverMeta != null) {
//                if (ModuleUpdater.compareVersion(serverMeta.version!!, it.version!!) > 0) {
//                    println("Updating module ${it.moduleName!!} from version ${it.version} to ${serverMeta.version}")
//                    ModuleUpdater.downloadModule(it.moduleName!!)
//                }
//            } else println("Failed to get metadata for module ${it.moduleName!!}")
            classLoader!!.addAllURLs(it.jars!!.map { File(it).toURI().toURL() })
        }
    }

    fun teardown() {
        classLoader?.close()
        classLoader = null
    }

    fun start() {
        installedModules!!.forEach {
            if (it.entry == null) return
            Impl.execute(File(it.directory, it.entry!!))
        }
    }

    fun import(module: String) {
        if (installedModules!!.any { it.moduleName == module }) throw Exception("Module already installed")
        if (ModuleUpdater.downloadModule(module)) {
            teardown()
            setup()
        }
    }

    fun deleteModule(moduleName: String): Boolean {
        val module = installedModules?.find { it.name?.lowercase() == moduleName.lowercase() } ?: return false
        val file = module.directory ?: return false
        check(file.exists()) { "Module directory does not exist." }

        try {
            classLoader?.close()
            if (file.deleteRecursively()) {
                Impl.shutdown()
                setup()
                return true
            }
        } catch (_: Exception) {}

        return false
    }

    private fun parseModule(dir: File): ModuleMetadata {
        val mfile = File(dir, "metadata.json")
        var metadata = ModuleMetadata()

        if (mfile.exists()) {
            try {
                metadata = Akutz.gson.fromJson(mfile.readText(), ModuleMetadata::class.java)
                // TODO: maybe change this in the future for a better "api"
                metadata.moduleName = dir.name
                metadata.directory = dir
                metadata.jars = (metadata.jars ?: emptyList()).map { File(dir, it).path }
            } catch (exception: Exception) {
                println("Module ${dir.name} has invalid metadata.json")
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