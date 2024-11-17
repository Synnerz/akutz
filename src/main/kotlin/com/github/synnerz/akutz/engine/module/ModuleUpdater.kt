package com.github.synnerz.akutz.engine.module

import com.github.synnerz.akutz.Akutz
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.zip.*

object ModuleUpdater {
    private fun getUrl(module: String, file: String) =
        URL("https://raw.githubusercontent.com/Synnerz/AkutzModules/refs/heads/main/modules/$module/$file")

    fun getMetadata(module: String): ModuleMetadata? {
        try {
            val url = getUrl(module, "metadata.json")
            val str = url.readText(Charset.forName("UTF-8"))
            return Akutz.gson.fromJson(str, ModuleMetadata::class.java)
        } catch (_: Exception) {
            return null
        }
    }

    fun compareVersion(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toInt() }
        val v2Parts = version2.split(".").map { it.toInt() }

        for (i in 0 until 3) {
            when {
                v1Parts[i] < v2Parts[i] -> return -1
                v1Parts[i] > v2Parts[i] -> return 1
            }
        }

        return 0
    }

    fun downloadModule(module: String): Boolean {
        val url = getUrl(module, "download.zip")
        val zipPath = File(Akutz.configLocation, "$module-download.zip")
        try {
            url.openStream()?.use { stream ->
                zipPath.outputStream().use { stream.copyTo(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Module with name $module was not found in the repo")
            return false
        }

        val dest = File(Akutz.configLocation, module)
        dest.mkdirs()

        ZipInputStream(zipPath.inputStream()).use { zipInputStream ->
            var zipEntry: ZipEntry?

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                val extractedFile = File(dest, zipEntry!!.name)

                if (zipEntry!!.isDirectory) extractedFile.mkdirs()
                else {
                    extractedFile.parentFile.mkdirs()
                    extractedFile.outputStream().use { zipInputStream.copyTo(it) }
                }

                zipInputStream.closeEntry()
            }
        }

        zipPath.delete()
        return true
    }
}