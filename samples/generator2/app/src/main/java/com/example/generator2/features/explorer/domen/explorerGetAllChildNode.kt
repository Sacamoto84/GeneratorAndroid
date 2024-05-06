package com.example.generator2.features.explorer.domen

import com.example.generator2.model.TreeNode

/**
 * ## Получить список дочерних нодов текущей TreeNode
 */
fun <T> explorerGetAllChildNode(node : TreeNode<T>): List<TreeNode<T>> {
   return node.children.toList()
}