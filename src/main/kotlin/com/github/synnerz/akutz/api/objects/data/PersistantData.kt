package com.github.synnerz.akutz.api.objects.data

import com.github.synnerz.akutz.Akutz
import java.io.File
import java.nio.charset.Charset

class PersistantData(val fileName: File, initialValue: Map<String, AProperty<Any>>) : PropertyObject(initialValue) {
    constructor(moduleName: String, fileName: String, initialValue: Map<String, AProperty<Any>>) :
            this(Akutz.configLocation.resolve(moduleName).resolve(fileName), initialValue)

    init {
        load()
    }

    fun load() {
        val text = fileName.readText(Charset.forName("utf-8"))
        update(text)
    }

    fun save() {
        val text = serialize()
        fileName.writeText(text, Charset.forName("utf-8"))
    }

    init {
        INSTANCES.add(this)
    }

    companion object {
        @JvmField
        val INSTANCES: MutableSet<PersistantData> = mutableSetOf()
    }
}