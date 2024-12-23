package com.github.synnerz.akutz.engine.module

import java.io.File

// Taken from ChatTriggers under MIT license
// https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/engine/module/ModuleMetadata.kt
data class ModuleMetadata(
    val name: String? = null,
    val description: String? = null,
    val version: String? = null,
    var entry: String? = null,
    val creator: String? = null,
    var directory: File? = null,
    var moduleName: String? = null,
    var jars: List<String>? = null,
    var requires: List<String>? = null
)