package com.github.synnerz.akutz.api.libs

import com.github.synnerz.akutz.Akutz
import java.io.*
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

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
    @JvmStatic
    fun absoluteLocation(moduleName: String, location: String): String {
        return Akutz.configLocation.path + File.separator + moduleName + File.separator + location
    }

    // We provide this method so JS can use it in case we need it for future features
    @JvmStatic
    fun readFromResource(resourceName: String): String? {
        val normalized = resourceName.replace("\\", "/")
        val name = if (normalized.startsWith("/")) normalized else "/$normalized"
        val resourceStream = javaClass.getResourceAsStream(name) ?: return null

        return resourceStream.bufferedReader().readText()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun unzip(zipFilePath: String, destDirectory: String) {
        val destDir = File(destDirectory)
        if (!destDir.exists()) destDir.mkdir()

        val zipIn = ZipInputStream(FileInputStream(zipFilePath))
        var entry: ZipEntry? = zipIn.nextEntry
        // iterates over entries in the zip file
        while (entry != null) {
            val filePath = destDirectory + File.separator + entry.name
            if (!entry.isDirectory) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath)
            } else {
                // if the entry is a directory, make the directory
                val dir = File(filePath)
                dir.mkdir()
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()
    }

    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        val toWrite = File(filePath)
        toWrite.parentFile.mkdirs()
        toWrite.createNewFile()

        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(4096)
        var read = zipIn.read(bytesIn)
        while (read != -1) {
            bos.write(bytesIn, 0, read)
            read = zipIn.read(bytesIn)
        }
        bos.close()
    }

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