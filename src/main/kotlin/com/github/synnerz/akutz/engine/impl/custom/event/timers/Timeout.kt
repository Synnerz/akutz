package com.github.synnerz.akutz.engine.impl.custom.event.timers

import com.caoccao.javet.enums.V8ValueSymbolType
import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.interop.callback.IJavetDirectCallable
import com.caoccao.javet.interop.callback.IJavetDirectCallable.*
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.callback.JavetCallbackType
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.reference.V8ValueFunction

class Timeout(
    timerHandler: TimerHandler,
    recurrent: Boolean,
    callback: V8ValueFunction?,
    idelay: Int
) : BaseTimersFunction(timerHandler, recurrent, callback, idelay), IJavetDirectCallable {
    override fun getCallbackContexts(): Array<JavetCallbackContext> {
        if (javetCallbackContexts == null) {
            javetCallbackContexts = listOf(
                JavetCallbackContext(
                    "hasRef",
                    this, JavetCallbackType.DirectCallNoThisAndResult,
                    NoThisAndResult<Exception?> { values: Array<out V8Value> -> this.hasRef(*values) }),
                JavetCallbackContext(
                    "toPrimitive",
                    V8ValueSymbolType.BuiltIn,
                    this, JavetCallbackType.DirectCallGetterAndNoThis,
                    GetterAndNoThis<Exception?> { getReferenceIdFunction() }
                ),
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
                    }),
                JavetCallbackContext(
                    "refresh",
                    this, JavetCallbackType.DirectCallThisAndResult,
                    ThisAndResult<Exception?> { thisObject, v8Values -> refresh(thisObject, *v8Values) }
                ),
            )
        }
        return javetCallbackContexts!!.toTypedArray()
    }

    @Throws(JavetException::class)
    fun getReferenceIdFunction(vararg v8Values: V8Value?): V8ValueFunction {
        val stringBuilder = StringBuilder("() => ").append(getReferenceId())
        return getEventLoop().v8runtime.createV8ValueFunction(stringBuilder.toString())
    }

    fun refresh(thisObject: V8Value, vararg v8Values: V8Value?): V8Value {
        if (hasRef()) {
            cancel()
            run()
        }
        return thisObject
    }
}
