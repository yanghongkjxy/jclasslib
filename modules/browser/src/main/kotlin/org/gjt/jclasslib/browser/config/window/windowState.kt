/*
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2 of the license, or (at your option) any later version.
 */

package org.gjt.jclasslib.browser.config.window

import org.gjt.jclasslib.browser.NodeType
import java.util.*

// keep vars for bean serialization

// TODO XSL transform: move one package up
class WindowState {

    var fileName: String? = null
    var browserPath: BrowserPath? = null

    constructor(fileName: String, browserPath: BrowserPath?) {
        this.fileName = fileName
        this.browserPath = browserPath
    }

    constructor(fileName: String) {
        this.fileName = fileName
    }

    constructor() {

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as WindowState

        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        return fileName?.hashCode() ?: 0
    }
}

class BrowserPath {
    var pathComponents = LinkedList<PathComponent>()
    fun addPathComponent(pathComponent: PathComponent) {
        pathComponents.add(pathComponent)
    }
}

interface PathComponent
data class CategoryHolder(var category: NodeType = NodeType.NO_CONTENT) : PathComponent {
    // backwards compatibility with pre-5.0 format, called by XMLDecoder
    fun setCategory(name: String) {
        category = NodeType.values().find { it.name.toLowerCase() == name } ?: NodeType.NO_CONTENT
    }
}

data class IndexHolder(var index: Int = -1) : PathComponent
data class ReferenceHolder(var name: String = "", var type: String = "") : PathComponent