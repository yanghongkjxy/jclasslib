/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package org.gjt.jclasslib.browser.detail

import net.miginfocom.swing.MigLayout
import org.gjt.jclasslib.browser.*
import org.gjt.jclasslib.structures.Constant
import org.gjt.jclasslib.structures.attributes.BootstrapMethodsAttribute
import org.gjt.jclasslib.util.*
import org.jetbrains.annotations.Nls
import java.awt.Point
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.event.HyperlinkEvent
import javax.swing.tree.TreePath

abstract class KeyValueDetailPane<T : Any>(elementClass: Class<T>, services: BrowserServices) : DetailPane<T>(elementClass, services) {

    private val scrollPane = JScrollPane(this).apply {
        GUIHelper.setDefaultScrollBarUnits(this)
        border = null
    }

    public override val wrapper: JComponent
        get() = scrollPane

    private fun addKeyValue(keyValue: KeyValue<T, *>) {
        add(keyValue.keyLabel as JComponent)
        val valueLabel = keyValue.valueLabel
        val hyperlinkButton = keyValue.hyperlinkButton
        if (hyperlinkButton != null) {
            add(hyperlinkButton, if (valueLabel.isEnabled) "" else "spanx")
        }
        if (valueLabel.isEnabled) {
            add(valueLabel, "spanx")
            if (hyperlinkButton != null && valueLabel is MultiLineLabel) {
                valueLabel.autoTooltip = true
            }
        }
    }

    override fun setupComponent() {
        layout = MigLayout("wrap" + if (hasInsets()) "" else ", insets 0", "[][][grow]")
        addLabels()
    }

    protected open fun hasInsets() = false

    override fun show(treePath: TreePath) {
        scrollPane.viewport.viewPosition = Point(0, 0)
        runShowHandlers(treePath)
    }

    private fun runShowHandlers(treePath: TreePath) {
        element = getElement(treePath)
        element?.let { element -> showHandlers.forEach { it.invoke(element) } }
    }

    override fun updateFilter(tree: JTree, treeNode: BrowserTreeNode, expand: Boolean) {
        super.updateFilter(tree, treeNode, expand)
        runShowHandlers(TreePath(treeNode.path))
    }

    protected abstract fun addLabels()

    protected val showHandlers = ArrayList<(element: T) -> Unit>()
    protected var element: T? = null

    protected fun addConstantPoolLink(@Nls key: String, indexResolver: (element: T) -> Int): HyperlinkKeyValue<T> {
        val keyValue = HyperlinkKeyValue<T>(key, multiLineLabel(), HyperlinkButton())
        addKeyValue(keyValue)
        showHandlers.add { element ->
            val constantPoolIndex = indexResolver(element)
            keyValue.hyperlinkButton?.action = object : AbstractAction() {
                init {
                    putValue(NAME, CPINFO_LINK_TEXT + constantPoolIndex)
                }

                override fun actionPerformed(e: ActionEvent?) {
                    constantPoolLink(services, constantPoolIndex)
                }
            }
            keyValue.valueLabel.applyComment(constantPoolIndex)
        }
        return keyValue
    }

    protected fun addAttributeLink(@Nls key: String, attributeClass: Class<BootstrapMethodsAttribute>, prefix: String, indexResolver: (element: T) -> Int): HyperlinkKeyValue<T> {
        val keyValue = HyperlinkKeyValue<T>(key, multiLineLabel().apply { isEnabled = false }, HyperlinkButton())
        addKeyValue(keyValue)
        showHandlers.add { element ->
            val index = indexResolver(element)
            keyValue.hyperlinkButton?.action = object : AbstractAction() {
                init {
                    putValue(NAME, prefix + index)
                }

                override fun actionPerformed(e: ActionEvent?) {
                    classAttributeLink(services, index, attributeClass)
                }
            }
        }
        return keyValue
    }

    protected fun addDetail(@Nls key: String, textResolver: (element: T) -> String): MultiLineKeyValue<T> {
        val keyValue = MultiLineKeyValue<T>(key, multiLineLabel())
        addKeyValue(keyValue)
        showHandlers.add { element ->
            keyValue.valueLabel.text = textResolver(element)
            keyValue.show(element)
        }
        return keyValue
    }

    protected fun addMultiLineHtmlDetail(@Nls key: String, textResolver: (element: T) -> String): HtmlKeyValue<T> {
        val keyValue = HtmlKeyValue<T>(key, highlightTextArea())
        addKeyValue(keyValue)
        showHandlers.add { element ->
            keyValue.valueLabel.text = textResolver(element)
            keyValue.show(element)
        }
        return keyValue
    }

    protected fun addClassElementOpener(constantResolver: (element: T) -> Constant) {
        if (services.canOpenClassFiles()) {
            val classElementOpener = ClassElementOpener(this)
            add(classElementOpener, "newline unrel, spanx")
            showHandlers.add { element ->
                classElementOpener.setConstant(constantResolver(element))
            }
        }
    }

    private fun MultiLineLabel.applyComment(constantPoolIndex: Int) {
        toolTipText = text
        text = "<" + getConstantPoolEntryName(constantPoolIndex) + ">"
    }

    abstract class KeyValue<T : Any, out L>(@Nls key: String, val valueLabel: L, val hyperlinkButton: HyperlinkButton? = null) where L : JComponent, L : TextDisplay {

        val keyLabel = ExtendedJLabel(key)
        private var visibilityPredicate: ((T) -> Boolean)? = null

        fun visibilityPredicate(visibilityPredicate: (T) -> Boolean) {
            this.visibilityPredicate = visibilityPredicate
        }

        fun show(element: T) {
            visibilityPredicate?.let {
                val show = it(element)
                keyLabel.isVisible = show
                valueLabel.isVisible = show
                hyperlinkButton?.isVisible = show
            }
        }
    }

    class HyperlinkKeyValue<T : Any>(@Nls key: String, valueLabel: MultiLineLabel, hyperlinkButton: HyperlinkButton) : KeyValue<T, MultiLineLabel>(key, valueLabel, hyperlinkButton)

    class MultiLineKeyValue<T : Any>(@Nls key: String, multiLineLabel: MultiLineLabel) : KeyValue<T, MultiLineLabel>(key, multiLineLabel, null)

    class HtmlKeyValue<T : Any>(@Nls key: String, valueLabel: HtmlDisplayTextArea) : KeyValue<T, HtmlDisplayTextArea>(key, valueLabel, null) {
        fun linkHandler(handler: (String) -> Unit) {
            valueLabel.addHyperlinkListener { e ->
                if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                    val description = e.description
                    handler(description)
                }
            }
        }
    }
}
