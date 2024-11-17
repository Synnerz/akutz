package com.github.synnerz.akutz.api.objects

import java.awt.Color as AWTColor

class Color(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int = 255
) {
    fun getR() = r
    fun getG() = g
    fun getB() = b
    fun getA() = a
    fun getRF(): Double = r / 255.0
    fun getGF(): Double = g / 255.0
    fun getBF(): Double = b / 255.0
    fun getAF(): Double = a / 255.0
    fun asRGB(): Long = ((r shl 16) or (g shl 8) or b).toLong()
    fun asRGBA(): Long = ((r shl 24) or (g shl 16) or (b shl 8) or a).toLong()
    fun asARGB(): Long = ((a shl 24) or (r shl 16) or (g shl 8) or b).toLong()
    fun asRGBF() = doubleArrayOf(getRF(), getGF(), getBF())
    fun asRGBAF() = doubleArrayOf(getRF(), getGF(), getBF(), getAF())
    fun asARGBF() = doubleArrayOf(getAF(), getRF(), getGF(), getBF())
    fun asAWTColor() = AWTColor(r, g, b, a)

    companion object {
        @JvmStatic
        fun fromRGB(color: Long) = Color(
            (color shr 16).toInt() and 0xFF,
            (color shr 8).toInt() and 0xFF,
            (color shr 0).toInt() and 0xFF,
            255
        )

        @JvmStatic
        fun fromRGBA(color: Long) = Color(
            (color shr 24).toInt() and 0xFF,
            (color shr 16).toInt() and 0xFF,
            (color shr 8).toInt() and 0xFF,
            color.toInt() and 0xFF
        )

        @JvmStatic
        fun fromARGB(color: Long) = Color(
            (color shr 16).toInt() and 0xFF,
            (color shr 8).toInt() and 0xFF,
            color.toInt() and 0xFF,
            (color shr 24).toInt() and 0xFF
        )

        @JvmOverloads
        @JvmStatic
        fun fromRGBA(r: Double, g: Double, b: Double, a: Double = 1.0) = Color(
            (r * 255).toInt().coerceIn(0, 255),
            (g * 255).toInt().coerceIn(0, 255),
            (b * 255).toInt().coerceIn(0, 255),
            (a * 255).toInt().coerceIn(0, 255)
        )
    }
}