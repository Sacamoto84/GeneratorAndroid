package com.example.generator2.features.explorer.domen

import com.example.generator2.features.explorer.model.ExplorerTreeNode

/**
 * ## Функция для построения дерева файлов из списка путей
 */
fun explorerBuildTree(fileList: List<String>): ExplorerTreeNode {
    val root = ExplorerTreeNode("/")
    for (path in fileList) {
        val components = path.split("/")
        var currentNode = root
        for (component in components.drop(1)) { // пропускаем первый элемент, так как он пустой из-за начального слэша
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
                currentNode.addChild(newNode)
                currentNode = newNode
            }
        }
    }
    return root
}