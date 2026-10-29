package com.example.generator2.features.explorer.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.generator2.features.explorer.model.ExploreNodeItem
import com.example.generator2.model.TreeNode

/**
 * Дерево всех аудиофайлов на устройстве
 *
 * Состояние Compose: фоновая инициализация публикует готовое дерево целиком,
 * и открытый проводник перечитывает его
 */
var treeAllAudio: TreeNode<ExploreNodeItem> by mutableStateOf(
    TreeNode(ExploreNodeItem("/", "/", isDirectory = true))
)


