package com.example.generator2.features.explorer.domen

import com.example.generator2.features.explorer.model.ExploreNodeItem
import com.example.generator2.model.TreeNode

/**
 * ## Функция для построения дерева файлов из списка путей
 */
fun explorerTreeBuild(fileList: List<String>): TreeNode<ExploreNodeItem> {
    val root = TreeNode(ExploreNodeItem("/", "/"))

    for (path in fileList) {
        val components = path.split("/")
        var currentNode = root
        for (component in components.drop(1)) {

            var foundChild: TreeNode<ExploreNodeItem>? = null

            for (child in currentNode.children) {
                if (child.value == ExploreNodeItem(component, "")) {
                    foundChild = child
                    break
                }
            }
            if (foundChild != null) {
                currentNode = foundChild
            } else {
                val newNode = TreeNode(ExploreNodeItem(component, ""))
                newNode.parent = currentNode
                currentNode.add(newNode)
                currentNode = newNode
            }
        }
    }
    return root
}