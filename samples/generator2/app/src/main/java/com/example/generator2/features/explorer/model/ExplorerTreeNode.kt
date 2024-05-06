package com.example.generator2.features.explorer.model

class ExplorerTreeNode(val name: String) {

    var parent: ExplorerTreeNode? = null
    val children = mutableListOf<ExplorerTreeNode>()

    fun addChild(child: ExplorerTreeNode) {
        child.parent = this
        children.add(child)
    }

//    fun getParent(): ExplorerTreeNode? {
//        return parent
//    }

}

