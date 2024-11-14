package com.github.synnerz.akutz.api.libs

import com.github.synnerz.akutz.Akutz
import java.io.File
import java.util.Base64

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/minecraft/libs/FileLib.kt)
 */
object FileLib {
    @JvmOverloads
    @JvmStatic
    fun write(moduleName: String, fileName: String, toWrite: String, recursive: Boolean = false) {
        write(absoluteLocation(moduleName, fileName), toWrite, recursive)
    }

    @JvmOverloads
    @JvmStatic
    fun write(location: String, toWrite: String, recursive: Boolean = false) {
        File(location).apply {
            if (recursive && !exists()) parentFile.mkdirs()
        }.writeText(toWrite)
    }

    @JvmStatic
    fun append(moduleName: String, fileName: String, toAppend: String) {
        append(absoluteLocation(moduleName, fileName), toAppend)
    }

    @JvmStatic
    fun append(location: String, toAppend: String) {
        File(location).appendText(toAppend)
    }

    @JvmStatic
    fun read(moduleName: String, fileName: String): String? {
        return read(absoluteLocation(moduleName, fileName))
    }

    @JvmStatic
    fun read(location: String): String? {
        return read(File(location))
    }

    @JvmStatic
    fun read(file: File): String? {
        return try {
            file.readText()
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun exists(moduleName: String, fileName: String): Boolean {
        return exists(absoluteLocation(moduleName, fileName))
    }

    @JvmStatic
    fun exists(location: String): Boolean {
        return File(location).exists()
    }

    @JvmStatic
    fun isDirectory(moduleName: String, fileName: String): Boolean {
        return isDirectory(absoluteLocation(moduleName, fileName))
    }

    @JvmStatic
    fun isDirectory(location: String): Boolean {
        return File(location).isDirectory
    }

    @JvmStatic
    fun delete(location: String): Boolean {
        return File(location).delete()
    }

    @JvmStatic
    fun deleteDir(location: String): Boolean {
        return deleteDir(File(location))
    }

    @JvmStatic
    fun deleteDir(dir: File): Boolean {
        return dir.deleteRecursively()
    }

    // ChatTriggers itself does not provide this as public api, but it might be useful
    // to have as public for some users
    fun absoluteLocation(moduleName: String, location: String): String {
        return Akutz.configLocation.path + File.separator + moduleName + File.separator + location
    }

    // TODO: unzip feature

    // Making a named object did not work in the JS side, so we are back
    // to it being a field
    @JvmField
    val base64 = object {
        fun encode(toEncode: String): String {
            return Base64.getEncoder().encodeToString(toEncode.toByteArray())
        }

        fun decode(toDecode: String): String {
            return String(Base64.getDecoder().decode(toDecode))
        }
    }
}