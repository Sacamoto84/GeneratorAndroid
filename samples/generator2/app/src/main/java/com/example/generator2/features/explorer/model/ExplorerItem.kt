package com.example.generator2.features.explorer.model

import com.example.generator2.model.TreeNode

data class ExplorerItem(
    val node: TreeNode<ExploreNodeItem>,
    val name: String = "",         //имя файла
    val spec : Boolean = false,    //специальный элемент
    var counterItems: Int = 0 //Только для директорий, показ количества и темов которые есть в директории
)