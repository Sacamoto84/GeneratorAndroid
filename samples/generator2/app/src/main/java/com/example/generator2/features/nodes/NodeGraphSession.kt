package com.example.generator2.features.nodes

import com.example.generator2.features.nodes.model.NodeGraph
import com.example.generator2.features.nodes.model.newGraph
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Последний открытый на экране граф.
 *
 * VMNodes — ScreenModel: он умирает, когда экран графа уходит из стека, и
 * при возврате создаётся заново с пустым графом. Этот синглтон переживает
 * пересоздание, поэтому вернувшись на экран, пользователь видит тот же граф,
 * а не New. Хранит только данные (граф, имя, флаг правок); масштаб, выделение
 * и ориентация — вид, они восстанавливаться не обязаны.
 */
@Singleton
class NodeGraphSession @Inject constructor() {
    var graph: NodeGraph = newGraph()
    var name: String = "New"
    var dirty: Boolean = false
}
