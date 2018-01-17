/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2018, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang.time

import java.util.Arrays

/**
 * Record timer nodes
 *
 * @author chaostone
 * @since 3.0.0
 */
class TimerStack(root: TimerNode, initCapacity: Int) {

  var index: Int = -1

  var nodes: Array[TimerNode] = new Array[TimerNode](initCapacity)

  push(root)

  def this(root: TimerNode) {
    this(root, 15)
  }

  private def ensureCapacity() {
    if (index >= nodes.length) {
      val newCapacity = nodes.length * 2
      nodes = Arrays.copyOf(nodes, newCapacity)
    }
  }

  def push(node: TimerNode) {
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
