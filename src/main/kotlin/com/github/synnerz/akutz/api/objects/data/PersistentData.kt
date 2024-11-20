package com.github.synnerz.akutz.api.objects.data

import com.github.synnerz.akutz.Akutz
import java.io.File
import java.nio.charset.Charset

class PersistentData(val fileName: File, initialValue: Map<String, AProperty<*>>) : PropertyObject(initialValue) {
    constructor(moduleName: String, fileName: String, initialValue: Map<String, AProperty<*>>) :
            this(Akutz.configLocation.resolve(moduleName).resolve(fileName), initialValue)

    init {
        load()
        INSTANCES.add(this)
    }

    fun load() {
        val text = fileName.readText(Charset.forName("utf-8"))
        update(text)
    }

    fun save() {
        val text = serialize()
        fileName.writeText(text, Charset.forName("utf-8"))
    }

    companion object {
        @JvmField
        val INSTANCES: MutableSet<PersistentData> = mutableSetOf()
    }
}