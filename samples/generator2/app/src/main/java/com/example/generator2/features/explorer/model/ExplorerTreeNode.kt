package com.example.generator2.features.explorer.model

class ExplorerTreeNode(val name: String) {
    val children = mutableListOf<ExplorerTreeNode>()

    fun addChild(child: ExplorerTreeNode) {
        children.add(child)
    }
}
