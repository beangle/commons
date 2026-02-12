/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.collection

import scala.reflect.ClassTag

/** Array-based stack for high performance. Not thread-safe.
 *
 * @param initialCapacity initial array capacity
 */
final class FastStack[T: ClassTag](initialCapacity: Int = 16) {

  private var stack: Array[T] = new Array[T](initialCapacity)
  private var pointer: Int = 0

  /** Pushes a value onto the stack.
   *
   * @param value the value to push
   */
  def push(value: T): Unit = {
    if (pointer + 1 >= stack.length) resizeStack(stack.length * 2)
    stack(pointer) = value
    pointer += 1
  }

  /** Pops the top element without returning it. Assumes non-empty stack. */
  def popSilently(): Unit = {
    pointer -= 1
    stack(pointer) = null.asInstanceOf[T]
  }

  /** Pops and returns the top element. */
  def pop(): T = {
    pointer -= 1
    val result = stack(pointer)
    stack(pointer) = null.asInstanceOf[T]
    result
  }

  /** Returns the top element without removing it. */
  def peek(): T = {
    if (pointer == 0) null.asInstanceOf[T] else stack(pointer - 1)
  }

  /** Replaces the top element with the given value.
   *
   * @param value the new value
   * @return the previous top element
   */
  def replace(value: T): T = {
    val result = stack(pointer - 1)
    stack(pointer - 1) = value
    result
  }

  /** Returns the number of elements on the stack. */
  def size(): Int = pointer

  /** Returns true if the stack has no elements. */
  def isEmpty: Boolean = pointer == 0

  /** Gets the element at the given index (0 = bottom).
   *
   * @param i the index
   * @return the element
   */
  def get(i: Int): T = stack(i)

  /** Copies the stack elements to a new array (bottom to top). */
  def toArray: Array[T] = {
    val result = new Array[T](pointer)
    System.arraycopy(stack, 0, result, 0, pointer)
    result
  }

  override def toString: String = {
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
