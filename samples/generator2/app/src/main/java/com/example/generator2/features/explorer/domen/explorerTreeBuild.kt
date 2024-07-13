package com.example.generator2.features.explorer.domen

import com.example.generator2.features.explorer.model.ExploreNodeItem
import com.example.generator2.model.TreeNode

fun isDirectory(path: String): Boolean {
    return path.endsWith("/")
}

///**
// * ## Функция для построения дерева файлов из списка путей
// */
//fun explorerTreeBuild(fileList: List<String>, isS3 : Boolean = false, fileListURI : List<String> = emptyList()): TreeNode<ExploreNodeItem> {
//    val root = TreeNode(ExploreNodeItem("/", "/"))
//
//    for (path in fileList) {
//
//        val isDirectory = isDirectory(path)
//
//        val components = path.split("/")
//        var currentNode = root
//        for (component in components.drop(1)) {
//
//            var foundChild: TreeNode<ExploreNodeItem>? = null
//
//            for (child in currentNode.children) {
//                if (child.value == ExploreNodeItem(component, "", isS3 = isS3)) {
//                    foundChild = child
//                    break
//                }
//            }
//
//            if (foundChild != null) {
//                currentNode = foundChild
//            } else {
//                val newNode = TreeNode(ExploreNodeItem(component, "", isS3 = isS3))
//                newNode.parent = currentNode
//                currentNode.add(newNode)
//                currentNode = newNode
//            }
//        }
//    }
//    return root
//}

/**
 * ## Функция для построения дерева файлов из списка путей
 */
fun explorerTreeBuild(
    fileList: List<String>,
    isS3: Boolean = false,
    fileListURI: List<String> = emptyList()
): TreeNode<ExploreNodeItem> {
    val root = TreeNode(ExploreNodeItem("/", "/"))

    fileList.forEachIndexed { index, path ->

        val isDirectory = isDirectory(path)

        val components = path.split("/")
        var currentNode = root
        //for (component in components.drop(1)) {

        components.forEachIndexed innerLoop@{ index1, component ->

            if (index1 == 0) return@innerLoop

            var foundChild: TreeNode<ExploreNodeItem>? = null

            for (child in currentNode.children) {
                if (child.value == ExploreNodeItem(component, "", isS3 = isS3)) {
                    foundChild = child
                    break
                }
            }

            if (foundChild != null) {
                currentNode = foundChild
            } else {
                val newNode = TreeNode(ExploreNodeItem(component, "", isS3 = isS3))
                newNode.parent = currentNode
                currentNode.add(newNode)
                currentNode = newNode
            }
        }
    }
    return root
}

/**
 * ## Функция для построения дерева файлов из списка путей
 */
fun explorerTreeBuildS3(
    fileList: List<FileInfoS3>,
): TreeNode<ExploreNodeItem> {

    val root = TreeNode(ExploreNodeItem("rabbit", "", uri = "https://ru-spb-s3.hexcore.cloud/rabbit/"))

    fileList.forEach {

        val components = it.url.replace("https://ru-spb-s3.hexcore.cloud/rabbit/","").split("/")

        var currentNode = root

        //for (component in components.drop(1)) {

        components.forEach { component ->

            var foundChild: TreeNode<ExploreNodeItem>? = null

            for (child in currentNode.children) {

                if (child.value.name == component)
                {
                    foundChild = child
                    break
                }

            }

            if (foundChild != null) {
                currentNode = foundChild
            } else {

                val newNode = TreeNode(
                    ExploreNodeItem(
                        component,
                        "",
                        isS3 = true,
                    )
                )
                newNode.parent = currentNode
                currentNode.add(newNode)
                currentNode = newNode
            }
        }
    }
    return root
}