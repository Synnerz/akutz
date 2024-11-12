package com.github.synnerz.akutz.engine.module

import com.github.synnerz.akutz.Akutz
import com.github.synnerz.akutz.engine.impl.Impl
import java.io.File

// Taken from ChatTriggers under MIT license
// https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/module/ModuleMetadata.kt
object ModuleManager {
    fun setup() {
        Akutz.configLocation.mkdirs()

        val installedModules = getFoldersInDir(Akutz.configLocation).map(ModuleManager::parseModule).distinctBy {
            it.name?.lowercase()
        }

        installedModules.forEach {
            if (it.entry == null) return
            Impl.execute(File(it.directory, it.entry!!))
        }
    }

    private fun parseModule(dir: File) : ModuleMetadata {
        val mfile = File(dir, "metadata.json")
        var metadata = ModuleMetadata()

        if (mfile.exists()) {
            try {
                metadata = Akutz.gson.fromJson(mfile.readText(), ModuleMetadata::class.java)
                // TODO: maybe change this in the future for a better "api"
                metadata.moduleName = dir.name
                metadata.directory = dir
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
}