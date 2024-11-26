package com.github.synnerz.akutz.api.objects.render

import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.image.BufferedImage
import java.text.AttributedCharacterIterator
import java.text.AttributedString
import java.util.function.Consumer
import kotlin.math.ceil


class BufferedText @JvmOverloads constructor(
    private var text: String = "",
    private var shadow: Boolean = false,
    private var resolution: Float = 24f,
    private var fontFamily: String = "mojangles"
) : Text {
    private val OBFUSCATED_REG = "[&§]k".toRegex()
    private var dirty = true
    private var font: Font = getFont(fontFamily, resolution)
    private var f1 = getFont("%MONOSPACED%", resolution)
    private var f2 = getFont("%SANS_SERIF%", resolution)
    private var aStr: AttributedString? = null
    private var bStr: AttributedString? = null
    private var bImg: BufferedImage? = null
    private var hasObfs = OBFUSCATED_REG.matches(text)
    private var h: Float = 0f
    private var vh: Float = 0f
    private var w: Float = 0f
    private var vw: Float = 0f
    val COLORS = mutableMapOf(
        '0' to Color.fromRGB(0),
        '1' to Color.fromRGB(170),
        '2' to Color.fromRGB(43520),
        '3' to Color.fromRGB(43690),
        '4' to Color.fromRGB(11141120),
        '5' to Color.fromRGB(11141290),
        '6' to Color.fromRGB(16755200),
        '7' to Color.fromRGB(11184810),
        '8' to Color.fromRGB(5592405),
        '9' to Color.fromRGB(5592575),
        'a' to Color.fromRGB(5635925),
        'b' to Color.fromRGB(5636095),
        'c' to Color.fromRGB(16733525),
        'd' to Color.fromRGB(16733695),
        'e' to Color.fromRGB(16777045),
        'f' to Color.fromRGB(16777215)
    )
    val COLORS_SHADOW = mutableMapOf(
        '0' to Color.fromRGB(0),
        '1' to Color.fromRGB(42),
        '2' to Color.fromRGB(10752),
        '3' to Color.fromRGB(10794),
        '4' to Color.fromRGB(2752512),
        '5' to Color.fromRGB(2752554),
        '6' to Color.fromRGB(4139520),
        '7' to Color.fromRGB(2763306),
        '8' to Color.fromRGB(1381653),
        '9' to Color.fromRGB(1381695),
        'a' to Color.fromRGB(1392405),
        'b' to Color.fromRGB(1392447),
        'c' to Color.fromRGB(4134165),
        'd' to Color.fromRGB(4134207),
        'e' to Color.fromRGB(4144917),
        'f' to Color.fromRGB(4144959)
    )

    private fun mark() = apply { dirty = true }
    override fun getText() = text
    override fun setText(v: String) = mark().also { text = v; hasObfs = OBFUSCATED_REG.matches(text) }
    override fun getWidth() = w
    override fun getHeight() = h
    override fun getVisibleWidth() = vw
    override fun getVisibleHeight() = vh
    fun getShadow() = shadow
    fun setShadow(v: Boolean) = mark().also { shadow = v }
    fun getResolution() = resolution
    fun setResolution(v: Float) = mark().also {
        resolution = v
        font = getFont(fontFamily, resolution)
        f1 = getFont("%MONOSPACED%", resolution)
        f2 = getFont("%SANS_SERIF%", resolution)
    }

    fun getFontFamily() = fontFamily
    fun setFontFamily(v: String) = mark().also { fontFamily = normalizeFont(v); font = getFont(fontFamily, resolution) }
    fun getFont() = font

    fun render(graphics: Graphics2D, x: Int, y: Int) {
        graphics.drawImage(bImg!!, x, y, null)
    }

    fun update() {
        if (!dirty && !hasObfs) return
        dirty = false

        var g = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        g.font = font

        val str = "$text&r"
        val sb = StringBuilder()
        val o = mutableListOf<obfObj>()
        val atts = mutableListOf<attsObj>()
        val cAtts = mutableListOf<attsObj>()
        var obfS = -1

        var i = 0
        while (i < str.length) {
            val c = str[i]
            if ((c == '&' || c == '§') && i < str.length - 1) {
                val k = str[i + 1]
                if (isColorCode(k)) {
                    cAtts.removeAll {
                        if (!isColorCode(it.t)) return@removeAll true
                        atts.add(attsObj(it.t, it.s, sb.length))
                        return@removeAll false
                    }
                    cAtts.add(attsObj(k, sb.length, 0))
                    i += 2
                    continue
                }
                if (k == 'k') {
                    obfS = sb.length
                    i += 2
                    continue
                }
                if (k == 'l' || k == 'o' || k == 'm' || k == 'n') {
                    cAtts.add(attsObj(k, sb.length, 0))
                    i += 2
                    continue
                }
                if (k == 'r') {
                    cAtts.forEach { atts.add(attsObj(it.t, it.s, sb.length)) }
                    cAtts.clear()
                    if (obfS >= 0) o.add(obfObj(obfS, sb.length))
                    obfS = -1
                    i += 2
                    continue
                }
            }
            sb.append(if (obfS >= 0) getObfChar() else c)
            i++
        }

        val s = sb.toString()
        val ca = s.toCharArray()
        val a = AttributedString(s)
        val b = if (shadow) AttributedString(s) else null

        addAttribute(a, TextAttribute.SIZE, resolution, 0, s.length)
        addAttribute(b, TextAttribute.SIZE, resolution, 0, s.length)
        var end = 0
        for (v in o) {
            setFontAttr(a, ca, font, f2, end, v.s)
            setFontAttr(b, ca, font, f2, end, v.s)
            addAttribute(a, TextAttribute.FONT, f1, v.s, v.e)
            addAttribute(b, TextAttribute.FONT, f1, v.s, v.e)
            end = v.e
        }
        setFontAttr(a, ca, font, f2, end, s.length)
        setFontAttr(b, ca, font, f2, end, s.length)

        atts.forEach(Consumer { v: attsObj ->
            if (isColorCode(v.t)) {
                addAttribute(a, TextAttribute.FOREGROUND, COLORS[v.t]!!.asAWTColor(), v.s, v.e)
                addAttribute(b, TextAttribute.FOREGROUND, COLORS_SHADOW[v.t]!!.asAWTColor(), v.s, v.e)
            } else if (v.t == 'l') {
                addAttribute(b, TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, v.s, v.e)
                addAttribute(a, TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, v.s, v.e)
            } else if (v.t == 'o') {
                addAttribute(b, TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, v.s, v.e)
                addAttribute(a, TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, v.s, v.e)
            } else if (v.t == 'm') {
                addAttribute(b, TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, v.s, v.e)
                addAttribute(a, TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, v.s, v.e)
            } else if (v.t == 'n') {
                addAttribute(b, TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, v.s, v.e)
                addAttribute(a, TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, v.s, v.e)
            } else throw RuntimeException("unknown attribute: " + v.t)
        })

        val tyl = TextLayout(a.iterator, g.fontRenderContext)

        aStr = a
        bStr = b
        w = tyl.advance
        vw = tyl.visibleAdvance
        h = tyl.ascent
        vh = h + tyl.descent
        g.dispose()

        bImg = BufferedImage(
            ceil(vw + if (shadow) resolution / 10 else 0f).toInt(),
            ceil(vh + if (shadow) resolution / 10 else 0f).toInt(),
            BufferedImage.TYPE_INT_ARGB
        )
        g = bImg!!.createGraphics()
        g.font = font
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (shadow) {
            g.color = COLORS_SHADOW['f']!!.asAWTColor()
            g.drawString(bStr!!.iterator, (resolution / 10).toInt(), (h + resolution / 10).toInt())
        }
        g.color = COLORS['f']!!.asAWTColor()
        g.drawString(aStr!!.iterator, 0, h.toInt())
        g.dispose()
    }

    data class attsObj internal constructor(val t: Char, val s: Int, val e: Int)
    data class obfObj internal constructor(val s: Int, val e: Int)

    private fun getObfChar(): Char =
        "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".random()

    private fun isColorCode(c: Char) = c in '0'..'9' || c in 'a'..'f'
    private fun addAttribute(
        str: AttributedString?,
        a: AttributedCharacterIterator.Attribute,
        v: Any?,
        s: Int,
        e: Int
    ) {
        if (str == null) return
        if (s >= e) return
        str.addAttribute(a, v, s, e)
    }

    private fun setFontAttr(att: AttributedString?, str: CharArray, f1: Font, f2: Font, s: Int, e: Int) {
        var s = s
        if (att == null) return
        if (s >= e) return
        if (s >= str.size) return
        if (e > str.size) return

        var i = f1.canDisplayUpTo(str, s, e)
        var maxIters = 10
        while (s < str.size && i >= 0) {
            if (--maxIters == 0) return
            addAttribute(att, TextAttribute.FONT, f1, s, i)
            s = i
            val b = s
            while (s < e && f1.canDisplayUpTo(str, s, s + 1) != -1) s++
            addAttribute(att, TextAttribute.FONT, f2, b, s)
            i = f1.canDisplayUpTo(str, s, e)
        }
        addAttribute(att, TextAttribute.FONT, f1, s, e)
    }

    companion object {
        @JvmStatic
        private val FONT_MAP =
            GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.associateBy { normalizeFont(it) }

        @JvmStatic
        val FONTS = FONT_MAP.keys

        @JvmStatic
        fun normalizeFont(family: String) = family.replace("\\W".toRegex(), "").lowercase()

        @JvmStatic
        private val CUSTOM_FONTS = mutableMapOf<String, Font>()

        @JvmStatic
        fun registerFont(name: String, font: Font): Boolean {
            val n = normalizeFont(name)
            if (FONTS.contains(n)) return false
            CUSTOM_FONTS[name] = font
            return true
        }

        init {
            registerFont(
                "mojangles",
                Font.createFont(Font.TRUETYPE_FONT, BufferedText::class.java.getResourceAsStream("/Mojangles.ttf"))
            )
        }

        @JvmStatic
        fun getFont(family: String, resolution: Float): Font {
            if (family == "%MONOSPACED%") return Font(Font.MONOSPACED, Font.PLAIN, resolution.toInt())
            if (family == "%SANS_SERIF%") return Font(Font.SANS_SERIF, Font.PLAIN, resolution.toInt())

            if (CUSTOM_FONTS.containsKey(family)) return CUSTOM_FONTS[family]!!.deriveFont(Font.PLAIN, resolution)
            if (!FONTS.contains(family)) throw IllegalArgumentException("Unknown font: $family")
            return Font(FONT_MAP[family], Font.PLAIN, resolution.toInt())
        }
    }
}