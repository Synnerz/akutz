package com.github.synnerz.akutz.shaders.uniform

import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL20

/**
 * Taken from Elementa under MIT License
 * [Link](https://github.com/EssentialGG/Elementa/blob/c8cb78334a073ca4554cb74d49a771e5320351c5/src/main/kotlin/club/sk1er/elementa/shaders/Uniforms.kt)
 */

data class Vector2f(val x: Float, val y: Float)
data class Vector3f(val x: Float, val y: Float, val z: Float)
data class Vector4f(val x: Float, val y: Float, val z: Float, val w: Float)

val supportsShaders = OpenGlHelper.areShadersSupported()

abstract class Uniform<T>(val location: Int) {
    abstract fun setValue(value: T)
}

class FloatUniform(location: Int) : Uniform<Float>(location) {
    override fun setValue(value: Float) {
        if (supportsShaders) GL20.glUniform1f(location, value)
        else ARBShaderObjects.glUniform1fARB(location, value)
    }
}

class IntUniform(location: Int) : Uniform<Int>(location) {
    override fun setValue(value: Int) {
        if (supportsShaders) GL20.glUniform1i(location, value)
        else ARBShaderObjects.glUniform1iARB(location, value)
    }
}

class Vec4Uniform(location: Int) : Uniform<Vector4f>(location) {
    override fun setValue(value: Vector4f) {
        if (supportsShaders) GL20.glUniform4f(location, value.x, value.y, value.z, value.w)
        else ARBShaderObjects.glUniform4fARB(location, value.x, value.y, value.z, value.w)
    }
}

class Vec2Uniform(location: Int) : Uniform<Vector2f>(location) {
    override fun setValue(value: Vector2f) {
        if (supportsShaders) GL20.glUniform2f(location, value.x, value.y)
        else ARBShaderObjects.glUniform2fARB(location, value.x, value.y)
    }
}