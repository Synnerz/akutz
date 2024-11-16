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
        installedModules!!.forEach { classLoader!!.addAllURLs(it.jars!!.map { File(it).toURI().toURL() }) }
    }

    fun teardown() {
        classLoader?.close()
        classLoader = null
        installedModules = null
    }

    fun start() {
        installedModules!!.forEach {
            if (it.entry == null) return
            Impl.execute(File(it.directory, it.entry!!))
        }
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
            it.isDirectory
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
}