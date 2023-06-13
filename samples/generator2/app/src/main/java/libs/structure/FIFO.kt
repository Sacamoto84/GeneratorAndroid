package libs.structure

class FIFO<T>(private val capacity: Int) {

    private val queue = ArrayDeque<T>(capacity)

    //Добавить элемент в очередь, если очередь полная то удаляет первый элемент
    fun enqueue(item: T) {
        while (queue.size >= capacity) {
            queue.removeFirst()
        }
        queue.addLast(item)
    }

    fun dequeue(): T? = queue.removeFirst()
    fun peek(): T?    = queue.firstOrNull()
    fun isFull()      = queue.size >= capacity
    fun isEmpty()     = queue.isEmpty()
    fun clear()       = queue.clear()
    fun size()        = queue.size

}