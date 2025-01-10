package com.github.synnerz.akutz.engine.impl.custom.event.timers

import com.caoccao.javet.interop.callback.IJavetDirectCallable
import com.caoccao.javet.interop.callback.IJavetDirectCallable.NoThisAndResult
import com.caoccao.javet.interop.callback.IJavetDirectCallable.ThisAndResult
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.callback.JavetCallbackType
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueFunction

class Immediate(
    timerHandler: TimerHandler,
    v8func: V8ValueFunction?
) : BaseTimersFunction(timerHandler, false, v8func, 1), IJavetDirectCallable {
    override fun getCallbackContexts(): Array<JavetCallbackContext> {
        if (javetCallbackContexts == null) {
            javetCallbackContexts = listOf(
                JavetCallbackContext(
                    "hasRef",
                    this, JavetCallbackType.DirectCallNoThisAndResult,
                    NoThisAndResult<Exception?> { values: Array<out V8Value> ->
                        this.hasRef(
                            *values
                        )
                    }),
                JavetCallbackContext(
                    "ref",
                    this, JavetCallbackType.DirectCallThisAndResult,
                    ThisAndResult<Exception?> { thisObject: V8Value?, values: Array<out V8Value> ->
                        this.ref(
                            thisObject!!, *values
                        )
                    }),
                JavetCallbackContext(
                    "unref",
                    this, JavetCallbackType.DirectCallThisAndResult,
                    ThisAndResult<Exception?> { thisObject: V8Value?, values: Array<out V8Value> ->
                        this.unref(
                            thisObject!!, *values
                        )
                    })
            )
        }

        return javetCallbackContexts!!.toTypedArray()
    }
}
