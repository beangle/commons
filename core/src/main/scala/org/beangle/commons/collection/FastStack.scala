package org.beangle.commons.collection

import scala.reflect.ClassTag

/**
 * Array based Stack
 */
final class FastStack[T: ClassTag](initialCapacity: Int = 16) {

  private var stack: Array[T] = new Array[T](initialCapacity)
  // pointer to top empty slot
  private var pointer: Int = 0

  def push(value: T): Unit = {
    if (pointer + 1 >= stack.length) resizeStack(stack.length * 2)
    stack(pointer) = value
    pointer += 1
  }

  def popSilently(): Unit = {
    pointer -= 1
    stack(pointer) = null.asInstanceOf[T]
  }

  def pop(): T = {
    pointer -= 1
    val result = stack(pointer)
    stack(pointer) = null.asInstanceOf[T]
    result
  }

  def peek(): T = {
    if (pointer == 0) null.asInstanceOf[T] else stack(pointer - 1)
  }

  def replace(value: T): T = {
    val result = stack(pointer - 1)
    stack(pointer - 1) = value
    result
  }

  def size(): Int = {
    pointer
  }

  def isEmpty(): Boolean = {
    pointer == 0
  }

  def get(i: Int): T = {
    stack(i)
  }

  def toArray(): Array[T] = {
    val result = new Array[T](pointer)
    System.arraycopy(stack, 0, result, 0, pointer)
    result
  }

  override def toString(): String = {
    val result = new StringBuffer("[")
    (0 until pointer) foreach { i =>
      if (i > 0) result.append(", ")
      result.append(stack(i))
    }
    result.append(']')
    result.toString
  }

  private def resizeStack(newCapacity: Int): Unit = {
    val newStack = new Array[T](newCapacity)
    System.arraycopy(stack, 0, newStack, 0, Math.min(pointer, newCapacity))
    stack = newStack
  }
}
