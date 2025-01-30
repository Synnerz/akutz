package com.github.synnerz.akutz.engine.impl.custom

import com.caoccao.javet.enums.V8ConversionMode
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.proxy.BaseJavetReflectionProxyHandler
import com.caoccao.javet.utils.JavetReflectionUtils
import com.caoccao.javet.values.V8Value
import com.github.synnerz.akutz.engine.impl.Impl
import com.google.gson.internal.LinkedTreeMap
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

abstract class BaseReflectionProxyHandler<T, E : Exception>(
    val v8runtime: V8Runtime,
    val target: T
) : BaseJavetReflectionProxyHandler<T, E>(v8runtime, target) {

    override fun getCallbackContexts(): Array<JavetCallbackContext> {
        return super.callbackContexts
    }

    private fun addCField(field: Field, fieldName: String? = null) {
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
        if (!Impl.inDev && Impl.mappings?.containsKey(currentClass?.name) == true) {
            val mappings = Impl.mappings?.get(currentClass?.name) as? LinkedTreeMap<String, Any> ?: return
            val fields = mappings["fields"] as? LinkedTreeMap<String, String> ?: return

            for (field in currentClass?.declaredFields!!) {
                val name = fields[field.name]
                val mods = field.modifiers
                if (staticMode && !Modifier.isStatic(mods) || !isAllowed(conversionMode, field)) continue
                if (name == null) {
                    addCField(field)
                    continue
                }
                if (name == "t") continue

                addCField(field, name)
            }
            return
        }
        super.initializePublicFields(currentClass, conversionMode, staticMode)
    }

    private fun addCMethod(method: Method, methodName: String? = null) {
        val name = methodName ?: method.name

        JavetReflectionUtils.safeSetAccessible(method)
        val map = classDescriptor.methodsMap
        val methods = map.computeIfAbsent(name) { _ -> ArrayList() }
        methods.add(method)
    }

    override fun initializePublicMethods(
        currentClass: Class<*>?,
        conversionMode: V8ConversionMode?,
        staticMode: Boolean
    ) {
        if (!Impl.inDev && Impl.mappings?.containsKey(currentClass?.name) == true) {
            val mappings = Impl.mappings?.get(currentClass?.name) as? LinkedTreeMap<String, Any> ?: return
            val methods = mappings["methods"] as? LinkedTreeMap<String, String> ?: return

            for (method in currentClass?.declaredMethods!!) {
                val name = methods[method.name]
                val mods = method.modifiers
                if (staticMode && !Modifier.isStatic(mods) || !isAllowed(conversionMode, method)) continue
                if (name == null) {
                    addCMethod(method)
                    continue
                }
                if (name == "t") continue

                addCMethod(method, name)
            }
            return
        }
        super.initializePublicMethods(currentClass, conversionMode, staticMode)
    }

    override fun addMethod(method: Method?, startIndex: Int, map: MutableMap<String, MutableList<Method>>?) {
        if (method == null || map == null) return

        val methodName = method.name
        val methodList = map.computeIfAbsent(methodName) { ArrayList() }

        methodList.add(method)
    }

    override fun getPrototypeOf(target: V8Value?): V8Value {
        val value = ProxyPrototypeStore.getPrototype(
            v8runtime, proxyMode, classDescriptor.targetClass
        )
        if (value != null) return value

        return super.getPrototypeOf(target)
    }
}