package com.example.generator2.features.explorer.data

import com.example.generator2.features.explorer.model.ExploreNodeItem
import com.example.generator2.model.TreeNode

/**
 * Дерево всех аудиофайлов на устройстве
 */
var treeAllAudio: TreeNode<ExploreNodeItem> = TreeNode(ExploreNodeItem("/", "/"))