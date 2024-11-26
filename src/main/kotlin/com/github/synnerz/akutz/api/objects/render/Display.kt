package com.github.synnerz.akutz.api.objects.render

import com.github.synnerz.akutz.api.events.EventType
import com.github.synnerz.akutz.api.events.NormalEvent
import com.github.synnerz.akutz.api.libs.render.Renderer
import com.github.synnerz.akutz.listeners.MouseListener
import java.awt.Font
import java.awt.image.BufferedImage

class Display @JvmOverloads constructor(
    private val isBuffered: Boolean = false
) {
    private val listeners = object {
        var onClick: NormalEvent? = null
        var onScroll: NormalEvent? = null
        var onDragged: NormalEvent? = null
        var onCreateLine: NormalEvent? = null
    }
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
    private var font: String? = null
    private var dirty = false
    private var cw: Float? = null
    private var cvw: Float? = null
    private var cvh: Float? = null
    private var bw: Float? = null
    private var bh: Float? = null
    private var img: Image? = null

    init {
        MouseListener.registerClickListener { x, y, button, pressed ->
            if (!isInBounds(x, y)) return@registerClickListener
            val lineUnder = getLineUnder(x, y)
            if (lineUnder != null) lineUnder.onClick?.trigger(arrayOf(x, y, button, pressed))
            listeners.onClick?.trigger(arrayOf(x, y, button, pressed, lineUnder))
        }

        MouseListener.registerScrollListener { x, y, delta ->
            if (!isInBounds(x, y)) return@registerScrollListener
            val lineUnder = getLineUnder(x, y)
            if (lineUnder != null) lineUnder.onScroll?.trigger(arrayOf(x, y, delta))
            listeners.onScroll?.trigger(arrayOf(x, y, delta, lineUnder))
        }

        MouseListener.registerDraggedListener { deltaX, deltaY, x, y, button ->
            if (!isInBounds(x, y)) return@registerDraggedListener
            val lineUnder = getLineUnder(x, y)
            if (lineUnder != null) lineUnder.onDragged?.trigger(arrayOf(x, y, deltaX, deltaY, button))
            listeners.onDragged?.trigger(arrayOf(x, y, deltaX, deltaY, button, lineUnder))
        }
    }

    fun onClick(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onClick = NormalEvent(method, EventType.Other)
        listeners.onClick
    }

    fun onScroll(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onScroll = NormalEvent(method, EventType.Other)
        listeners.onScroll
    }

    fun onDragged(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onDragged = NormalEvent(method, EventType.Other)
        listeners.onDragged
    }

    fun onCreateLine(method: (args: Array<out Any?>) -> Unit) = run {
        listeners.onCreateLine = NormalEvent(method, EventType.Other)
        listeners.onCreateLine
    }

    fun onLineCreate(method: (args: Array<out Any?>) -> Unit) = onCreateLine(method)

    fun mark() = apply {
        dirty = true
        cw = null
        cvw = null
        cvh = null
    }

    private fun createLine() = run {
        val line = DisplayLine(isBuffered).setScale(scale).setShadow(shadow).setResolution(resolution)
        if (font != null) (line.getText() as BufferedText).setFontFamily(font!!)
        listeners.onCreateLine?.trigger(arrayOf(line))
        line
    }

    fun getLines() = lines
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
        lines.forEach { it.setShadow(s) }
        dirty = true
    }

    fun getResolution() = resolution
    fun setResolution(r: Float) = apply {
        if (!isBuffered) throw UnsupportedOperationException("only to be used when `isBuffered` is set to true")
        resolution = r
        lines.forEach { it.setResolution(r) }
        dirty = true
    }

    fun getFont() = font
    fun setFont(f: Font): Display = setFont(f.family)
    fun setFont(f: String) = apply {
        if (!isBuffered) throw UnsupportedOperationException("only to be used when `isBuffered` is set to true")
        val family = BufferedText.normalizeFont(f)
        if (!BufferedText.FONTS.contains(family)) throw IllegalArgumentException("Unknown font: $f. If you are trying to use a custom installed font on windows, make sure it is installed for all users otherwise the JVM will not recognize it.")
        font = family
        lines.forEach { (it.getText() as BufferedText).setFontFamily(family) }
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

    fun getWidth() = cw ?: if (lines.size == 0) 0f else lines.maxOf { it.getWidth() }.also { cw = it }
    fun getVisibleWidth() = cvw ?: if (lines.size == 0) 0f else lines.maxOf { it.getVisibleWidth() }.also { cvw = it }
    fun getLineHeight() = 10 * scale + gap
    fun getHeight() = if (lines.size == 0) 0f else getLineHeight() * lines.size - gap
    fun getVisibleHeight(): Float = cvh ?: run {
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
        if (isBuffered) {
            if (dirty) forceRenderBuffered()
            img!!.draw(
                getTopLeftX(),
                getTopLeftY(),
                (getWidth() * roundPow2(bw!!) / bw!!).toDouble(),
                (getHeight() * roundPow2(bh!!) / bh!!).toDouble()
            )
        } else {
            val tx = getTopLeftX()
            var y = getTopLeftY()
            Renderer.beginDraw(backgroundColor, false)
            if (background == Background.FULL) Renderer.drawRectangle(
                tx,
                y,
                getWidth().toDouble(),
                getHeight().toDouble()
            )
            for (l in lines) {
                val x = when (horzAlign) {
                    HorzAlign.START -> 0f
                    HorzAlign.CENTER -> (getWidth() - l.getVisibleWidth()) / 2
                    HorzAlign.END -> (getWidth() - l.getVisibleWidth())
                }
                if (background == Background.LINE) {
                    Renderer.color(backgroundColor)
                    Renderer.drawRectangle(
                        tx + x, y, l.getVisibleWidth().toDouble(),
                        l.getVisibleHeight().toDouble()
                    )
                }
                l.render((tx + x).toFloat(), y.toFloat())
                y += getLineHeight()
            }
            Renderer.finishDraw()
        }
    }

    // round sizes up to the nearest // 128 so DynamicTexture doesn't have to be recreated as often
    private fun roundPow2(f: Float) = (f.toInt() + 127) and (127).inv()
    private fun forceRenderBuffered() {
        dirty = false
        cw = null
        cvw = null
        cvh = null
        lines.forEach { it.update() }
        bw = lines.maxOf { it.getText().getWidth() } + if (shadow) resolution / 10 else 0f
        bh = lines.size * resolution + (lines.size - 1) * (gap * resolution / 10) + if (shadow) resolution / 10 else 0f
        val bImg = BufferedImage(
            roundPow2(bw!!),
            roundPow2(bh!!),
            BufferedImage.TYPE_INT_ARGB
        )
        val g = bImg.createGraphics()
        if (background == Background.FULL) {
            g.color = backgroundColor.asAWTColor()
            g.drawRect(0, 0, bImg.width, bImg.height)
        }
        var y = 0f
        for (l in lines) {
            val x = when (horzAlign) {
                HorzAlign.START -> 0f
                HorzAlign.CENTER -> (getWidth() - l.getVisibleWidth()) / 2
                HorzAlign.END -> (getWidth() - l.getVisibleWidth())
            }
            if (background == Background.LINE) {
                g.color = backgroundColor.asAWTColor()
                g.drawRect(x.toInt(), y.toInt(), l.getVisibleWidth().toInt(), l.getVisibleHeight().toInt())
            }
            l.render(x.toInt(), y.toInt(), g)
            y += resolution + gap * resolution / 10
        }
        if (img == null) img = Image(bImg)
        else img!!.update(bImg)
    }

    fun getLineUnder(x: Double, y: Double): DisplayLine? {
        val dx = x - getTopLeftX()
        val dy = y - getTopLeftY()
        if (dx < 0 || dy < 0) return null
        if (dx > getWidth()) return null
        val i = (dy / getLineHeight()).toInt()
        if (i >= lines.size) return null
        // mouse is in gap
        if (dy % getLineHeight() > 10 * scale) return null
        return lines[i]
    }

    fun isInBounds(x: Double, y: Double): Boolean {
        val tx = getTopLeftX()
        val ty = getTopLeftY()
        return x >= tx && x <= tx + getWidth() && y >= ty && y <= ty + getHeight()
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