package com.github.synnerz.akutz.engine.impl.custom.event

import com.caoccao.javet.interfaces.IJavetClosable
import com.caoccao.javet.interop.callback.JavetCallbackContext

abstract class BaseCallable : IJavetClosable {
    var javetCallbackContexts: List<JavetCallbackContext>? = null

    override fun close() {
        javetCallbackContexts = null
    }

    override fun isClosed(): Boolean {
        return javetCallbackContexts == null
    }
}
