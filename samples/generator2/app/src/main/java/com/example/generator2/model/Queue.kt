package com.example.generator2.model

interface Queue<T> {

  fun enqueue(element: T): Boolean

  fun dequeue(): T?

  val count: Int
    get

  val isEmpty: Boolean
    get() = count == 0

  fun peek(): T?
}