package com.github.synnerz.akutz.gui

import com.github.synnerz.akutz.Akutz
import java.io.File

object Config {
    val dataFile: File
    val data: HashMap<String, Boolean>

    init {
        val file = File(Akutz.configLocation.parentFile, "config.json")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("{ \"threadLoading\": false, \"autoUpdate\": false}")
        }
        dataFile = file
        data = Akutz.gson.fromJson(file.readText(), HashMap::class.java) as HashMap<String, Boolean>
    }

    fun save() {
        dataFile.writeText(Akutz.gson.toJson(data))
    }

    fun get(configName: String): Boolean {
        return data.getOrPut(configName) { false }
    }

    fun set(configName: String, value: Boolean) {
        data[configName] = value
    }
}
