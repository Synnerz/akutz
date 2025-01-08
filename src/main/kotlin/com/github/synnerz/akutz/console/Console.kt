package com.github.synnerz.akutz.console

import java.awt.Color
import java.awt.Font
import java.awt.Insets
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.DefaultCaret

/**
 * Taken from ChatTriggers under MIT License
 * [Link](https://github.com/ChatTriggers/ChatTriggers/blob/master/src/main/kotlin/com/chattriggers/ctjs/utils/console/Console.kt)
 */
object Console {
    // TODO: make a better console whenever not lazy
    val frame = JFrame("Akutz Console")
    val textArea = JTextPane()
    val writer = TextAreaWriter(textArea)

    init {
        // Setup textAreaWriter
        textArea.isEditable = false
        textArea.margin = Insets(5, 5, 5, 5)
        textArea.autoscrolls = true
        val caret = textArea.caret as DefaultCaret
        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE

        // Setup frame
        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        frame.add(JScrollPane(textArea))
        frame.pack()
        frame.isVisible = false
        frame.setSize(800, 600)
    }

    fun clearConsole() {
        SwingUtilities.invokeLater {
            writer.clear()
        }
    }

    @JvmOverloads
    fun println(obj: Any, logType: LogType = LogType.INFO, end: String = "\n") {
        SwingUtilities.invokeLater {
            try {
                writer.println(obj.toString(), logType, end)
            } catch (exception: Exception) {
                println(obj.toString(), logType, end)
            }
        }
    }

    fun printError(error: String) {
        writer.println(error, LogType.ERROR)
    }

    fun Exception.printError() {
        val stringWriter = StringWriter()
        this.printStackTrace(PrintWriter(stringWriter))
        writer.println(stringWriter.toString(), LogType.ERROR)
    }

    fun showConsole() {
        frame.isVisible = true
        textArea.background = Color(25, 25, 25)
        textArea.foreground = Color(199, 204, 209)
        textArea.font = Font(
            "DejaVu Sans Mono",
            Font.PLAIN,
            15
        ).deriveFont(16f)
        frame.toFront()
        frame.repaint()
    }
}