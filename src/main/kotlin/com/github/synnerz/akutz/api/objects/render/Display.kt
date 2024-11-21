package com.github.synnerz.akutz.api.objects.render

import java.awt.image.BufferedImage

class Display(private val isBuffered: Boolean) {
    private val lines = mutableListOf<DisplayLine>()
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var scale: Float = 1f
    private var gap: Float = 0f
    private var shadow: Boolean = false
    private var resolution: Float = 24f
    private var horzAlign: HorzAlign = HorzAlign.START
    private var vertAlign: VertAlign = VertAlign.TOP
    private var background: Background = Background.NONE
    private var backgroundColor: Color = Color.EMPTY
    private var dirty = false
    private var cw: Float? = null
    private var cvw: Float? = null
    private var cvh: Float? = null
    private var img: Image? = null

    fun mark() = apply {
        dirty = true
        cw = null
        cvw = null
        cvh = null
    }

    private fun createLine() = DisplayLine(isBuffered).setScale(scale).setShadow(shadow).setResolution(resolution)

    fun getX() = x
    fun getY() = y
    fun setX(x: Double) = apply { this.x = x }
    fun setY(y: Double) = apply { this.y = y }
    fun getTopLeftX(): Double = when (horzAlign) {
        HorzAlign.START -> x
        HorzAlign.CENTER -> x - getWidth() / 2
        HorzAlign.END -> x - getWidth()
    }

    fun getTopLeftY(): Double = when (vertAlign) {
        VertAlign.TOP -> y
        VertAlign.CENTER -> y - getHeight() / 2
        VertAlign.BOTTOM -> y - getHeight()
    }

    fun getScale() = scale
    fun setScale(s: Float) = apply {
        scale = s
        cw = null
        cvw = null
        cvh = null
        lines.forEach { it.setScale(s) }
    }

    fun getGap() = gap
    fun setGap(g: Float) = apply {
        mark()
        gap = g
    }

    fun getShadow() = shadow
    fun setShadow(s: Boolean) = apply {
        shadow = s
        lines.forEach{ it.setShadow(s) }
        dirty = true
    }

    fun getResolution() = resolution
    fun setResolution(r: Float) = apply {
        if (!isBuffered) throw UnsupportedOperationException("only to be used when `isBuffered` is set to true")
        resolution = r
        lines.forEach{ it.setResolution(r) }
        dirty = true
    }

    fun setLine(s: String) = apply {
        if (lines.size != 1 || lines.getOrNull(0)?.getString() != s) {
            clearLines()
            addLine(s)
        }
    }

    fun setLines(s: List<String>) = apply {
        if (s.isEmpty()) return clearLines()
        if (s.size == 1) return setLine(s[0])
        if (s.size < lines.size) {
            lines.dropLast(lines.size - s.size)
            mark()
        }
        s.forEachIndexed { i, v ->
            if (i < lines.size && v == lines[i].getString()) return@forEachIndexed
            mark()
            lines[i] = createLine().setString(v)
        }
    }

    fun addLine(s: String) = apply {
        mark()
        lines.add(createLine().setString(s))
    }

    fun addLines(s: List<String>) = apply { s.forEach(::addLine) }
    fun clearLines() = apply {
        mark()
        lines.clear()
    }

    fun getWidth() = cw ?: lines.maxOf { it.getWidth() }.also { cw = it }
    fun getVisibleWidth() = cvw ?: lines.maxOf { it.getVisibleWidth() }.also { cvw = it }
    fun getLineHeight() = 10 * scale + gap
    fun getHeight() = getLineHeight() * lines.size
    fun getVisibleHeight() = cvh ?: {
        val s = lines.indexOfFirst { it.getVisibleWidth() > 0f }
        val e = lines.indexOfLast { it.getVisibleWidth() > 0f }
        if (s == -1) 0f else (e - s + 1) * getLineHeight()
    }

    fun getHorzAlign() = horzAlign
    fun setHorzAlign(s: String) = setHorzAlign(HorzAlign.getParameterByName(s))
    fun setHorzAlign(a: HorzAlign) = apply { horzAlign = a }
    fun getVertAlign() = vertAlign
    fun setVertAlign(s: String) = setVertAlign(VertAlign.getParameterByName(s))
    fun setVertAlign(o: VertAlign) = apply { vertAlign = o }
    fun getBackground() = background
    fun setBackground(s: String) = setBackground(Background.getParameterByName(s))
    fun setBackground(b: Background) = apply { background = b }
    fun getBackgroundColor() = backgroundColor
    fun setBackgroundColor(c: Color) = apply { backgroundColor = c }

    fun render() {
        if (lines.size == 0) return
        // hi if you're trying to refactor this, do not call .getTopLeft[X/Y]() here and pass into the img.draw() later on, because the `BufferedText` have not been necessarily updated yet by `forceRenderBuffered()`, viz. `lines.forEach { it.update() }`
        if (dirty) forceRenderBuffered()
        if (isBuffered) img!!.draw(getTopLeftX(), getTopLeftY())
        else {
            val tx = getTopLeftX()
            val ty = getTopLeftY()
            var y = ty
            for (l in lines) {
                val x = when (horzAlign) {
                    HorzAlign.START -> 0f
                    HorzAlign.CENTER -> (getWidth() - l.getVisibleWidth()) / 2
                    HorzAlign.END -> (getWidth() - l.getVisibleWidth())
                }
                l.render((tx + x).toInt(), y.toInt(), x, (y - ty).toFloat())
                y += getLineHeight()
            }
        }
    }

    // round sizes up to the nearest // 128 so DynamicTexture doesn't have to be recreated as often
    private fun roundPow2(f: Float) = (f.toInt() + 127) and (127).inv()
    private fun forceRenderBuffered() {
        dirty = false
        lines.forEach { it.update() }
        val w = getWidth()
        val h = getHeight()
        val bImg = BufferedImage(
            roundPow2(w + if (shadow) resolution / 10 else 0f),
            roundPow2(h + if (shadow) resolution / 10 else 0f),
            BufferedImage.TYPE_INT_ARGB
        )
        val g = bImg.createGraphics()
        var y = 0f
        for (l in lines) {
            val x = when (horzAlign) {
                HorzAlign.START -> 0f
                HorzAlign.CENTER -> (getWidth() - l.getVisibleWidth()) / 2
                HorzAlign.END -> (getWidth() - l.getVisibleWidth())
            }
            l.render(x.toInt(), y.toInt(), g)
            y += resolution + gap * resolution / 10
        }
        if (img == null) img = Image(bImg)
        else img!!.update(bImg)
    }

    enum class HorzAlign {
        START,
        CENTER,
        END;

        companion object {
            fun getParameterByName(name: String) =
                HorzAlign.entries.find { it.name == name.uppercase() }
                    ?: throw IllegalArgumentException("Unknown parameter: $name")
        }
    }

    enum class VertAlign {
        TOP,
        CENTER,
        BOTTOM;

        companion object {
            fun getParameterByName(name: String) =
                VertAlign.entries.find { it.name == name.uppercase() }
                    ?: throw IllegalArgumentException("Unknown parameter: $name")
        }
    }

    enum class Background {
        NONE,
        FULL,
        LINE;

        companion object {
            fun getParameterByName(name: String) =
                Background.entries.find { it.name == name.uppercase() }
                    ?: throw IllegalArgumentException("Unknown parameter: $name")
        }
    }
}