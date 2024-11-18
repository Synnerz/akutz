package com.github.synnerz.akutz.api.objects.render

import java.awt.Color as AWTColor

class Color @JvmOverloads constructor(
    @JvmField
    val r: Int,
    @JvmField
    val g: Int,
    @JvmField
    val b: Int,
    @JvmField
    val a: Int = 255
) {
    fun getRf(): Double = r / 255.0
    fun getGf(): Double = g / 255.0
    fun getBf(): Double = b / 255.0
    fun getAf(): Double = a / 255.0
    fun asRGB(): Long = ((r shl 16) or (g shl 8) or b).toLong()
    fun asRGBA(): Long = ((r shl 24) or (g shl 16) or (b shl 8) or a).toLong()
    fun asARGB(): Long = ((a shl 24) or (r shl 16) or (g shl 8) or b).toLong()
    fun asRGBf() = doubleArrayOf(getRf(), getGf(), getBf())
    fun asRGBAf() = doubleArrayOf(getRf(), getGf(), getBf(), getAf())
    fun asARGBf() = doubleArrayOf(getAf(), getRf(), getGf(), getBf())
    fun asAWTColor() = AWTColor(r, g, b, a)

    fun asShade(amount: Double): Color {
        val v = amount.coerceIn(0.0, 1.0)
        return Color(
            (r * v).toInt(),
            (g * v).toInt(),
            (b * v).toInt(),
            a
        )
    }

    fun asTint(amount: Double): Color {
        val v = amount.coerceIn(0.0, 1.0)
        return Color(
            ((255 - r) * v).toInt() + 255,
            ((255 - g) * v).toInt() + 255,
            ((255 - b) * v).toInt() + 255,
            a
        )
    }

    fun getShadow(): Color = fromRGBA(((asRGB() shr 2) and 0x3F3F3F) or (a.toLong() shl 24))

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

        @JvmField
        val BLACK = Color(0, 0, 0, 255)

        @JvmField
        val DARK_BLUE = Color(0, 0, 190, 255)

        @JvmField
        val DARK_GREEN = Color(0, 190, 0, 255)

        @JvmField
        val DARK_AQUA = Color(0, 190, 190, 255)

        @JvmField
        val DARK_RED = Color(190, 0, 0, 255)

        @JvmField
        val DARK_PURPLE = Color(190, 0, 190, 255)

        @JvmField
        val GOLD = Color(217, 163, 52, 255)

        @JvmField
        val GRAY = Color(190, 190, 190, 255)

        @JvmField
        val DARK_GRAY = Color(63, 63, 63, 255)

        @JvmField
        val BLUE = Color(63, 63, 254, 255)

        @JvmField
        val GREEN = Color(63, 254, 63, 255)

        @JvmField
        val AQUA = Color(63, 254, 254, 255)

        @JvmField
        val RED = Color(254, 63, 63, 255)

        @JvmField
        val LIGHT_PURPLE = Color(254, 63, 254, 255)

        @JvmField
        val YELLOW = Color(254, 254, 63, 255)

        @JvmField
        val WHITE = Color(255, 255, 255, 255)
    }
}