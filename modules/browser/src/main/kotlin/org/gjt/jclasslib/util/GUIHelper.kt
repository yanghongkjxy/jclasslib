/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package org.gjt.jclasslib.util

import com.install4j.api.Util
import java.awt.Component
import java.awt.Rectangle
import java.awt.Window
import java.net.MalformedURLException
import java.net.URL
import javax.swing.*

object GUIHelper {

    val MESSAGE_TITLE = "jclasslib"
    val YES_NO_OPTIONS = arrayOf("Yes", "No")
    val ICON_EMPTY: Icon = EmptyIcon(16, 16)
    val WINDOW_BORDER = BorderFactory.createEmptyBorder(8, 8, 8, 8)
    val RECT_ORIGIN = Rectangle(0, 0, 0, 0)

    fun showOptionDialog(parent: Component, message: String, options: Array<String>, messageType: Int): Int {
        return JOptionPane.showOptionDialog(parent,
                message,
                MESSAGE_TITLE,
                JOptionPane.YES_NO_OPTION,
                messageType,
                null,
                options,
                options[0])
    }

    fun showMessage(parent: Component?, message: String?, messageType: Int) {
        JOptionPane.showMessageDialog(adjustParent(parent),
                message,
                MESSAGE_TITLE,
                messageType,
                null)
    }

    private fun adjustParent(parent: Component?): Component? =
            if (parent != null && parent !is Window) {
                SwingUtilities.getAncestorOfClass(Window::class.java, parent)
            } else {
                parent
            }

    fun centerOnParentWindow(window: Window, parentWindow: Window) {
        val x = parentWindow.x + (parentWindow.width - window.width) / 2
        val y = parentWindow.y + (parentWindow.height - window.height) / 2
        window.setLocation(x, y)
    }

    fun setDefaultScrollBarUnits(scrollPane: JScrollPane) {
        val unit = JLabel().font.size * 2
        scrollPane.apply {
            horizontalScrollBar.unitIncrement = unit
            verticalScrollBar.unitIncrement = unit
        }
    }

    fun showURL(urlSpec: String) {
        try {
            Util.showUrl(URL(urlSpec))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }
}
