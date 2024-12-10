package com.github.synnerz.akutz.engine.impl.custom

import com.caoccao.javet.enums.V8ConversionMode
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.proxy.BaseJavetReflectionProxyHandler
import com.caoccao.javet.utils.JavetReflectionUtils
import java.lang.reflect.Field

abstract class BaseReflectionProxyHandler<T : Class<*>, E : Exception>(
    val v8runtime: V8Runtime,
    val target: T
) : BaseJavetReflectionProxyHandler<T, E>(v8runtime, target) {

    override fun getCallbackContexts(): Array<JavetCallbackContext> {
        return super.callbackContexts
    }

    private fun addField(field: Field, fieldName: String? = null) {
        val name = fieldName ?: field.name
        if (classDescriptor.fieldMap.containsKey(name)) return

        JavetReflectionUtils.safeSetAccessible(field)
        classDescriptor.fieldMap[name] = field

        if (!classDescriptor.classProxyPlugin.isUniqueKeySupported(classDescriptor.targetClass))
            classDescriptor.uniqueKeySet.add(name)
    }

    override fun initializePublicFields(
        currentClass: Class<*>?,
        conversionMode: V8ConversionMode?,
        staticMode: Boolean
    ) {
        super.initializePublicFields(currentClass, conversionMode, staticMode)
    }
}