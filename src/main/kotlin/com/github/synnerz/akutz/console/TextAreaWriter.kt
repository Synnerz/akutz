package com.github.synnerz.akutz.console

import java.awt.Color
import java.io.PrintWriter
import java.io.Writer
import javax.swing.JTextPane
import javax.swing.text.AttributeSet
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/utils/console/TextAreaWriter.kt)
 */
class TextAreaWriter(val textArea: JTextPane) : Writer() {
    val printWriter = PrintWriter(this)
    var currentLogType: LogType = LogType.INFO

    override fun close() {}
    override fun flush() {}

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        val s = String(cbuf, off, len)
        val sc: StyleContext = StyleContext.getDefaultStyleContext()

        val color = when (currentLogType) {
            LogType.INFO -> Color(208, 208, 208)
            LogType.WARN -> Color(248, 191, 84)
            LogType.ERROR -> Color(225, 65, 73)
        }

        val attributes: AttributeSet = sc.addAttribute(
            SimpleAttributeSet.EMPTY,
            StyleConstants.Foreground,
            color
        )

        textArea.document.insertString(textArea.document.length, s, attributes)
        textArea.caretPosition = textArea.document.length
        currentLogType = LogType.INFO
    }

    @JvmOverloads
    fun println(s: Any, logType: LogType = LogType.INFO, end: String = "\n") {
        currentLogType = logType
        printWriter.print(s)
        printWriter.print(end)
    }

    fun clear() {
        textArea.text = ""
    }
}