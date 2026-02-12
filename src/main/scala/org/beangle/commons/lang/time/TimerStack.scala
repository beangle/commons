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

package org.beangle.commons.lang.time

import java.util.Arrays

/** Stack of TimerNodes for profiling (push/pop).
 *
 * @author chaostone
 * @since 3.0.0
 */
class TimerStack(root: TimerNode, initCapacity: Int) {

  /** Current stack index (-1 when empty). */
  var index: Int = -1

  /** Stack storage array. */
  var nodes: Array[TimerNode] = new Array[TimerNode](initCapacity)

  push(root)

  /** Creates TimerStack with default capacity (15). */
  def this(root: TimerNode) = {
    this(root, 15)
  }

  private def ensureCapacity(): Unit =
    if (index >= nodes.length) {
      val newCapacity = nodes.length * 2
      nodes = Arrays.copyOf(nodes, newCapacity)
    }

  /** Pushes a timer node onto the stack.
   *
   * @param node the node to push
   */
  def push(node: TimerNode): Unit = {
    ensureCapacity()
    nodes(index) = node
  }

  /** Pops the top timer node from the stack.
   *
   * @return the top node, or null if empty
   */
  def pop(): TimerNode = {
    if (index < 0) return null
    val top = nodes(index)
    nodes(index) = null
    index -= 1
    top
  }

  /** Returns the top timer node without removing it.
   *
   * @return the top node, or null if empty
   */
  def peek(): TimerNode = {
    if (index < 0) return null
    nodes(index)
  }
}
