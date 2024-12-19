package com.github.synnerz.akutz.api.libs.render.shaders

import com.github.synnerz.akutz.api.libs.FileLib
import com.github.synnerz.akutz.api.libs.render.shaders.uniform.supportsShaders
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL20

/**
 * Taken from Elementa under MIT License
 * [Link](https://github.com/EssentialGG/Elementa/blob/c8cb78334a073ca4554cb74d49a771e5320351c5/src/main/kotlin/club/sk1er/elementa/shaders/Shader.kt)
 */
open class Shader(
    private val vertSource: String,
    private val fragSource: String
) {
    private var vertShader: Int = 0
    private var fragShader: Int = 0
    private var program: Int = 0
    var usable = false

    init {
        if (!vertSource.contains("void main()") || !fragSource.contains("void main()")) {
            println("Akutz Shader: Invalid shader input.")
            throw Error("Please enter a valid Shader Source")
        }
        try {
            createShaders()
        } catch (e: Exception) {
            println("Akutz Shader: Error whilst creating the shaders")
            e.printStackTrace()
        }
    }

    open fun bind() {
        if (!usable) return

        GL20.glUseProgram(program)
    }

    open fun unbind() {
        if (!usable) return

        GL20.glUseProgram(0)
    }

    open fun delete() {
        GL20.glDeleteProgram(program)
    }

    fun getUniformLoc(name: String): Int {
        return if (supportsShaders) GL20.glGetUniformLocation(program, name)
        else ARBShaderObjects.glGetUniformLocationARB(program, name)
    }

    private fun createShaders() {
        // Create the shader program and store its ID
        program = GL20.glCreateProgram()

        // Creating vertex shader
        vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        if (supportsShaders) GL20.glShaderSource(vertShader, vertSource)
        else ARBShaderObjects.glShaderSourceARB(vertShader, vertSource)
        GL20.glCompileShader(vertShader)

        if (GL20.glGetShaderi(vertShader, GL20.GL_COMPILE_STATUS) != 1) {
            println("Akutz Shader Vertex: ${GL20.glGetShaderInfoLog(vertShader, 32768)}")
            return
        }

        // Creating fragment shader
        fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        if (supportsShaders) GL20.glShaderSource(fragShader, fragSource)
        else ARBShaderObjects.glShaderSourceARB(fragShader, fragSource)
        GL20.glCompileShader(fragShader)

        if (GL20.glGetShaderi(fragShader, GL20.GL_COMPILE_STATUS) != 1) {
            println("Akutz Shader Fragment: ${GL20.glGetShaderInfoLog(fragShader, 32768)}")
            return
        }

        // Link programs
        GL20.glAttachShader(program, vertShader)
        GL20.glAttachShader(program, fragShader)
        GL20.glLinkProgram(program)

        if (supportsShaders) {
            GL20.glDetachShader(program, vertShader)
            GL20.glDetachShader(program, fragShader)
            GL20.glDeleteProgram(vertShader)
            GL20.glDeleteProgram(fragShader)
        } else {
            ARBShaderObjects.glDetachObjectARB(program, vertShader)
            ARBShaderObjects.glDetachObjectARB(program, fragShader)
            ARBShaderObjects.glDeleteObjectARB(vertShader)
            ARBShaderObjects.glDeleteObjectARB(fragShader)
        }

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) != 1) {
            println("Akutz Shader Linking: ${GL20.glGetProgramInfoLog(program, 32768)}")
            return
        }

        // Validating program
        if (supportsShaders) GL20.glValidateProgram(program)
        else ARBShaderObjects.glValidateProgramARB(program)

        if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) != 1) {
            println("Akutz Shader Validating: ${GL20.glGetProgramInfoLog(program, 32768)}")
            return
        }

        usable = true
    }

    companion object {
        fun fromResources(vertName: String, fragName: String) =
            Shader(
                FileLib.readFromResource("assets/akutz/shaders/${vertName}.vsh")!!,
                FileLib.readFromResource("assets/akutz/shaders/${fragName}.fsh")!!
            )
    }
}