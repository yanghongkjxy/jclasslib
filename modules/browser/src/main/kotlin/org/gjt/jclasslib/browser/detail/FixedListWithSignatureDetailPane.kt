/*
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2 of the license, or (at your option) any later version.
 */

package org.gjt.jclasslib.browser.detail

import org.gjt.jclasslib.browser.BrowserServices
import org.gjt.jclasslib.structures.ClassMember
import org.gjt.jclasslib.structures.InvalidByteCodeException
import java.awt.Insets
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JButton

abstract class FixedListWithSignatureDetailPane<T : Any>(
        elementClass: Class<T>,
        services: BrowserServices,
        protected val signatureMode: FixedListWithSignatureDetailPane.SignatureMode
) : KeyValueDetailPane<T>(elementClass, services) {

    private val btnCopyToClipboard: JButton = JButton(signatureButtonText).apply {
        addActionListener { copySignatureToClipboard() }
    }

    protected abstract val signatureVerbose: String
    protected abstract val signatureButtonText: String

    override val clipboardText: String?
        get() = signatureVerbose

    override fun addLabels() {
        add(btnCopyToClipboard, gc() {
            insets = Insets(5, 10, 0, 10)
            weightx = 1.0
            gridx = 0
            gridwidth = 3
        })
        nextLine()
    }

    private fun copySignatureToClipboard() {
        val stringSelection = StringSelection(signatureVerbose)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
    }

    protected fun StringBuilder.appendSignature(classMember: ClassMember, signatureMode: SignatureMode) {
        signatureMode.appendSignature(classMember, this)
    }

    enum class SignatureMode {
        FIELD {
            override fun appendSignature(classMember: ClassMember, buffer: StringBuilder) {
                try {
                    buffer.append(classMember.descriptor).append(' ').append(classMember.name)
                } catch (e: InvalidByteCodeException) {
                    e.printStackTrace()
                }
            }
        },
        METHOD {
            override fun appendSignature(classMember: ClassMember, buffer: StringBuilder) {
                try {
                    buffer.append(classMember.name).append(classMember.descriptor)
                } catch (e: InvalidByteCodeException) {
                    e.printStackTrace()
                }
            }
        };

        abstract fun appendSignature(classMember: ClassMember, buffer: StringBuilder)
    }
}
