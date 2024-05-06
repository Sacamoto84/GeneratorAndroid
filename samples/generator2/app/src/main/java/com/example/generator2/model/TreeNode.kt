package com.example.generator2.model

typealias Visitor<T> = (TreeNode<T>) -> Unit

// val root = TreeNode("A")
// val b = TreeNode("B")
// val c = TreeNode("C")
// val d = TreeNode("D")
// val e = TreeNode("E")
//
// root.add(b)
// root.add(c)
// b.add(d)
// b.add(e)


//Не работает фильтр
fun <T> countNodes(node: TreeNode<T>?, filter : (TreeNode<T>?) -> Boolean = {true}): Int {
    var count = 0
    traverseTree(node) {
        if (filter(node)){
            count++
        }
    }
    return count
}





/*
*  // Предположим, что rootNode - это корневой узел вашего дерева
*  traverseTree(rootNode) { node ->
*      // Здесь вы можете выполнять действия с каждым узлом, например, выводить его значение
*       println(node.value)
*  }
*/
fun <T> traverseTree(node: TreeNode<T>?, action: (TreeNode<T>) -> Unit) {
    if (node != null) {
        action(node)
        for (child in node.children) {
            traverseTree(child, action)
        }
    }
}


class TreeNode<T>(val value: T) {

    var parent: TreeNode<T>? = null

    val children: MutableList<TreeNode<T>> = mutableListOf()

    fun add(child: TreeNode<T>) {
        child.parent = this
        children.add(child)
    }


    /**
     * Получение полного списка пути от текущего узла до корня
     */
    fun pathToRoot(): List<T> {
        val path = mutableListOf<T>()
        var current: TreeNode<T>? = this

        while (current != null) {
            path.add(current.value)
            current = current.parent
        }

        path.reverse() // Переворачиваем список, чтобы путь шёл от корня к текущему узлу
        return path
    }


    /**
     * Печать дерева в порядке обхода по глубине
     * ```kotlin
     * root.forEachDepthFirst { println(it.value) }
     * ```
     */
    fun forEachDepthFirst(visit: Visitor<T>) {
        visit(this)
        children.forEach {
            it.forEachDepthFirst(visit)
        }
    }

    /**
     * Печать дерева в порядке обхода по уровням
     * ```kotlin
     * root.forEachLevelOrder { println(it.value) }
     * ```
     */
    fun forEachLevelOrder(visit: Visitor<T>) {
        visit(this)
        val queue = ArrayListQueue<TreeNode<T>>()
        children.forEach { queue.enqueue(it) }

        var node = queue.dequeue()
        while (node != null) {
            visit(node)
            node.children.forEach { queue.enqueue(it) }
            node = queue.dequeue()
        }
    }


    /**
     * Поиск узла со значением "D"
     * ```kotlin
     * val node = root.search("D")
     * println(node?.value) // Вывод: D
     * ```
     */
    fun search(value: T): TreeNode<T>? {
        var result: TreeNode<T>? = null

        forEachLevelOrder {
            if (it.value == value) {
                result = it
            }
        }

        return result
    }

    fun printEachLevel() {
        var level = 0
        // 1
        val queue = ArrayListQueue<TreeNode<T>>()
        var nodesLeftInCurrentLevel = 0
        queue.enqueue(this)
        // 2
        while (queue.isEmpty.not()) {
            println("Уровень ${level++}")
            // 3
            nodesLeftInCurrentLevel = queue.count
            // 4
            while (nodesLeftInCurrentLevel > 0) {
                val node = queue.dequeue()
                node?.let {
                    print("${node.value} ! ")
                    node.children.forEach { queue.enqueue(it) }
                    nodesLeftInCurrentLevel--
                } ?: break
            }
            // 5
            println()
        }
    }

}

