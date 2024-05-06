package com.example.generator2.features.explorer.domen

import com.example.generator2.features.explorer.model.ExplorerTreeNode

/**
 * ## Функция для построения дерева файлов из списка путей
 */
fun explorerTreeBuild(fileList: List<String>): ExplorerTreeNode {
    val root = ExplorerTreeNode("/")
    for (path in fileList) {
        val components = path.split("/")
        var currentNode = root
        for (component in components.drop(1)) {
            var foundChild: ExplorerTreeNode? = null
            for (child in currentNode.children) {
                if (child.name == component) {
                    foundChild = child
                    break
                }
            }
            if (foundChild != null) {
                currentNode = foundChild
            } else {
                val newNode = ExplorerTreeNode(component)
                newNode.parent = currentNode
                currentNode.addChild(newNode)
                currentNode = newNode
            }
        }
    }
    return root
}