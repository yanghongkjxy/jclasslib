/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package org.gjt.jclasslib.browser

import browser.BrowserBundle.getString
import com.install4j.api.launcher.Variables
import net.miginfocom.swing.MigLayout
import org.gjt.jclasslib.util.DefaultAction
import org.gjt.jclasslib.util.GUIHelper
import java.awt.Font
import java.io.IOException
import java.util.*
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel

class BrowserAboutDialog(parent: JFrame) : JDialog(parent) {

    private val okAction = DefaultAction(getString("action.ok")) {
        isVisible = false
        dispose()
    }

    init {
        setupComponent()
    }

    private fun setupComponent() {
        (contentPane as JComponent).apply {
            layout = MigLayout("wrap", "50[align center]50")

            add(JLabel(getString("about.jclasslib.name")).apply {
                font = font.deriveFont(Font.BOLD)
            })
            add(JLabel(getString("about.version", getVersion())))
            add(JLabel(getString("about.copyright", "ej-technologies GmbH", "2001-" + Calendar.getInstance().get(Calendar.YEAR))))
            add(JLabel(getString("about.license")))
            add(JLabel(getString("about.icons", "iconexperience.com")))

            contentPane.add(okAction.createTextButton().apply {
                this@BrowserAboutDialog.getRootPane().defaultButton = this
            }, "newline para")
        }

        pack()
        isModal = true
        isResizable = false
        title = getString("about.title")
        GUIHelper.centerOnParentWindow(this, owner)
        defaultCloseOperation = DISPOSE_ON_CLOSE
    }

    private fun getVersion(): String {
        return try {
            Variables.getCompilerVariable("sys.version")
        } catch (e: IOException) {
            "<unknown>"
        }
    }
}
