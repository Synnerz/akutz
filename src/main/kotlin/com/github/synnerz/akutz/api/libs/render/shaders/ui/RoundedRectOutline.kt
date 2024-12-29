package com.github.synnerz.akutz.api.libs.render.shaders.ui

import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.api.libs.render.shaders.Shader
import com.github.synnerz.akutz.api.libs.render.shaders.uniform.FloatUniform
import com.github.synnerz.akutz.api.libs.render.shaders.uniform.Vec4Uniform
import com.github.synnerz.akutz.api.libs.render.shaders.uniform.Vector4f

object RoundedRectOutline {
    private val shader = Shader.fromResources("ui/rect/rect", "ui/rect/rounded_rect_outline")
    private val shaderRadiusUniform: FloatUniform = FloatUniform(shader.getUniformLoc("u_Radius"))
    private val shaderInnerRectUniform: Vec4Uniform = Vec4Uniform(shader.getUniformLoc("u_InnerRect"))
    private val shaderOutlineWidth: FloatUniform = FloatUniform(shader.getUniformLoc("u_OutlineWidth"))

    fun drawRoundedRectOutline(x: Float, y: Float, width: Float, height: Float, radius: Float, lineWidth: Float = 0.5f) {
        shader.bind()

        shaderRadiusUniform.setValue(radius)
        shaderInnerRectUniform.setValue(
            Vector4f(
            x + radius,
            y + radius,
            (x + width) - radius,
            (y + height) - radius)
        )
        shaderOutlineWidth.setValue(lineWidth)

        Renderer.drawRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

        shader.unbind()
    }
}