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
import scala.collection.mutable

/** Active span stack and completed root trees for one `TimerTrace.runScoped` scope.
 *
 * @author chaostone
 * @since 3.0.0
 */
class TimerStack(initCapacity: Int) {

  /** Minimum span duration (ms) for the current root tree. */
  var minMs: Int = 0

  /** Current stack index (-1 when no active span). */
  var index: Int = -1

  /** Stack storage array. */
  var nodes: Array[TimerNode] = new Array[TimerNode](initCapacity)

  private val roots = mutable.ListBuffer.empty[TimerNode]

  def this() = this(15)

  /** Completed root trees in this scope. */
  def completedRoots: Iterable[TimerNode] = roots

  /** Records a finished root tree when it passes `minMs` filtering. */
  def completeRoot(root: TimerNode, elapsed: Long): Unit = {
    if (elapsed >= minMs || root.children.nonEmpty) roots += root
  }

  private def ensureCapacity(): Unit =
    if (index >= nodes.length) {
      val newCapacity = nodes.length * 2
      nodes = Arrays.copyOf(nodes, newCapacity)
    }

  def push(node: TimerNode): Unit = {
    index += 1
    ensureCapacity()
    nodes(index) = node
  }

  def pop(): TimerNode = {
    if (index < 0) return null
    val top = nodes(index)
    nodes(index) = null
    index -= 1
    top
  }

  def peek(): TimerNode = {
    if (index < 0) return null
    nodes(index)
  }
}
